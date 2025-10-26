package org.avyla.vehicles.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Catálogo de tipos de vehículo (Camión estacas, Camioneta, Automóvil, etc.).
 */
@Entity
@Table(name = "vehicle_type",
        uniqueConstraints = @UniqueConstraint(name = "uk_vehicle_type_name", columnNames = "name"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "type_id")
    private Long id;

    @Column(name = "name", nullable = false, length = 120, unique = true)
    private String name;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
