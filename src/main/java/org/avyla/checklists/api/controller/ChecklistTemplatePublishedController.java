package org.avyla.checklists.api.controller;

import lombok.RequiredArgsConstructor;
import org.avyla.checklists.application.service.ChecklistTemplateQueryService;
import org.avyla.checklists.api.dto.PublishedChecklistResponse;
import org.avyla.common.exceptions.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@RestController
@RequestMapping("/api/checklists/templates")
@RequiredArgsConstructor
public class ChecklistTemplatePublishedController {

    private final ChecklistTemplateQueryService queryService;

    /**
     * GET /checklists/templates/{templateCode}/versions/published
     * Devuelve el diseño "published" para que el frontend lo renderice (secciones, ítems y catálogos).
     * Incluye ETag (versionHash) y Last-Modified (publishedAt) para cache condicional (304).
     */
    @GetMapping("/{templateCode}/versions/published")
    public ResponseEntity<PublishedChecklistResponse> getPublished(
            @PathVariable String templateCode,
            WebRequest webRequest
    ) {
        PublishedChecklistResponse dto = queryService.getPublishedDesign(templateCode);

        String etag = "\"" + dto.versionHash + "\"";
        ZonedDateTime lastMod = dto.publishedAt != null
                ? dto.publishedAt.atZoneSameInstant(ZoneOffset.UTC)
                : ZonedDateTime.now(ZoneOffset.UTC);

        if (webRequest.checkNotModified(etag, lastMod.toInstant().toEpochMilli())) {
            // Spring responde 304 Not Modified automáticamente
            return null;
        }

        return ResponseEntity.ok()
                .eTag(etag)
                .lastModified(lastMod.toInstant().toEpochMilli())
                .body(dto);
    }

    /** Mapea la NotFoundException del service a 404 (por si no hay versión publicada). */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Void> handleNotFound(NotFoundException ex) {
        return ResponseEntity.notFound().build();
    }
}
