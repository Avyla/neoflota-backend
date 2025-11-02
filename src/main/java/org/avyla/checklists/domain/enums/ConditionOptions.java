package org.avyla.checklists.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ConditionOptions {
    APTO, APTO_RESTRICCIONES, NO_APTO;

    @JsonCreator
    public static ConditionOptions from(String value) {
        if (value == null) return null;
        return ConditionOptions.valueOf(value.trim().toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}
