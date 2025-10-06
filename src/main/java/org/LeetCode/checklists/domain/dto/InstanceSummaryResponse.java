package org.LeetCode.checklists.domain.dto;

public record InstanceSummaryResponse(
        Long instanceId, Long vehicleId, Integer odometer,
        String conditionGeneral, boolean overallPass
) {}
