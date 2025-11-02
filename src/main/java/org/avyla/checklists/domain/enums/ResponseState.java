package org.avyla.checklists.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Estados permitidos para respuestas de checklist.
 * Mantiene mayúsculas para compatibilidad JSON (OK/OBS/NOOP/NA).
 */
public enum ResponseState {
    OK, OBS, NOOP, NA;

    @JsonCreator
    public static ResponseState from(String value) {
        if (value == null) return null;
        return ResponseState.valueOf(value.trim().toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}

