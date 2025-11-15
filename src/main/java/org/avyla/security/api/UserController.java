package org.avyla.security.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.avyla.security.api.dto.request.AuthCreateUserRequest;
import org.avyla.security.api.dto.request.ChangePasswordRequest;
import org.avyla.security.api.dto.request.UpdateUserRequest;
import org.avyla.security.api.dto.response.UserListResponse;
import org.avyla.security.api.dto.response.UserResponse;
import org.avyla.security.application.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for user profile and management operations
 *
 * Security:
 * - All endpoints require authentication via JWT
 * - List/detail endpoints require ADMIN or SUPERVISOR role
 * - Create/Update/Delete endpoints require ADMIN role only
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ========== CONSULTAS ==========

    /**
     * Get current authenticated user's profile
     * Available to all authenticated users
     *
     * @param userDetails Spring Security user details from JWT
     * @return Current user's profile information
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        UserResponse response = userService.getCurrentUserProfile(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * Get list of all users in the system
     * Only accessible by ADMIN and SUPERVISOR roles
     *
     * @return List of all active users with basic information
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<List<UserListResponse>> getAllUsers() {
        List<UserListResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Get detailed information of a specific user by ID
     * Only accessible by ADMIN and SUPERVISOR roles
     *
     * @param userId User ID to retrieve
     * @return Detailed user information
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Search users by query string
     * Only accessible by ADMIN and SUPERVISOR roles
     *
     * @param query Search term (username, email, name)
     * @return List of matching users
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<List<UserListResponse>> searchUsers(
            @RequestParam String query) {
        List<UserListResponse> users = userService.searchUsers(query);
        return ResponseEntity.ok(users);
    }

    // ========== CREACIÓN ==========

    /**
     * Create a new user (ADMIN only)
     * Creates user with specified roles
     *
     * @param request User creation request
     * @return Created user information
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody AuthCreateUserRequest request) {
        UserResponse response = userService.createUserByAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ========== ACTUALIZACIÓN ==========

    /**
     * Update user information (ADMIN only)
     * Updates only the fields provided in the request
     *
     * @param userId User ID to update
     * @param request Update request with fields to change
     * @return Updated user information
     */
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = userService.updateUser(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Change user password (ADMIN only)
     * Allows ADMIN to change any user's password
     *
     * @param userId User ID
     * @param request New password
     * @return Success response
     */
    @PatchMapping("/{userId}/password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> changeUserPassword(
            @PathVariable Long userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changeUserPassword(userId, request);
        return ResponseEntity.noContent().build();
    }

    // ========== DESACTIVACIÓN Y ACTIVACIÓN (NUEVO) ==========

    /**
     * Disable user (set isEnabled = false) without soft delete
     * User cannot login but remains in system and can be re-enabled
     * Different from soft delete - user still appears in user lists
     *
     * @param userId User ID to disable
     * @return Success response
     */
    @PatchMapping("/{userId}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> disableUser(@PathVariable Long userId) {
        userService.disableUser(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Enable a previously disabled user
     * User will be able to login again
     *
     * @param userId User ID to enable
     * @return Success response
     */
    @PatchMapping("/{userId}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> enableUser(@PathVariable Long userId) {
        userService.enableUser(userId);
        return ResponseEntity.noContent().build();
    }

    // ========== ELIMINACIÓN (SOFT DELETE) ==========

    /**
     * Soft delete a user (ADMIN only)
     * Marks user as deleted without removing from database
     * User will not appear in most queries and cannot be re-enabled
     * Use disableUser() instead if you want to temporarily disable
     *
     * @param userId User ID to delete
     * @return Success response
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.softDeleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Restore a soft-deleted user (ADMIN only)
     * Clears deletedAt timestamp and enables the user
     *
     * @param userId User ID to restore
     * @return Success response
     */
    @PostMapping("/{userId}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> restoreUser(@PathVariable Long userId) {
        userService.restoreUser(userId);
        return ResponseEntity.noContent().build();
    }
}