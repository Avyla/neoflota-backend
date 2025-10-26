package org.avyla.vehicles.api.dto;

import lombok.Builder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class VehicleDocumentDtos {
    @Builder
    public record DocumentMeta(
            Long id,
            String docType,
            String number,
            String issuer,
            LocalDate issuedAt,
            LocalDate expirationDate,
            String filename,
            String mimeType,
            Long size,
            Instant uploadedAt
    ) {}

    @Builder
    public record UploadResponse(UUID id, String filename) {}
}
