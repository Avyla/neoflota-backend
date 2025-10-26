package org.avyla.vehicles.api.dto;

import lombok.Builder;

import java.time.Instant;
import java.time.LocalDate;

@Builder
public record VehicleDetailResponse(
        Long id,
        String plate,

        // Marca (expandida)
        Long makeId,
        String makeName,

        String modelName,
        Integer modelYear,

        // Tipo (expandido)
        Long typeId,
        String typeName,

        // Categoría (expandida)
        Long categoryId,
        String categoryName,

        // Combustible (expandido)
        Long fuelTypeId,
        String fuelTypeName,

        // Estado operativo (expandido)
        Long statusId,
        String statusCode,
        String statusName,

        // Condición física (expandida, puede ser null)
        Long conditionId,
        String conditionCode,
        String conditionName,

        // Datos opcionales
        String vin,
        String color,
        Integer currentOdometer,
        LocalDate soatExpirationDate,
        LocalDate rtmExpirationDate,

        // Cálculo de días para vencimientos
        Long daysToSoatExpiration,
        Long daysToRtmExpiration,

        // Metadatos
        Boolean active,
        Long createdByUserId,
        Instant createdAt,
        Long updatedByUserId,
        Instant updatedAt
) {}