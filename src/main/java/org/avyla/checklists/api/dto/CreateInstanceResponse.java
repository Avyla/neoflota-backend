package org.avyla.checklists.api.dto;

import org.avyla.checklists.infrastructure.InstanceStatus;

import java.time.Instant;

public record CreateInstanceResponse(Long instanceId, InstanceStatus status, Instant startedAt, Instant dueAt) {}
