package org.avyla.vehicles.infrastructure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DocumentType {
    SOAT,
    RTM;

    @JsonCreator
    public static DocumentType from(String value) {
        if (value == null) return null;
        return DocumentType.valueOf(value.trim().toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}
