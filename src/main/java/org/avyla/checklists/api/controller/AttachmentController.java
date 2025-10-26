package org.avyla.checklists.api.controller;


import lombok.RequiredArgsConstructor;
import org.avyla.checklists.api.dto.ResponseAttachment;
import org.avyla.checklists.application.service.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AttachmentController {

    @Autowired
    private final AttachmentService service;

    // ===== Subir evidencia por RESPUESTA =====
    @PostMapping(value = "/checklists/responses/{responseId}/attachments",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseAttachment> uploadForResponse(
            @PathVariable Long responseId,
            @RequestParam("file") MultipartFile file
            // @AuthenticationPrincipal ... currentUser → aquí obtienes el userId real
    ) {
        Long currentUserId = 0L; // TODO: reemplaza por el id real del usuario autenticado
        var dto = service.uploadForResponse(responseId, file, currentUserId);
        return ResponseEntity.created(URI.create(dto.getUrl())).body(dto);
    }

    // (Opcional) Subir adjunto por INSTANCIA
    @PostMapping(value = "/checklists/instances/{instanceId}/attachments",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseAttachment> uploadForInstance(
            @PathVariable Long instanceId,
            @RequestParam("file") MultipartFile file) {
        Long currentUserId = 0L; // TODO auth real
        var dto = service.uploadForInstance(instanceId, file, currentUserId);
        return ResponseEntity.created(URI.create(dto.getUrl())).body(dto);
    }

    // Listar por RESPUESTA
    @GetMapping("/checklists/responses/{responseId}/attachments")
    public List<ResponseAttachment> listForResponse(@PathVariable Long responseId) {
        return service.listForResponse(responseId);
    }

    // Descargar
    @GetMapping("/attachments/{id}")
    public ResponseEntity<ByteArrayResource> download(@PathVariable UUID id) {
        var a = service.getAttachmentOr404(id);

        var resource = new ByteArrayResource(a.getData());
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(a.getType()));
        headers.setContentLength(a.getSize());

        // Content-Disposition seguro
        String safeFilename = a.getFilename().replaceAll("[\\r\\n\"]", "_");
        ContentDisposition cd = ContentDisposition.attachment()
                .filename(safeFilename, StandardCharsets.UTF_8)
                .build();
        headers.setContentDisposition(cd);

        // ETag simple con tamaño (puedes usar SHA-256 si lo agregas)
        headers.setETag("\"" + a.getSize() + "\"");

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    // Eliminar
    @DeleteMapping("/attachments/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
