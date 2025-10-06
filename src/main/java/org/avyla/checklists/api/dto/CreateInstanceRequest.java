package org.avyla.checklists.api.dto;

public record CreateInstanceRequest(
        Long vehicleId,
        Long serviceId,
        Long driverId,
        Long maintenanceOrderId,
        Integer odometer // obligatorio si vehicleId != null (regla en servicio)
) {}

