package org.LeetCode.checklists.domain.model;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;

@Entity @Table(name = "checklist_attachment")
@Getter @Setter
public class ChecklistAttachment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attachment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "instance_id")
    private ChecklistInstance instance;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "response_id")
    private ChecklistResponse response;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(length = 200)
    private String caption;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @Column(name = "created_at", nullable = false)
    private java.time.Instant createdAt;
}

