package org.avyla.checklists.domain.model;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import java.time.Instant;

@Entity @Table(name = "checklist_response",
        uniqueConstraints = @UniqueConstraint(columnNames = {"instance_id","item_id"}))
@Getter @Setter
public class ChecklistResponse {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "response_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "instance_id", nullable = false)
    private ChecklistInstance instance;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "item_id", nullable = false)
    private ChecklistItem item;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "selected_option_id", nullable = false)
    private OptionItem selectedOption; // 'EstadoGeneral' option

    @Column(length = 1000)
    private String comment;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}

