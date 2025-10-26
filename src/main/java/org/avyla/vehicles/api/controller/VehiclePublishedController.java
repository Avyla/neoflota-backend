package org.avyla.vehicles.api.controller;

import lombok.RequiredArgsConstructor;
import org.avyla.checklists.application.service.VehiclePublishedService;
import org.avyla.vehicles.api.dto.VehiclePublishedMetadata;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import java.time.Duration;
import java.time.Instant;

/**
 * Controlador para publicar metadata del módulo de vehículos al frontend.
 * Similar al concepto de ChecklistTemplatePublishedController.
 *
 * Endpoint:
 *   GET /api/vehicles/published - Catálogos + validaciones + estructura
 */
@RestController
@RequestMapping("/api/vehicles/published")
@RequiredArgsConstructor
public class VehiclePublishedController {

    private final VehiclePublishedService publishedService;

    /**
     * GET /api/vehicles/published
     * Devuelve toda la metadata necesaria para que el frontend construya formularios:
     *   - Catálogos activos (marcas, tipos, categorías, combustibles, estados, condiciones)
     *   - Reglas de validación (placa, año modelo, odómetro)
     *   - Campos obligatorios
     *
     * Incluye caché HTTP con ETag y Cache-Control para optimizar requests.
     */
    @GetMapping
    public ResponseEntity<VehiclePublishedMetadata> getPublishedMetadata(WebRequest webRequest) {
        VehiclePublishedMetadata metadata = publishedService.getPublishedMetadata();

        // ETag basado en versión (hash de catálogos)
        String etag = "\"" + metadata.version() + "\"";

        // Last-Modified (momento actual ya que catálogos pueden cambiar)
        long lastModified = Instant.now().toEpochMilli();

        // Verificar si el cliente tiene versión actualizada (304 Not Modified)
        if (webRequest.checkNotModified(etag, lastModified)) {
            return null; // Spring devuelve 304 automáticamente
        }

        // Cache-Control: cachear por 1 hora (los catálogos no cambian frecuentemente)
        return ResponseEntity.ok()
                .eTag(etag)
                .lastModified(lastModified)
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)))
                .body(metadata);
    }
}