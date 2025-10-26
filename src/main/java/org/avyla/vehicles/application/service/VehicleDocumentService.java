package org.avyla.vehicles.application.service;

import lombok.RequiredArgsConstructor;
import org.avyla.common.exceptions.BadRequestException;
import org.avyla.common.exceptions.NotFoundException;
import org.avyla.vehicles.api.dto.VehicleDocumentDtos;
import org.avyla.vehicles.domain.model.Vehicle;
import org.avyla.vehicles.domain.model.VehicleDocument;
import org.avyla.vehicles.domain.repo.VehicleDocumentRepository;
import org.avyla.vehicles.domain.repo.VehicleRepository;
import org.avyla.vehicles.infrastructure.DocumentType;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VehicleDocumentService {

    private static final long MAX_BYTES = 10 * 1024 * 1024; // 10 MB
    private static final Set<String> ALLOWED_MIME = Set.of(MediaType.APPLICATION_PDF_VALUE, "image/jpeg", "image/png");
    // No confiar ciegamente en Content-Type del cliente; se valida longitud, extensión y MIME permitido (allow-list). :contentReference[oaicite:3]{index=3}

    private final VehicleRepository vehicleRepo;
    private final VehicleDocumentRepository docRepo;

    @Transactional
    public VehicleDocumentDtos.UploadResponse upload(Long vehicleId, DocumentType docType, // "SOAT" | "RTM"
                                                     String issuer, LocalDate issuedAt, LocalDate expirationDate, MultipartFile file, Long currentUserId) {
        if (vehicleId == null || docType == null || file == null || file.isEmpty()) {
            throw new BadRequestException("Parámetros obligatorios ausentes");
        }

        if (!docType.equals(DocumentType.SOAT) && !docType.equals(DocumentType.RTM)) {
            throw new BadRequestException("docType inválido: " + docType);
        }
        if (file.getSize() > MAX_BYTES) {
            throw new BadRequestException("Archivo excede el límite (" + MAX_BYTES + " bytes)");
        }
        var ct = file.getContentType();
        if (ct == null || !ALLOWED_MIME.contains(ct)) {
            throw new BadRequestException("MIME no permitido (solo PDF, JPG, PNG)");
        }

        Vehicle veh = vehicleRepo.findByIdForUpdate(vehicleId).orElseThrow(() -> new NotFoundException("Vehículo no encontrado"));

        byte[] data;
        try {
            data = file.getBytes();
        } catch (Exception e) {
            throw new BadRequestException("No se pudo leer el archivo");
        }

        VehicleDocument doc = VehicleDocument.builder().vehicle(veh).docType(docType).issuer(issuer).issuedAt(issuedAt).expirationDate(expirationDate).filename(safeFilename(file.getOriginalFilename())).mimeType(ct).size(file.getSize()).data(data).createdByUserId(currentUserId).createdAt(Instant.now()).build();

        doc = docRepo.save(doc);

        if (docType == DocumentType.SOAT && expirationDate != null) {
            veh.setSoatExpirationDate(expirationDate);
        } else if (docType == DocumentType.RTM && expirationDate != null) {
            veh.setRtmExpirationDate(expirationDate);
        }
        return VehicleDocumentDtos.UploadResponse.builder().id(doc.getDocumentId()).filename(doc.getFilename()).build();
    }

    @Transactional(readOnly = true)
    public List<VehicleDocumentDtos.DocumentMeta> list(Long vehicleId, DocumentType docType) {
        var docs = docRepo.findByVehicle_VehicleIdAndDocTypeOrderByCreatedAtDesc(vehicleId, docType);
        return docs.stream().map(d -> VehicleDocumentDtos.DocumentMeta.builder()
                .id(d.getVehicle().getVehicleId())
                .docType(d.getDocType().name())
                .issuer(d.getIssuer())
                .issuedAt(d.getIssuedAt())
                .expirationDate(d.getExpirationDate())
                .filename(d.getFilename())
                .mimeType(d.getMimeType())
                .size(d.getSize())
                .uploadedAt(d.getCreatedAt())
                .build()
        ).toList();
    }

    @Transactional(readOnly = true)
    public VehicleDocument getOrThrow(UUID docId) {
        return docRepo.findById(docId).orElseThrow(() -> new NotFoundException("Documento no encontrado"));
    }

    private String safeFilename(String original) {
        if (original == null) return "document";
        // permitir solo letras, números, .-_ y limitar longitud
        String base = original.replaceAll("[^A-Za-z0-9._-]", "_");
        return base.length() > 120 ? base.substring(base.length() - 120) : base;
    }
}
