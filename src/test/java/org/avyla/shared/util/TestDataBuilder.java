package org.avyla.shared.util;

import org.avyla.checklists.domain.enums.ConditionOptions;
import org.avyla.security.domain.entity.Permission;
import org.avyla.security.domain.entity.Role;
import org.avyla.security.domain.entity.UserEntity;
import org.avyla.security.domain.enums.RoleEnum;
import org.avyla.vehicles.domain.entity.*;
import org.avyla.vehicles.domain.enums.DocumentIdentityType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

/**
 * Builder de datos de prueba para tests.
 * Centraliza la creación de objetos de dominio con valores predeterminados razonables.
 */
public class TestDataBuilder {

    // ==================== VEHICLE MODULE ====================

    public static VehicleMake createVehicleMake(Long id, String name) {
        return VehicleMake.builder()
                .id(id)
                .name(name)
                .active(true)
                .createdAt(Instant.now())
                .build();
    }

    public static VehicleMake createVehicleMake() {
        return createVehicleMake(1L, "Toyota");
    }

    public static VehicleType createVehicleType(Long id, String name) {

        return VehicleType.builder()
                .id(id)
                .name(name)
                .active(true)
                .createdAt(Instant.now())
                .build();

    }

    public static VehicleType createVehicleType() {
        return createVehicleType(1L, "Camioneta");
    }

    public static VehicleCategory createVehicleCategory(Long id, String name) {
        return VehicleCategory.builder()
                .id(id)
                .name(name)
                .active(true)
                .createdAt(Instant.now())
                .build();
    }

    public static VehicleCategory createVehicleCategory() {
        return createVehicleCategory(1L, "Liviano");
    }

    public static VehicleFuelType createVehicleFuelType(Long id, String name) {
        return VehicleFuelType.builder()
                .id(id)
                .name(name)
                .active(true)
                .createdAt(Instant.now())
                .build();
    }

    public static VehicleFuelType createVehicleFuelType() {
        return createVehicleFuelType(1L, "Gasolina");
    }

    public static VehicleStatus createVehicleStatus(Long id, String code, String name, String description) {
        return VehicleStatus.builder()
                .id(id)
                .code(code)
                .name(name)
                .description(description)
                .active(true)
                .createdAt(Instant.now())
                .build();
    }

    public static VehicleStatus createVehicleStatus() {
        return createVehicleStatus(1L, "ACTIVE", "Activo", "El vehículo está operativo y disponible.");
    }

    public static VehicleCondition createVehicleCondition(Long id, ConditionOptions code, String name) {
        return VehicleCondition.builder()
                .id(id)
                .code(code)
                .name(name)
                .orderIndex(0)
                .active(true)
                .createdAt(Instant.now())
                .build();
    }

    public static VehicleCondition createVehicleCondition() {
        return createVehicleCondition(1L, ConditionOptions.APTO, "Apto");
    }

    public static Vehicle createVehicle(String plate, String modelName,
                                        int modelYear, String vin, String color,
                                        int currentOdometer) {

        return Vehicle.builder()
                .vehicleId(1L)
                .plate(plate) // "ABC123"
                .make(createVehicleMake())
                .modelName(modelName) //Hilux
                .modelYear(modelYear) //2023
                .type(createVehicleType())
                .category(createVehicleCategory())
                .fuelType(createVehicleFuelType())
                .status(createVehicleStatus())
                .condition(createVehicleCondition())
                .vin(vin) // "1HGBH41JXMN109186"
                .color(color) // "Blanco"
                .currentOdometer(currentOdometer) // 50000
                .soatExpirationDate(LocalDate.now().plusMonths(6))
                .rtmExpirationDate(LocalDate.now().plusMonths(3))
                .active(true)
                .createdByUserId(1L)
                .createdAt(Instant.now())
                .updatedByUserId(1L)
                .updatedAt(Instant.now())
                .build();
    }

    public static Vehicle createVehicle() {
        return createVehicle("ABC123", "Hilux", 2023, "1HGBH41JXMN109186", "Blanco", 50000);
    }

    public static Vehicle createVehicle(String plate) {
        return createVehicle(plate, "Hilux", 2023, "1HGBH41JXMN109186", "Blanco", 50000);
    }

    // ==================== SECURITY MODULE ====================

    public static Permission createPermission(Long id, String name) {
        return Permission.builder()
                .id(id)
                .name(name)
                .build();
    }
    public static Permission createPermission() {
        return createPermission(1L, "READ");
    }

    public static Role createRole(Long id, RoleEnum roleEnum, Set<Permission> permissions) {
        return Role.builder()
                .id(id)
                .roleEnum(roleEnum)
                .permissionList(permissions)
                .build();
    }

    public static Role createRole() {
        Set<Permission> permissions = Set.of(createPermission());
        return createRole(1L, RoleEnum.ADMIN, permissions);
    }

    public static UserEntity createUserEntity(Long userId, String username, String passowrd, String email,
                                               String firstName, String lastName, DocumentIdentityType documentType,
                                               String documentNumber, String phoneNumber, Set<Role> roles) {
        return UserEntity.builder()
                .userId(userId) // 1L
                .username(username) // "testuser"
                .password(passowrd) // "$2a$10$dummyHashedPassword"
                .email(email) // "test@example.com"
                .firstName(firstName)  // "Test"
                .lastName(lastName) // "User"
                .documentType(documentType) // DocumentIdentityType.CC
                .documentNumber(documentNumber) // "123456789"
                .phoneNumber(phoneNumber) // "3001234567"
                .isEnable(true)
                .isAccountNoLocked(true)
                .isAccountNonExpired(true)
                .isCredentialsNonExpired(true)
                .roles(roles)
                .build();
    }

    public static UserEntity createUserEntity() {
        return createUserEntity(1L, "testuser", "$2a$10$dummyHashedPassword", "text@example.com",
                "Test", "User", DocumentIdentityType.CC, "123456789", "3001234567", Set.of(createRole()));
    }

}
