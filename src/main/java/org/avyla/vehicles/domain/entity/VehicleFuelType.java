package org.avyla.vehicles.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Catálogo de tipos de combustible (Gasolina, Diesel, Eléctrico, Híbrido, etc.).
 */
@Entity
@Table(name = "vehicle_fuel_type",
        uniqueConstraints = @UniqueConstraint(name = "uk_vehicle_fuel_type_name", columnNames = "name"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleFuelType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fuel_type_id")
    private Long id;

    @Column(name = "name", nullable = false, length = 50, unique = true)
    private String name;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
