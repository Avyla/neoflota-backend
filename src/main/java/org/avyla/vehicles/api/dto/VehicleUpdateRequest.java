package org.avyla.vehicles.api.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record VehicleUpdateRequest(

        // Placa NO se actualiza (es identificador único del vehículo)

        @Positive(message = "ID de marca inválido")
        Long makeId,

        @Size(max = 100, message = "El nombre del modelo no puede exceder 100 caracteres")
        String modelName,

        @Min(value = 1950, message = "El año debe ser mayor o igual a 1950")
        @Max(value = 2099, message = "El año debe ser menor o igual a 2099")
        Integer modelYear,

        @Positive(message = "ID de tipo inválido")
        Long typeId,

        @Positive(message = "ID de categoría inválido")
        Long categoryId,

        @Positive(message = "ID de tipo de combustible inválido")
        Long fuelTypeId,

        @Positive(message = "ID de estado inválido")
        Long statusId,

        @Positive(message = "ID de condición inválido")
        Long conditionId,

        @Size(max = 50, message = "El VIN no puede exceder 50 caracteres")
        String vin,

        @Size(max = 50, message = "El color no puede exceder 50 caracteres")
        String color,

        @Min(value = 0, message = "El odómetro debe ser mayor o igual a 0")
        Integer currentOdometer,

        LocalDate soatExpirationDate,

        LocalDate rtmExpirationDate,

        Boolean active
) {
    // Constructor compacto para normalización
    public VehicleUpdateRequest {
        // Normalizar strings opcionales
        if (vin != null) {
            vin = vin.trim().isEmpty() ? null : vin.trim().toUpperCase();
        }
        if (color != null) {
            color = color.trim().isEmpty() ? null : color.trim();
        }
        if (modelName != null && !modelName.isBlank()) {
            modelName = modelName.trim();
        }
    }
}