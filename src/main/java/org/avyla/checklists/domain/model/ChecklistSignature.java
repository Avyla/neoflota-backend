package org.avyla.checklists.domain.model;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import java.time.Instant;

@Entity @Table(name = "checklist_signature")
@Getter @Setter
public class ChecklistSignature {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "signature_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "instance_id", nullable = false)
    private ChecklistInstance instance;

    @Column(nullable = false, length = 20)
    private String role; // Driver|Inspector|Supervisor

    @Column(name = "signed_by_user_id", nullable = false)
    private Long signedByUserId;

    @Column(name = "signed_at", nullable = false)
    private Instant signedAt;

    @Column(name = "signature_image_url", length = 500)
    private String signatureImageUrl;
}
