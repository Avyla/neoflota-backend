package org.avyla.checklists.domain.entity;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import java.time.Instant;

@Entity @Table(name = "checklist_version",
        uniqueConstraints = @UniqueConstraint(columnNames = {"template_id","version_label"}))
@Getter @Setter
public class ChecklistVersion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "version_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "template_id", nullable = false)
    private ChecklistTemplate template;

    @Column(name = "version_label", nullable = false, length = 20)
    private String versionLabel; // ej. "1.1"

    @Column(nullable = false, length = 20)
    private String status; // 'Draft'|'Published'|'Archived'

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;
}

