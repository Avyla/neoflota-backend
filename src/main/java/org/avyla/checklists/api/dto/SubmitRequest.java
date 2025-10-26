package org.avyla.checklists.api.dto;

import jakarta.validation.constraints.NotNull;
import org.avyla.common.util.ConditionOptions;

public record SubmitRequest(
        @NotNull ConditionOptions conditionGeneral // "APTO" | "APTO_RESTRICCIONES" | "NO_APTO"
) {}
