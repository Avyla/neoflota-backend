package org.avyla.checklists.api.dto;

public record InstanceSummaryResponse(
        Long instanceId, Long vehicleId, Integer odometer,
        String conditionGeneral, boolean overallPass
) {}
