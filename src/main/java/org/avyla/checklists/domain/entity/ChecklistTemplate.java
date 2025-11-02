package org.avyla.checklists.domain.entity;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import java.time.Instant;

@Entity @Table(name = "checklist_template")
@Getter @Setter
public class ChecklistTemplate {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;  // ej. CHK_PREOP_VEH_GEN

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "entity_target", nullable = false, length = 24)
    private String entityTarget; // 'Vehicle'|'Service'|'Driver'|'MaintenanceOrder'

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}

