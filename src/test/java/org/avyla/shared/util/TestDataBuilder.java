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

    public static VehicleMake createVehicleMake(Long id, String name, String country) {
        VehicleMake make = new VehicleMake();
        make.setId(id);
        make.setName(name);
        make.setCountry(country);
        return make;
    }

    public static VehicleMake createVehicleMake() {
        return createVehicleMake(1L, "Toyota", "Japón");
    }

    public static VehicleType createVehicleType(Long id, String name, String description) {
        VehicleType type = new VehicleType();
        type.setId(id);
        type.setName(name);
        type.setDescription(description);
        return type;
    }

    public static VehicleType createVehicleType() {
        return createVehicleType(1L, "Camioneta", "Vehículo de carga ligera");
    }

    public static VehicleCategory createVehicleCategory(Long id, String name, String description) {
        VehicleCategory category = new VehicleCategory();
        category.setId(id);
        category.setName(name);
        category.setDescription(description);
        return category;
    }

    public static VehicleCategory createVehicleCategory() {
        return createVehicleCategory(1L, "Liviano", "Vehículo liviano hasta 3.5 toneladas");
    }

    public static VehicleFuelType createVehicleFuelType(Long id, String name) {
        VehicleFuelType fuelType = new VehicleFuelType();
        fuelType.setId(id);
        fuelType.setName(name);
        return fuelType;
    }

    public static VehicleFuelType createVehicleFuelType() {
        return createVehicleFuelType(1L, "Gasolina");
    }

    public static VehicleStatus createVehicleStatus(Long id, String code, String name) {
        VehicleStatus status = new VehicleStatus();
        status.setId(id);
        status.setCode(code);
        status.setName(name);
        return status;
    }

    public static VehicleStatus createVehicleStatus() {
        return createVehicleStatus(1L, "OPERATIVO", "Operativo");
    }

    public static VehicleCondition createVehicleCondition(Long id, ConditionOptions code, String name) {
        VehicleCondition condition = new VehicleCondition();
        condition.setId(id);
        condition.setCode(code);
        condition.setName(name);
        condition.setActive(true);
        condition.setOrderIndex(0);
        condition.setCreatedAt(Instant.now());
        return condition;
    }

    public static VehicleCondition createVehicleCondition() {
        return createVehicleCondition(1L, ConditionOptions.APTO, "Apto");
    }

    public static Vehicle.VehicleBuilder vehicleBuilder() {
        Instant now = Instant.now();
        return Vehicle.builder()
                .vehicleId(1L)
                .plate("ABC123")
                .make(createVehicleMake())
                .modelName("Hilux")
                .modelYear(2023)
                .type(createVehicleType())
                .category(createVehicleCategory())
                .fuelType(createVehicleFuelType())
                .status(createVehicleStatus())
                .condition(createVehicleCondition())
                .vin("1HGBH41JXMN109186")
                .color("Blanco")
                .currentOdometer(50000)
                .soatExpirationDate(LocalDate.now().plusMonths(6))
                .rtmExpirationDate(LocalDate.now().plusMonths(3))
                .active(true)
                .createdByUserId(1L)
                .createdAt(now)
                .updatedByUserId(1L)
                .updatedAt(now);
    }

    public static Vehicle createVehicle() {
        return vehicleBuilder().build();
    }

    public static Vehicle createVehicle(String plate) {
        return vehicleBuilder().plate(plate).build();
    }

    // ==================== SECURITY MODULE ====================

    public static Permission createPermission(Long id, String name) {
        Permission permission = new Permission();
        permission.setId(id);
        permission.setName(name);
        return permission;
    }

    public static Role createRole(Long id, RoleEnum roleEnum, Set<Permission> permissions) {
        Role role = new Role();
        role.setId(id);
        role.setRoleEnum(roleEnum);
        role.setPermissionList(permissions);
        return role;
    }

    public static Role createRole(RoleEnum roleEnum) {
        return createRole(1L, roleEnum, Set.of());
    }

    public static UserEntity.UserEntityBuilder userEntityBuilder() {
        return UserEntity.builder()
                .userId(1L)
                .username("testuser")
                .password("$2a$10$dummyHashedPassword") // BCrypt hash simulado
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .documentType(DocumentIdentityType.CC)
                .documentNumber("123456789")
                .phoneNumber("3001234567")
                .isEnable(true)
                .isAccountNoLocked(true)
                .isAccountNonExpired(true)
                .isCredentialsNonExpired(true)
                .roles(Set.of(createRole(RoleEnum.DRIVER)));
    }

    public static UserEntity createUserEntity() {
        return userEntityBuilder().build();
    }

    public static UserEntity createUserEntity(String username, RoleEnum... roles) {
        Set<Role> roleSet = Set.of();
        if (roles.length > 0) {
            roleSet = Set.of();
            for (int i = 0; i < roles.length; i++) {
                roleSet = Set.of(createRole((long) i + 1, roles[i], Set.of()));
            }
        }
        return userEntityBuilder()
                .username(username)
                .roles(roleSet)
                .build();
    }
}
