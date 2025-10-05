package org.LeetCode.common.application.dto;

public record InstanceSummaryResponse(
        Long instanceId, Long vehicleId, Integer odometer,
        String conditionGeneral, boolean overallPass
) {}
