package org.avyla.vehicles.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.avyla.vehicles.infrastructure.DocumentType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "vehicle_document")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VehicleDocument {

    @Id
    @UuidGenerator
    @Column(name = "document_id", columnDefinition = "uuid")
    private UUID documentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Enumerated(EnumType.STRING)
    @Column(name = "doc_type", nullable = false, length = 10)
    private DocumentType docType;

    @Column(name = "issuer", length = 120)
    private String issuer;

    @Column(name = "issued_at")
    private LocalDate issuedAt;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "filename", length = 255)
    private String filename;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "size")
    private Long size;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Basic(fetch = FetchType.LAZY) // evita traer el BLOB en listados
    @Column(name = "data")
    private byte[] data; // Para BLOB, @Lob es la forma estándar en JPA/Hibernate. :contentReference[oaicite:1]{index=1}

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;



}
