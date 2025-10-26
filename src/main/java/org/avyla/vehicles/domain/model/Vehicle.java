package org.avyla.vehicles.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Entidad principal de Vehículos de la flota.
 * Mapea la tabla vehicle y referencia los catálogos (marca, tipo, categoría,
 * combustible, estado operativo y condición).
 */
@Entity
@Table(name = "vehicle",
        uniqueConstraints = @UniqueConstraint(name = "uq_vehicle_plate", columnNames = "plate"))
@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehicle_id")
    private Long vehicleId;

    /** Placa en formato Colombia SIN guion: ABC123 o ABC12D. La BD la normaliza a MAYÚSCULAS. */
    @Column(name = "plate", nullable = false, length = 10)
    private String plate;

    /** Marca (FK: vehicle_make.make_id). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "make_id", nullable = false)
    @ToString.Exclude
    private VehicleMake make;

    /** Nombre de modelo (Hilux, Corolla, F-150, etc.). */
    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;

    /** Año del modelo (CHECK 1950..2099 en la BD). */
    @Column(name = "model_year")
    private Integer modelYear;

    /** Tipo de vehículo (FK: vehicle_type.type_id). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "type_id", nullable = false)
    @ToString.Exclude
    private VehicleType type;

    /** Categoría (FK: vehicle_category.category_id). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    @ToString.Exclude
    private VehicleCategory category;

    /** Tipo de combustible (FK: vehicle_fuel_type.fuel_type_id). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fuel_type_id", nullable = false)
    @ToString.Exclude
    private VehicleFuelType fuelType;

    /** Estado operativo (FK: vehicle_status.status_id). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "status_id", nullable = false)
    @ToString.Exclude
    private VehicleStatus status;

    /** Condición física (FK: vehicle_condition.condition_id). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condition_id")
    @ToString.Exclude
    private VehicleCondition condition;

    /** VIN — Vehicle Identification Number. */
    @Column(name = "vin", length = 50, unique = true)
    private String vin;

    /** Color principal. */
    @Column(name = "color", length = 50)
    private String color;

    /** Odómetro actual (>= 0 en la BD). */
    @Column(name = "current_odometer")
    private Integer currentOdometer;

    /** Fecha de vencimiento del SOAT. */
    @Column(name = "soat_expiration_date")
    private LocalDate soatExpirationDate;

    // Fecha de expriracion tecnomecanica
    @Column(name = "rtm_expiration_date")
    private LocalDate rtmExpirationDate;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_by_user_id")
    private Long updatedByUserId;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Si la aplicación crea registros manualmente (sin confiar en DEFAULT/trigger),
     * estos hooks garantizan valores por defecto. La BD también tiene default/trigger.
     */
    @PrePersist
    void prePersist() {
        final Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
