package org.avyla.vehicles.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
public record VehicleCreateRequest(

        @NotBlank(message = "La placa es obligatoria")
        @Pattern(regexp = "^(?:[A-Z]{3}[0-9]{3}|[A-Z]{3}[0-9]{2}[A-Z])$",
                message = "Formato de placa inválido. Use ABC123 o ABC12D (sin guion)")
        String plate,

        @NotNull(message = "La marca es obligatoria")
        @Positive(message = "ID de marca inválido")
        Long makeId,

        @NotBlank(message = "El nombre del modelo es obligatorio")
        @Size(max = 100, message = "El nombre del modelo no puede exceder 100 caracteres")
        String modelName,

        @Min(value = 1950, message = "El año debe ser mayor o igual a 1950")
        @Max(value = 2099, message = "El año debe ser menor o igual a 2099")
        Integer modelYear,

        @NotNull(message = "El tipo de vehículo es obligatorio")
        @Positive(message = "ID de tipo inválido")
        Long typeId,

        @NotNull(message = "La categoría es obligatoria")
        @Positive(message = "ID de categoría inválido")
        Long categoryId,

        @NotNull(message = "El tipo de combustible es obligatorio")
        @Positive(message = "ID de tipo de combustible inválido")
        Long fuelTypeId,

        @NotNull(message = "El estado es obligatorio")
        @Positive(message = "ID de estado inválido")
        Long statusId,

        // Opcionales
        @Positive(message = "ID de condición inválido")
        Long conditionId,

        @Size(max = 50, message = "El VIN no puede exceder 50 caracteres")
        String vin,

        @Size(max = 50, message = "El color no puede exceder 50 caracteres")
        String color,

        @Min(value = 0, message = "El odómetro debe ser mayor o igual a 0")
        Integer currentOdometer,

        LocalDate soatExpirationDate,

        LocalDate rtmExpirationDate
) {
    // Constructor compacto para normalización
    public VehicleCreateRequest {
        // Normalizar placa a MAYÚSCULAS (aunque el trigger de BD también lo hace)
        if (plate != null) {
            plate = plate.trim().toUpperCase();
        }

        // Normalizar strings opcionales
        if (vin != null) {
            vin = vin.trim().isEmpty() ? null : vin.trim().toUpperCase();
        }
        if (color != null) {
            color = color.trim().isEmpty() ? null : color.trim();
        }
        if (modelName != null) {
            modelName = modelName.trim();
        }
    }
}