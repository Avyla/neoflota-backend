package org.avyla.vehicles.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.avyla.checklists.domain.enums.ConditionOptions;

import java.time.Instant;

/**
 * Catálogo de condición física (Apto, Apto con restricciones y no apto).
 */
@Entity
@Table(name = "vehicle_condition",
        uniqueConstraints = @UniqueConstraint(name = "uk_vehicle_condition_code", columnNames = "code"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "condition_id")
    private Long id;

    @Column(name = "code", nullable = false, length = 20, unique = true)
    @Enumerated(EnumType.STRING)
    private ConditionOptions code; // APTO | APTO_RESTRICCIONES | NO_APTO

    @Column(name = "name", nullable = false, length = 50)
    private String name; // Apto | Apto con restricciones | No apto

    @Column(name = "order_index", nullable = false)
    private int orderIndex = 0;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
