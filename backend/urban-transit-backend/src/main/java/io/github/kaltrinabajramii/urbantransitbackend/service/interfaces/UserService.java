package io.github.kaltrinabajramii.urbantransitbackend.service.interfaces;

import io.github.kaltrinabajramii.urbantransitbackend.dto.request.UpdateProfileRequest;
import io.github.kaltrinabajramii.urbantransitbackend.dto.request.ChangePasswordRequest;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.UserResponse;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.UserSummaryResponse;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.PagedResponse;
import io.github.kaltrinabajramii.urbantransitbackend.model.entity.User;
import io.github.kaltrinabajramii.urbantransitbackend.model.enums.UserRole;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.List;
import java.util.Map;

/**
 * Service interface for user management operations
 * Handles user profile management, user queries, and admin user operations
 */
public interface UserService {

    // ===== USER PROFILE MANAGEMENT =====

    /**
     * Get current user's profile information
     *
     * @param userDetails Current authenticated user
     * @return ResponseEntity containing user profile data
     */
    ResponseEntity<UserResponse> getCurrentUserProfile(UserDetails userDetails);

    /**
     * Update current user's profile information
     *
     * @param userDetails Current authenticated user
     * @param updateRequest Profile update data
     * @return ResponseEntity containing updated user profile
     */
    ResponseEntity<UserResponse> updateProfile(UserDetails userDetails, UpdateProfileRequest updateRequest);

    /**
     * Change current user's password
     *
     * @param userDetails Current authenticated user
     * @param changePasswordRequest Password change request
     * @return ResponseEntity with success/failure message
     */
    ResponseEntity<String> changePassword(UserDetails userDetails, ChangePasswordRequest changePasswordRequest);

    /**
     * Deactivate current user's account
     *
     * @param userDetails Current authenticated user
     * @return ResponseEntity with confirmation message
     */
    ResponseEntity<String> deactivateAccount(UserDetails userDetails);

    // ===== USER LOOKUP OPERATIONS =====

    /**
     * Get user by ID (for admin operations)
     *
     * @param userId User ID to lookup
     * @return ResponseEntity containing user details
     * @throws RuntimeException if user not found
     */
    ResponseEntity<UserResponse> getUserById(Long userId);

    /**
     * Get user by email address
     *
     * @param email Email address to lookup
     * @return ResponseEntity containing user details
     * @throws RuntimeException if user not found
     */
    ResponseEntity<UserResponse> getUserByEmail(String email);

    /**
     * Check if user exists by ID
     *
     * @param userId User ID to check
     * @return true if user exists and is active
     */
    boolean existsById(Long userId);

    // ===== ADMIN USER MANAGEMENT =====

    /**
     * Get all users with pagination (admin only)
     *
     * @param page Page number (0-based)
     * @param size Page size
     * @return ResponseEntity containing PagedResponse with list of users
     */
    ResponseEntity<PagedResponse<UserSummaryResponse>> getAllUsers(int page, int size);

    /**
     * Search users by name or email (admin only)
     *
     * @param searchTerm Search term for name or email
     * @param page Page number
     * @param size Page size
     * @return ResponseEntity containing PagedResponse with matching users
     */
    ResponseEntity<PagedResponse<UserSummaryResponse>> searchUsers(String searchTerm, int page, int size);

    /**
     * Get users by role (admin only)
     *
     * @param role User role to filter by
     * @return ResponseEntity containing list of users with specified role
     */
    ResponseEntity<List<UserSummaryResponse>> getUsersByRole(UserRole role);

    /**
     * Update user role (admin only)
     *
     * @param userId User ID to update
     * @param newRole New role to assign
     * @return ResponseEntity containing updated user details
     */
    ResponseEntity<UserResponse> updateUserRole(Long userId, UserRole newRole);

    /**
     * Activate/Deactivate user account (admin only)
     *
     * @param userId User ID to update
     * @param active New active status
     * @return ResponseEntity with confirmation message
     */
    ResponseEntity<String> updateUserStatus(Long userId, boolean active);

    /**
     * Delete user account permanently (admin only)
     *
     * @param userId User ID to delete
     * @return ResponseEntity with confirmation message
     */
    ResponseEntity<String> deleteUser(Long userId);

    // ===== USER ANALYTICS =====

    /**
     * Get users without any tickets (for marketing campaigns)
     *
     * @param page Page number
     * @param size Page size
     * @return ResponseEntity containing PagedResponse with users who haven't purchased tickets
     */
    ResponseEntity<PagedResponse<UserSummaryResponse>> getUsersWithoutTickets(int page, int size);

    /**
     * Get recently registered users
     *
     * @param page Page number
     * @param size Page size
     * @return ResponseEntity containing PagedResponse with recently registered users
     */
    ResponseEntity<PagedResponse<UserSummaryResponse>> getRecentlyRegisteredUsers(int page, int size);

    /**
     * Get user statistics by role
     *
     * @return ResponseEntity containing user count by role
     */
    ResponseEntity<Map<UserRole, Long>> getUserCountByRole();

    // ===== UTILITY METHODS =====

    /**
     * Convert UserDetails to User entity (internal use)
     *
     * @param userDetails Spring Security UserDetails
     * @return User entity
     */
    User getCurrentUser(UserDetails userDetails);
}