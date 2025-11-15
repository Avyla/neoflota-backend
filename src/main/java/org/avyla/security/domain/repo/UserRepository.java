package org.avyla.security.domain.repo;

import org.avyla.security.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    // ========== BÚSQUEDAS BÁSICAS ==========

    /**
     * Buscar usuario por username (incluye eliminados)
     */
    Optional<UserEntity> findUserEntitiesByUsername(String username);

    /**
     * Buscar usuario por username excluyendo eliminados
     */
    @Query("SELECT u FROM UserEntity u WHERE u.username = :username AND u.deletedAt IS NULL")
    Optional<UserEntity> findByUsernameAndNotDeleted(@Param("username") String username);

    /**
     * Buscar usuario por email excluyendo eliminados
     */
    @Query("SELECT u FROM UserEntity u WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<UserEntity> findByEmailAndNotDeleted(@Param("email") String email);

    /**
     * Buscar usuario por ID excluyendo eliminados
     */
    @Query("SELECT u FROM UserEntity u WHERE u.userId = :userId AND u.deletedAt IS NULL")
    Optional<UserEntity> findByIdAndNotDeleted(@Param("userId") Long userId);

    /**
     * Obtener ID de usuario por username
     */
    @Query("SELECT u.userId FROM UserEntity u WHERE u.username = :username")
    Optional<Long> findUserIdByUsername(@Param("username") String username);

    // ========== LISTADOS ==========

    /**
     * Listar todos los usuarios NO eliminados
     */
    @Query("SELECT u FROM UserEntity u WHERE u.deletedAt IS NULL ORDER BY u.username")
    List<UserEntity> findAllNotDeleted();

    /**
     * Listar usuarios activos (no eliminados y habilitados)
     */
    @Query("SELECT u FROM UserEntity u WHERE u.deletedAt IS NULL AND u.isEnable = TRUE ORDER BY u.username")
    List<UserEntity> findAllActive();

    // ========== BÚSQUEDAS AVANZADAS ==========

    /**
     * Buscar usuarios por query (username, email, nombre, apellido)
     * Excluye eliminados
     */
    @Query("SELECT u FROM UserEntity u WHERE u.deletedAt IS NULL " +
            "AND (LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "ORDER BY u.username")
    List<UserEntity> searchUsers(@Param("query") String query);

    /**
     * Buscar usuarios por rol
     */
    @Query("SELECT u FROM UserEntity u JOIN u.roles r " +
            "WHERE r.roleEnum = :roleName AND u.deletedAt IS NULL " +
            "ORDER BY u.username")
    List<UserEntity> findByRole(@Param("roleName") String roleName);

    // ========== VALIDACIONES ==========

    /**
     * Verificar si existe username (excluyendo usuario actual y eliminados)
     */
    @Query("SELECT COUNT(u) > 0 FROM UserEntity u " +
            "WHERE LOWER(u.username) = LOWER(:username) " +
            "AND u.userId != :userId " +
            "AND u.deletedAt IS NULL")
    boolean existsByUsernameAndNotSelf(@Param("username") String username, @Param("userId") Long userId);

    /**
     * Verificar si existe email (excluyendo usuario actual y eliminados)
     */
    @Query("SELECT COUNT(u) > 0 FROM UserEntity u " +
            "WHERE LOWER(u.email) = LOWER(:email) " +
            "AND u.userId != :userId " +
            "AND u.deletedAt IS NULL")
    boolean existsByEmailAndNotSelf(@Param("email") String email, @Param("userId") Long userId);

    /**
     * Verificar si existe username (sin excluir ningún usuario)
     */
    @Query("SELECT COUNT(u) > 0 FROM UserEntity u " +
            "WHERE LOWER(u.username) = LOWER(:username) " +
            "AND u.deletedAt IS NULL")
    boolean existsByUsername(@Param("username") String username);

    /**
     * Verificar si existe email (sin excluir ningún usuario)
     */
    @Query("SELECT COUNT(u) > 0 FROM UserEntity u " +
            "WHERE LOWER(u.email) = LOWER(:email) " +
            "AND u.deletedAt IS NULL")
    boolean existsByEmail(@Param("email") String email);

    // ========== VALIDACIONES DOCUMENTO (NUEVO - FIX DUPLICACIÓN) ==========

    /**
     * Verificar si existe document number (sin excluir ningún usuario)
     * ✅ NUEVO - Previene duplicación de cédulas al crear usuarios
     */
    @Query("SELECT COUNT(u) > 0 FROM UserEntity u " +
            "WHERE u.documentNumber = :documentNumber " +
            "AND u.deletedAt IS NULL")
    boolean existsByDocumentNumber(@Param("documentNumber") String documentNumber);

    /**
     * Verificar si existe document number (excluyendo usuario actual y eliminados)
     * ✅ NUEVO - Previene duplicación de cédulas al actualizar usuarios
     */
    @Query("SELECT COUNT(u) > 0 FROM UserEntity u " +
            "WHERE u.documentNumber = :documentNumber " +
            "AND u.userId != :userId " +
            "AND u.deletedAt IS NULL")
    boolean existsByDocumentNumberAndNotSelf(@Param("documentNumber") String documentNumber, @Param("userId") Long userId);
}