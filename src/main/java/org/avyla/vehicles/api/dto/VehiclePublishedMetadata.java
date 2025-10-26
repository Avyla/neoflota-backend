package org.avyla.vehicles.api.dto;

import lombok.Builder;

import java.util.List;

/**
 * Metadata publicada para el frontend sobre el módulo de vehículos.
 * Similar al concepto de PublishedChecklistResponse, proporciona todos los
 * catálogos y reglas de validación necesarias para construir formularios.
 */
@Builder
public record VehiclePublishedMetadata(
        CatalogsDTO catalogs,
        ValidationRulesDTO validationRules,
        String version
) {

    @Builder
    public record CatalogsDTO(
            List<CatalogItemDTO> makes,
            List<CatalogItemDTO> types,
            List<CatalogItemDTO> categories,
            List<CatalogItemDTO> fuelTypes,
            List<StatusItemDTO> statuses,
            List<ConditionItemDTO> conditions
    ) {}

    @Builder
    public record CatalogItemDTO(
            Long id,
            String name
    ) {}

    @Builder
    public record StatusItemDTO(
            Long id,
            String code,
            String name,
            String description
    ) {}

    @Builder
    public record ConditionItemDTO(
            Long id,
            String code,
            String name,
            Integer order
    ) {}

    @Builder
    public record ValidationRulesDTO(
            PlateValidationDTO plate,
            ModelYearValidationDTO modelYear,
            OdometerValidationDTO odometer,
            List<String> requiredFields
    ) {}

    @Builder
    public record PlateValidationDTO(
            String pattern,
            String format,
            String description,
            List<String> examples
    ) {}

    @Builder
    public record ModelYearValidationDTO(
            Integer min,
            Integer max
    ) {}

    @Builder
    public record OdometerValidationDTO(
            Integer min,
            String unit
    ) {}
}