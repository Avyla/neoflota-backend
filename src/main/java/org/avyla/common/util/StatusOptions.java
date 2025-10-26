package org.avyla.common.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum StatusOptions {
    ACTIVE, IN_REPAIR, INACTIVE, SOLD;

    @JsonCreator
    public static StatusOptions from(String value) {
        if (value == null) return null;
        return StatusOptions.valueOf(value.trim().toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}
