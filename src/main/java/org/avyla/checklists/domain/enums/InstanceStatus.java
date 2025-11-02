package org.avyla.checklists.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum InstanceStatus {
    PENDING, IN_PROGRESS, SUBMITTED, APPROVED, REJECTED, EXPIRED;

    @JsonCreator
    public static InstanceStatus from(String value) {
        if (value == null) return null;
        return InstanceStatus.valueOf(value.trim().toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}
