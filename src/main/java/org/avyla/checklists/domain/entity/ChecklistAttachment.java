package org.avyla.checklists.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "checklist_attachment",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_attachment_response", columnNames = {"response_id"})
        },
        indexes = {
                @Index(name = "idx_attachment_response", columnList = "response_id"),
                @Index(name = "idx_attachment_instance", columnList = "instance_id"),
                @Index(name = "idx_attachment_created_at", columnList = "created_at")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ChecklistAttachment {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(length = 36, updatable = false, nullable = false)
    private UUID id;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String filename;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String type;  // MIME

    @NotNull
    @Column(nullable = false)
    private Long size;    // bytes

    @JdbcTypeCode(SqlTypes.BINARY)
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "data", nullable = false)
    private byte[] data;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ChecklistResponse response;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instance_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ChecklistInstance instance;

    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // Validación de dominio: exactamente uno de los dos (XOR)
    @AssertTrue(message = "Exactly one of response or instance must be set")
    public boolean isExactlyOneScope() {
        return (response != null) ^ (instance != null);
    }
}
