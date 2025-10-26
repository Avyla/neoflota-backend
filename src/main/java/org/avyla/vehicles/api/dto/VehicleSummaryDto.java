package org.avyla.vehicles.api.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record VehicleSummaryDto(
        Long id,
        String plate,
        String make,
        String model,
        String statusCode,     // ciclo de vida
        String conditionCode,  // APTO|APTO_RESTRICCIONES|NO_APTO
        LocalDate soatExpirationDate,
        LocalDate rtmExpirationDate,
        Long daysToSoat,
        Long daysToRtm
) {}
