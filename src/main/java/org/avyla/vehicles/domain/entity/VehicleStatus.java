package org.avyla.vehicles.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Catálogo de estados operativos del vehículo (Activo, En mantenimiento, etc.).
 */
@Entity
@Table(name = "vehicle_status",
        uniqueConstraints = @UniqueConstraint(name = "uk_vehicle_status_code", columnNames = "code"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "status_id")
    private Long id;

    @Column(name = "code", nullable = false, length = 50, unique = true)
    private String code; // ACTIVE | IN_REPAIR | INACTIVE | SOLD

    @Column(name = "name", nullable = false, length = 100)
    private String name; // Activo | En mantenimiento | Inactivo | Vendido

    @Column(name = "description", length = 300)
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
