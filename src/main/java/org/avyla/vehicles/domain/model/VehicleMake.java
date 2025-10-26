package org.avyla.vehicles.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Catálogo de marcas de vehículos (Toyota, Ford, etc.).
 */
@Entity
@Table(name = "vehicle_make",
        uniqueConstraints = @UniqueConstraint(name = "uk_vehicle_make_name", columnNames = "name"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleMake {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "make_id")
    private Long id;

    @Column(name = "name", nullable = false, length = 100, unique = true)
    private String name;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
