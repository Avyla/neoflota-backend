package org.avyla.vehicles.api.dto.response;

import lombok.Builder;

import java.time.Instant;
import java.time.LocalDate;

@Builder
public record DocumentMetaResponse(
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
