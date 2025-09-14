package io.github.kaltrinabajramii.urbantransitbackend.controller.rest;

import io.github.kaltrinabajramii.urbantransitbackend.dto.request.UpdateProfileRequest;
import io.github.kaltrinabajramii.urbantransitbackend.dto.request.ChangePasswordRequest;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.UserResponse;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.UserSummaryResponse;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.PagedResponse;
import io.github.kaltrinabajramii.urbantransitbackend.model.enums.UserRole;
import io.github.kaltrinabajramii.urbantransitbackend.service.interfaces.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getCurrentUserProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return userService.getCurrentUserProfile(userDetails);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                                      @Valid @RequestBody UpdateProfileRequest updateRequest) {
        return userService.updateProfile(userDetails, updateRequest);
    }

    @PutMapping("/profile/password")
    public ResponseEntity<String> changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                                 @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        return userService.changePassword(userDetails, changePasswordRequest);
    }

    @DeleteMapping("/profile")
    public ResponseEntity<String> deactivateAccount(@AuthenticationPrincipal UserDetails userDetails) {
        return userService.deactivateAccount(userDetails);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedResponse<UserSummaryResponse>> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                                                          @RequestParam(defaultValue = "10") int size) {
        return userService.getAllUsers(page, size);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedResponse<UserSummaryResponse>> searchUsers(@RequestParam String searchTerm,
                                                                          @RequestParam(defaultValue = "0") int page,
                                                                          @RequestParam(defaultValue = "10") int size) {
        return userService.searchUsers(searchTerm, page, size);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        return userService.getUserById(userId);
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserSummaryResponse>> getUsersByRole(@PathVariable UserRole role) {
        return userService.getUsersByRole(role);
    }

    @PutMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUserRole(@PathVariable Long userId,
                                                       @RequestParam UserRole newRole) {
        return userService.updateUserRole(userId, newRole);
    }

    @PutMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateUserStatus(@PathVariable Long userId,
                                                   @RequestParam boolean active) {
        return userService.updateUserStatus(userId, active);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        return userService.deleteUser(userId);
    }

    @GetMapping("/without-tickets")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedResponse<UserSummaryResponse>> getUsersWithoutTickets(@RequestParam(defaultValue = "0") int page,
                                                                                     @RequestParam(defaultValue = "10") int size) {
        return userService.getUsersWithoutTickets(page, size);
    }

    @GetMapping("/recent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedResponse<UserSummaryResponse>> getRecentlyRegisteredUsers(@RequestParam(defaultValue = "0") int page,
                                                                                         @RequestParam(defaultValue = "10") int size) {
        return userService.getRecentlyRegisteredUsers(page, size);
    }

    @GetMapping("/stats/by-role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<UserRole, Long>> getUserCountByRole() {
        return userService.getUserCountByRole();
    }
}
