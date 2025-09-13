package io.github.kaltrinabajramii.urbantransitbackend.service.impl;

import io.github.kaltrinabajramii.urbantransitbackend.dto.request.UpdateProfileRequest;
import io.github.kaltrinabajramii.urbantransitbackend.dto.request.ChangePasswordRequest;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.UserResponse;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.UserSummaryResponse;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.PagedResponse;
import io.github.kaltrinabajramii.urbantransitbackend.model.entity.User;
import io.github.kaltrinabajramii.urbantransitbackend.model.enums.UserRole;
import io.github.kaltrinabajramii.urbantransitbackend.repository.UserRepository;
import io.github.kaltrinabajramii.urbantransitbackend.security.UserPrincipal;
import io.github.kaltrinabajramii.urbantransitbackend.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of UserService for handling user management operations
 * Manages user profiles, admin operations, and user analytics
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ===== USER PROFILE MANAGEMENT =====

    /**
     * Get current user's profile information
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<UserResponse> getCurrentUserProfile(UserDetails userDetails) {
        log.info("Getting profile for user: {}", userDetails.getUsername());

        try {
            User user = getCurrentUser(userDetails);
            UserResponse userResponse = mapToUserResponse(user);

            log.debug("Profile retrieved successfully for user: {}", user.getEmail());
            return ResponseEntity.ok(userResponse);

        } catch (Exception e) {
            log.error("Error getting user profile for: {}", userDetails.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update current user's profile information
     */
    @Override
    public ResponseEntity<UserResponse> updateProfile(UserDetails userDetails, UpdateProfileRequest updateRequest) {
        log.info("Updating profile for user: {}", userDetails.getUsername());

        try {
            User user = getCurrentUser(userDetails);

            // Update profile fields
            user.setFullName(updateRequest.getFullName().trim());

            // Save updated user
            User updatedUser = userRepository.save(user);

            UserResponse userResponse = mapToUserResponse(updatedUser);

            log.info("Profile updated successfully for user: {}", user.getEmail());
            return ResponseEntity.ok(userResponse);

        } catch (Exception e) {
            log.error("Error updating profile for user: {}", userDetails.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Change current user's password
     */
    @Override
    public ResponseEntity<String> changePassword(UserDetails userDetails, ChangePasswordRequest changePasswordRequest) {
        log.info("Password change request for user: {}", userDetails.getUsername());

        try {
            User user = getCurrentUser(userDetails);

            // Verify current password
            if (!passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), user.getPasswordHash())) {
                log.warn("Password change failed: Invalid current password for user: {}", user.getEmail());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Current password is incorrect");
            }

            // Update password
            user.setPasswordHash(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
            userRepository.save(user);

            log.info("Password changed successfully for user: {}", user.getEmail());
            return ResponseEntity.ok("Password changed successfully");

        } catch (Exception e) {
            log.error("Error changing password for user: {}", userDetails.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to change password");
        }
    }

    /**
     * Deactivate current user's account
     */
    @Override
    public ResponseEntity<String> deactivateAccount(UserDetails userDetails) {
        log.info("Account deactivation request for user: {}", userDetails.getUsername());

        try {
            User user = getCurrentUser(userDetails);

            // Prevent admin from deactivating their own account if they're the last admin
            if (user.getRole() == UserRole.ADMIN) {
                long adminCount = userRepository.findByRole(UserRole.ADMIN).size();
                if (adminCount <= 1) {
                    log.warn("Deactivation failed: Cannot deactivate last admin account: {}", user.getEmail());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Cannot deactivate the last admin account");
                }
            }

            user.setActive(false);
            userRepository.save(user);

            log.info("Account deactivated successfully for user: {}", user.getEmail());
            return ResponseEntity.ok("Account deactivated successfully");

        } catch (Exception e) {
            log.error("Error deactivating account for user: {}", userDetails.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to deactivate account");
        }
    }

    // ===== USER LOOKUP OPERATIONS =====

    /**
     * Get user by ID (for admin operations)
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<UserResponse> getUserById(Long userId) {
        log.info("Getting user by ID: {}", userId);

        try {
            User user = userRepository.findById(userId)
                    .orElse(null);

            if (user == null) {
                log.warn("User not found with ID: {}", userId);
                return ResponseEntity.notFound().build();
            }

            UserResponse userResponse = mapToUserResponse(user);
            return ResponseEntity.ok(userResponse);

        } catch (Exception e) {
            log.error("Error getting user by ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get user by email address
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<UserResponse> getUserByEmail(String email) {
        log.info("Getting user by email: {}", email);

        try {
            User user = userRepository.findByEmail(email.toLowerCase().trim())
                    .orElse(null);

            if (user == null) {
                log.warn("User not found with email: {}", email);
                return ResponseEntity.notFound().build();
            }

            UserResponse userResponse = mapToUserResponse(user);
            return ResponseEntity.ok(userResponse);

        } catch (Exception e) {
            log.error("Error getting user by email: {}", email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Check if user exists by ID
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long userId) {
        try {
            return userRepository.findById(userId)
                    .map(user -> user.getActive())
                    .orElse(false);
        } catch (Exception e) {
            log.error("Error checking user existence for ID: {}", userId, e);
            return false;
        }
    }

    // ===== ADMIN USER MANAGEMENT =====

    /**
     * Get all users with pagination (admin only)
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<PagedResponse<UserSummaryResponse>> getAllUsers(int page, int size) {
        log.info("Getting all users - page: {}, size: {}", page, size);

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> userPage = userRepository.findByActiveTrue(pageable);

            List<UserSummaryResponse> userSummaries = userPage.getContent().stream()
                    .map(this::mapToUserSummaryResponse)
                    .collect(Collectors.toList());

            PagedResponse<UserSummaryResponse> pagedResponse = PagedResponse.of(
                    userSummaries,
                    userPage.getNumber(),
                    userPage.getSize(),
                    userPage.getTotalElements(),
                    userPage.getTotalPages()
            );

            return ResponseEntity.ok(pagedResponse);

        } catch (Exception e) {
            log.error("Error getting all users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Search users by name or email (admin only)
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<PagedResponse<UserSummaryResponse>> searchUsers(String searchTerm, int page, int size) {
        log.info("Searching users with term: '{}' - page: {}, size: {}", searchTerm, page, size);

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> userPage = userRepository.searchActiveUsers(searchTerm, pageable);

            List<UserSummaryResponse> userSummaries = userPage.getContent().stream()
                    .map(this::mapToUserSummaryResponse)
                    .collect(Collectors.toList());

            PagedResponse<UserSummaryResponse> pagedResponse = PagedResponse.of(
                    userSummaries,
                    userPage.getNumber(),
                    userPage.getSize(),
                    userPage.getTotalElements(),
                    userPage.getTotalPages()
            );

            return ResponseEntity.ok(pagedResponse);

        } catch (Exception e) {
            log.error("Error searching users with term: {}", searchTerm, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get users by role (admin only)
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<List<UserSummaryResponse>> getUsersByRole(UserRole role) {
        log.info("Getting users by role: {}", role);

        try {
            List<User> users = userRepository.findByRole(role);

            List<UserSummaryResponse> userSummaries = users.stream()
                    .filter(User::getActive)
                    .map(this::mapToUserSummaryResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(userSummaries);

        } catch (Exception e) {
            log.error("Error getting users by role: {}", role, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update user role (admin only)
     */
    @Override
    public ResponseEntity<UserResponse> updateUserRole(Long userId, UserRole newRole) {
        log.info("Updating user role for ID: {} to role: {}", userId, newRole);

        try {
            User user = userRepository.findById(userId)
                    .orElse(null);

            if (user == null) {
                log.warn("User not found with ID: {}", userId);
                return ResponseEntity.notFound().build();
            }

            // Prevent removing admin role if user is the last admin
            if (user.getRole() == UserRole.ADMIN && newRole != UserRole.ADMIN) {
                long adminCount = userRepository.findByRole(UserRole.ADMIN).size();
                if (adminCount <= 1) {
                    log.warn("Role update failed: Cannot remove admin role from last admin: {}", user.getEmail());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                }
            }

            user.setRole(newRole);
            User updatedUser = userRepository.save(user);

            UserResponse userResponse = mapToUserResponse(updatedUser);

            log.info("User role updated successfully for user: {} to role: {}", user.getEmail(), newRole);
            return ResponseEntity.ok(userResponse);

        } catch (Exception e) {
            log.error("Error updating user role for ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Activate/Deactivate user account (admin only)
     */
    @Override
    public ResponseEntity<String> updateUserStatus(Long userId, boolean active) {
        log.info("Updating user status for ID: {} to active: {}", userId, active);

        try {
            User user = userRepository.findById(userId)
                    .orElse(null);

            if (user == null) {
                log.warn("User not found with ID: {}", userId);
                return ResponseEntity.notFound().build();
            }

            // Prevent deactivating the last admin
            if (user.getRole() == UserRole.ADMIN && !active) {
                long activeAdminCount = userRepository.findByRole(UserRole.ADMIN).stream()
                        .mapToLong(u -> u.getActive() ? 1 : 0)
                        .sum();
                if (activeAdminCount <= 1) {
                    log.warn("Status update failed: Cannot deactivate last admin: {}", user.getEmail());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Cannot deactivate the last admin account");
                }
            }

            user.setActive(active);
            userRepository.save(user);

            String message = active ? "User activated successfully" : "User deactivated successfully";
            log.info("User status updated successfully for user: {} to active: {}", user.getEmail(), active);
            return ResponseEntity.ok(message);

        } catch (Exception e) {
            log.error("Error updating user status for ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update user status");
        }
    }

    /**
     * Delete user account permanently (admin only)
     */
    @Override
    public ResponseEntity<String> deleteUser(Long userId) {
        log.info("Deleting user with ID: {}", userId);

        try {
            User user = userRepository.findById(userId)
                    .orElse(null);

            if (user == null) {
                log.warn("User not found with ID: {}", userId);
                return ResponseEntity.notFound().build();
            }

            // Prevent deleting the last admin
            if (user.getRole() == UserRole.ADMIN) {
                long adminCount = userRepository.findByRole(UserRole.ADMIN).size();
                if (adminCount <= 1) {
                    log.warn("Delete failed: Cannot delete last admin: {}", user.getEmail());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Cannot delete the last admin account");
                }
            }

            // TODO: Check for related data (tickets, etc.) before deletion
            // For now, we'll soft delete by setting active to false
            user.setActive(false);
            userRepository.save(user);

            log.info("User soft-deleted successfully: {}", user.getEmail());
            return ResponseEntity.ok("User deleted successfully");

        } catch (Exception e) {
            log.error("Error deleting user with ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete user");
        }
    }

    // ===== USER ANALYTICS =====

    /**
     * Get users without any tickets (for marketing campaigns)
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<PagedResponse<UserSummaryResponse>> getUsersWithoutTickets(int page, int size) {
        log.info("Getting users without tickets - page: {}, size: {}", page, size);

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> userPage = userRepository.findUsersWithoutTickets(pageable);

            List<UserSummaryResponse> userSummaries = userPage.getContent().stream()
                    .map(this::mapToUserSummaryResponse)
                    .collect(Collectors.toList());

            PagedResponse<UserSummaryResponse> pagedResponse = PagedResponse.of(
                    userSummaries,
                    userPage.getNumber(),
                    userPage.getSize(),
                    userPage.getTotalElements(),
                    userPage.getTotalPages()
            );

            return ResponseEntity.ok(pagedResponse);

        } catch (Exception e) {
            log.error("Error getting users without tickets", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get recently registered users
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<PagedResponse<UserSummaryResponse>> getRecentlyRegisteredUsers(int page, int size) {
        log.info("Getting recently registered users - page: {}, size: {}", page, size);

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> userPage = userRepository.findRecentlyRegisteredUsers(pageable);

            List<UserSummaryResponse> userSummaries = userPage.getContent().stream()
                    .map(this::mapToUserSummaryResponse)
                    .collect(Collectors.toList());

            PagedResponse<UserSummaryResponse> pagedResponse = PagedResponse.of(
                    userSummaries,
                    userPage.getNumber(),
                    userPage.getSize(),
                    userPage.getTotalElements(),
                    userPage.getTotalPages()
            );

            return ResponseEntity.ok(pagedResponse);

        } catch (Exception e) {
            log.error("Error getting recently registered users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get user statistics by role
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Map<UserRole, Long>> getUserCountByRole() {
        log.info("Getting user count by role");

        try {
            List<Object[]> results = userRepository.countUsersByRole();

            Map<UserRole, Long> userCountByRole = results.stream()
                    .collect(Collectors.toMap(
                            result -> (UserRole) result[0],
                            result -> (Long) result[1]
                    ));

            return ResponseEntity.ok(userCountByRole);

        } catch (Exception e) {
            log.error("Error getting user count by role", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===== UTILITY METHODS =====

    /**
     * Convert UserDetails to User entity (internal use)
     */
    @Override
    public User getCurrentUser(UserDetails userDetails) {
        if (userDetails instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) userDetails;
            return userRepository.findById(userPrincipal.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        } else {
            return userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }
    }

    // ===== PRIVATE HELPER METHODS =====

    /**
     * Convert User entity to UserResponse DTO
     */
    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setRole(user.getRole());
        response.setActive(user.getActive());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }

    /**
     * Convert User entity to UserSummaryResponse DTO
     */
    private UserSummaryResponse mapToUserSummaryResponse(User user) {
        UserSummaryResponse response = new UserSummaryResponse();
        response.setId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setActive(user.getActive());
        return response;
    }
}