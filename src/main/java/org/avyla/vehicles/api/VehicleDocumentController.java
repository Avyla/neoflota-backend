package org.avyla.vehicles.api;

import lombok.RequiredArgsConstructor;
import org.avyla.security.application.service.CurrentUserService;
import org.avyla.vehicles.api.dto.response.DocumentMetaResponse;
import org.avyla.vehicles.api.dto.response.DocumentUploadResponse;
import org.avyla.vehicles.application.service.VehicleDocumentService;
import org.avyla.vehicles.domain.entity.VehicleDocument;
import org.avyla.vehicles.domain.enums.DocumentType;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleDocumentController {

    private final VehicleDocumentService service;
    private final CurrentUserService currentUserService;

    @PostMapping(value = "/{id}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DocumentUploadResponse upload(
            @PathVariable("id") Long vehicleId,
            @RequestParam("docType") DocumentType docType, // "SOAT" | "RTM"
            @RequestParam(value = "issuer", required = false) String issuer,
            @RequestParam(value = "issuedAt", required = false) LocalDate issuedAt,
            @RequestParam(value = "expirationDate", required = false) LocalDate expirationDate,
            @RequestPart("file") MultipartFile file
    ) {
        Long currentUserId = currentUserService.getCurrentUserId();
        return service.upload(vehicleId, docType, issuer, issuedAt, expirationDate, file, currentUserId);
    }

    @GetMapping("/{vehicleId}/documents")
    public List<DocumentMetaResponse> list(@PathVariable Long vehicleId,
                                           @RequestParam String docType) {
        var type = DocumentType.valueOf(docType.toUpperCase()); // maneja IllegalArgumentException si aplica
        return service.list(vehicleId, type);
    }

    @GetMapping("/{id}/documents/{docId}")
    public ResponseEntity<ByteArrayResource> download(
            @PathVariable("id") Long vehicleId,
            @PathVariable("docId") UUID docId
    ) {
        VehicleDocument doc = service.getOrThrow(docId);
        if (!doc.getVehicle().getVehicleId().equals(vehicleId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        ByteArrayResource body = new ByteArrayResource(doc.getData());

        String fname = doc.getFilename() != null ? doc.getFilename() : (doc.getDocType()+"-"+doc.getDocumentId());
        String cd = "attachment; filename=\"" + escapeFilename(fname) + "\""; // descarga directa
        // Content-Disposition + Content-Type + Content-Length son headers recomendados para descargas. :contentReference[oaicite:6]{index=6}

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(doc.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, cd)
                .contentLength(doc.getSize() != null ? doc.getSize() : body.contentLength())
                .body(body);
    }

    private String escapeFilename(String name) {
        // Fallback RFC5987 para nombres con espacios/UTF-8 si quieres ser más exhaustivo.
        return URLEncoder.encode(name, StandardCharsets.UTF_8);
    }
}
