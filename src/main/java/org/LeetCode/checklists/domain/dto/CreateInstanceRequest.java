package org.LeetCode.checklists.domain.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public record CreateInstanceRequest(
        Long vehicleId,
        Long serviceId,
        Long driverId,
        Long maintenanceOrderId,
        Integer odometer // obligatorio si vehicleId != null (regla en servicio)
) {}

