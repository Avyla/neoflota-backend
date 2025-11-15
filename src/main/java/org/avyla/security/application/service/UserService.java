package org.avyla.security.application.service;

import lombok.RequiredArgsConstructor;
import org.avyla.security.api.dto.request.AuthCreateUserRequest;
import org.avyla.security.api.dto.request.ChangePasswordRequest;
import org.avyla.security.api.dto.request.UpdateUserRequest;
import org.avyla.security.api.dto.response.UserListResponse;
import org.avyla.security.api.dto.response.UserResponse;
import org.avyla.security.domain.entity.Role;
import org.avyla.security.domain.entity.UserEntity;
import org.avyla.security.domain.repo.RoleRepository;
import org.avyla.security.domain.repo.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service layer for user profile and management operations
 * Handles business logic for user-related operations
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // ========== CONSULTAS ==========

    /**
     * Get current authenticated user's profile
     *
     * @param username Username from JWT token
     * @return User profile information
     * @throws RuntimeException if user not found
     */
    public UserResponse getCurrentUserProfile(String username) {
        UserEntity user = userRepository.findByUsernameAndNotDeleted(username)
                .orElseThrow(() -> new RuntimeException(
                        "User not found with username: " + username
                ));

        return mapToUserResponse(user);
    }

    /**
     * Get all active users in the system (for admin panel)
     * Excludes soft-deleted users
     *
     * @return List of all active users with basic information
     */
    public List<UserListResponse> getAllUsers() {
        List<UserEntity> users = userRepository.findAllNotDeleted();

        return users.stream()
                .map(this::mapToUserListResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get detailed information of a specific user by ID
     *
     * @param userId User ID to retrieve
     * @return Detailed user information
     * @throws RuntimeException if user not found or deleted
     */
    public UserResponse getUserById(Long userId) {
        UserEntity user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new RuntimeException(
                        "User not found with id: " + userId
                ));

        return mapToUserResponse(user);
    }

    /**
     * Search users by query string
     *
     * @param query Search term (matches username, email, firstName, lastName)
     * @return List of matching users
     */
    public List<UserListResponse> searchUsers(String query) {
        List<UserEntity> users = userRepository.searchUsers(query);

        return users.stream()
                .map(this::mapToUserListResponse)
                .collect(Collectors.toList());
    }

    // ========== CREACIÓN ==========

    /**
     * Create a new user (called by ADMIN via UserController)
     * Reuses logic from UserDetailServiceImpl but with additional validation
     *
     * @param request User creation request
     * @return Created user information
     * @throws IllegalArgumentException if username, email, or document number already exists
     */
    @Transactional
    public UserResponse createUserByAdmin(AuthCreateUserRequest request) {
        // Validar que no exista el username
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException(
                    "Username already exists: " + request.username()
            );
        }

        // Validar que no exista el email
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException(
                    "Email already exists: " + request.email()
            );
        }

        // ✅ NUEVO - Validar que no exista el document number (FIX DUPLICACIÓN)
        if (userRepository.existsByDocumentNumber(request.documentNumber())) {
            throw new IllegalArgumentException(
                    "Document number already exists: " + request.documentNumber()
            );
        }

        // Obtener roles
        List<String> rolesRequest = request.roleRequest().roleListName();
        Set<Role> roleEntitySet = roleRepository.findByRoleEnumIn(rolesRequest)
                .stream()
                .collect(Collectors.toSet());

        if (roleEntitySet.isEmpty()) {
            throw new IllegalArgumentException("No valid roles found for the user");
        }

        // Crear usuario
        UserEntity userEntity = UserEntity.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .email(request.email())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .documentType(request.documentIdentityType())
                .documentNumber(request.documentNumber())
                .phoneNumber(request.phoneNumber())
                .roles(roleEntitySet)
                .isEnable(true)
                .isAccountNoLocked(true)
                .isAccountNonExpired(true)
                .isCredentialsNonExpired(true)
                .build();

        UserEntity savedUser = userRepository.save(userEntity);

        return mapToUserResponse(savedUser);
    }

    // ========== ACTUALIZACIÓN ==========

    /**
     * Update user information (only ADMIN)
     *
     * @param userId User ID to update
     * @param request Update request with fields to change
     * @return Updated user information
     * @throws RuntimeException if user not found
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        // Buscar usuario
        UserEntity user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new RuntimeException(
                        "User not found with id: " + userId
                ));

        // Verificar que haya al menos un campo para actualizar
        if (!request.hasAnyFieldToUpdate()) {
            throw new IllegalArgumentException("No fields provided for update");
        }

        // Actualizar campos si están presentes
        if (request.username() != null) {
            // Validar que el nuevo username no exista
            if (userRepository.existsByUsernameAndNotSelf(request.username(), userId)) {
                throw new IllegalArgumentException(
                        "Username already exists: " + request.username()
                );
            }
            user.setUsername(request.username());
        }

        if (request.email() != null) {
            // Validar que el nuevo email no exista
            if (userRepository.existsByEmailAndNotSelf(request.email(), userId)) {
                throw new IllegalArgumentException(
                        "Email already exists: " + request.email()
                );
            }
            user.setEmail(request.email());
        }

        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }

        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }

        if (request.documentIdentityType() != null) {
            user.setDocumentType(request.documentIdentityType());
        }

        if (request.documentNumber() != null) {
            // ✅ NUEVO - Validar que el nuevo document number no exista (FIX DUPLICACIÓN)
            if (userRepository.existsByDocumentNumberAndNotSelf(request.documentNumber(), userId)) {
                throw new IllegalArgumentException(
                        "Document number already exists: " + request.documentNumber()
                );
            }
            user.setDocumentNumber(request.documentNumber());
        }

        if (request.phoneNumber() != null) {
            user.setPhoneNumber(request.phoneNumber());
        }

        if (request.isEnable() != null) {
            user.setIsEnable(request.isEnable());
        }

        // Actualizar roles si están presentes
        if (request.roleRequest() != null && request.roleRequest().roleListName() != null) {
            List<String> roleNames = request.roleRequest().roleListName();
            Set<Role> newRoles = roleRepository.findByRoleEnumIn(roleNames)
                    .stream()
                    .collect(Collectors.toSet());

            if (newRoles.isEmpty()) {
                throw new IllegalArgumentException("No valid roles found");
            }

            user.setRoles(newRoles);
        }

        // Guardar cambios
        UserEntity updatedUser = userRepository.save(user);

        return mapToUserResponse(updatedUser);
    }

    /**
     * Change user password (only ADMIN can change other users' passwords)
     *
     * @param userId User ID
     * @param request New password
     * @throws RuntimeException if user not found
     */
    @Transactional
    public void changeUserPassword(Long userId, ChangePasswordRequest request) {
        UserEntity user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new RuntimeException(
                        "User not found with id: " + userId
                ));

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    // ========== DESACTIVACIÓN Y ACTIVACIÓN ==========

    /**
     * Disable user (set isEnable = false) without soft delete
     * User cannot login but remains in system and can be re-enabled
     *
     * @param userId User ID to disable
     * @throws RuntimeException if user not found
     */
    @Transactional
    public void disableUser(Long userId) {
        UserEntity user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new RuntimeException(
                        "User not found with id: " + userId
                ));

        if (!user.getIsEnable()) {
            throw new IllegalArgumentException(
                    "User is already disabled: " + userId
            );
        }

        user.setIsEnable(false);
        userRepository.save(user);
    }

    /**
     * Enable a previously disabled user
     * User will be able to login again
     *
     * @param userId User ID to enable
     * @throws RuntimeException if user not found
     */
    @Transactional
    public void enableUser(Long userId) {
        UserEntity user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new RuntimeException(
                        "User not found with id: " + userId
                ));

        if (user.getIsEnable()) {
            throw new IllegalArgumentException(
                    "User is already enabled: " + userId
            );
        }

        user.setIsEnable(true);
        userRepository.save(user);
    }

    // ========== ELIMINACIÓN (SOFT DELETE) ==========

    /**
     * Soft delete a user (only ADMIN)
     * Sets deletedAt timestamp and disables the user
     * NOTE: This is different from disableUser() - soft deleted users
     * are excluded from most queries
     *
     * @param userId User ID to delete
     * @throws RuntimeException if user not found
     */
    @Transactional
    public void softDeleteUser(Long userId) {
        UserEntity user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new RuntimeException(
                        "User not found with id: " + userId
                ));

        user.softDelete();
        userRepository.save(user);
    }

    /**
     * Restore a soft-deleted user (only ADMIN)
     * Clears deletedAt timestamp and enables the user
     *
     * @param userId User ID to restore
     * @throws RuntimeException if user not found
     */
    @Transactional
    public void restoreUser(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(
                        "User not found with id: " + userId
                ));

        if (!user.isDeleted()) {
            throw new IllegalArgumentException(
                    "User is not deleted: " + userId
            );
        }

        user.restore();
        userRepository.save(user);
    }

    // ========== MAPPERS ==========

    /**
     * Map UserEntity to UserResponse DTO (detailed view)
     */
    private UserResponse mapToUserResponse(UserEntity user) {
        List<String> roleNames = user.getRoles().stream()
                .map(role -> role.getRoleEnum().name())
                .collect(Collectors.toList());

        return new UserResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getDocumentType(),
                user.getDocumentNumber(),
                user.getPhoneNumber(),
                roleNames,
                user.getIsEnable()
        );
    }

    /**
     * Map UserEntity to UserListResponse DTO (list view - lighter)
     */
    private UserListResponse mapToUserListResponse(UserEntity user) {
        List<String> roleNames = user.getRoles().stream()
                .map(role -> role.getRoleEnum().name())
                .collect(Collectors.toList());

        return new UserListResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getDocumentType(),
                user.getDocumentNumber(),
                roleNames,
                user.getIsEnable()
        );
    }
}