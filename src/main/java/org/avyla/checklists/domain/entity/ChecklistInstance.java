package org.avyla.checklists.domain.entity;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import org.avyla.checklists.domain.enums.InstanceStatus;

import java.math.BigDecimal;
import java.time.Instant;

@Entity @Table(name = "checklist_instance")
@Getter @Setter
public class ChecklistInstance {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "instance_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id", nullable = false)
    private ChecklistVersion version;

    @Column(name = "vehicle_id")
    private Long vehicleId; // FK futura a vehiculo(id)

    @Column(name = "service_id")
    private Long serviceId;

    @Column(name = "driver_id")
    private Long driverId;

    @Column(name = "maintenance_order_id")
    private Long maintenanceOrderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InstanceStatus status = InstanceStatus.PENDING;

    @Column(name = "due_at")
    private Instant dueAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "performed_by_user_id")
    private Long performedByUserId;

    @Column(name = "location_lat", precision = 9, scale = 6)
    private BigDecimal locationLat;

    @Column(name = "location_lon", precision = 9, scale = 6)
    private BigDecimal locationLon;

    private Integer odometer;

    @Column(name = "overall_pass")
    private Boolean overallPass;

    //Atributo no utilizado
    // Mantener por compatibilidad con BD existente
    @Column(name = "condition_general", length = 25)
    private String conditionGeneral; // APTO|APTO_RESTRICCIONES|NO_APTO

    @Column(length = 1000)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}

