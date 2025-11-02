package org.avyla.checklists.api.dto.response;

import org.avyla.checklists.domain.enums.InstanceStatus;

import java.time.Instant;

public record CreateInstanceResponse(Long instanceId, InstanceStatus status, Instant startedAt, Instant dueAt) {}
