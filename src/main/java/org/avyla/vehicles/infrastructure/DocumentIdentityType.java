package org.avyla.vehicles.infrastructure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DocumentIdentityType {
    CC,
    CE,
    TI,
    PASAPORTE;

    @JsonCreator
    public static DocumentIdentityType from(String value) {
        if (value == null) return null;
        return DocumentIdentityType.valueOf(value.trim().toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}
