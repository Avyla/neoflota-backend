package org.avyla.checklists.infrastructure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SeverityOptions {
    LOW, MEDIUM, HIGH, CRITICAL;

    @JsonCreator
    public static SeverityOptions from(String value) {
        if (value == null) return null;
        return SeverityOptions.valueOf(value.trim().toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}
