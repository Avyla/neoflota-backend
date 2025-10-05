package org.LeetCode.checklists.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record SaveResponsesRequest(
        Integer odometer, // opcional (si llega, se actualiza)
        @NotNull List<ItemResponse> responses,
        List<InstanceAttachment> attachments // evidencias a nivel instancia (opcional)
) {
    public record ItemResponse(
            @NotBlank String itemCode,         // ej: "TAB_INSTRUMENTOS"
            @Pattern(regexp="OK|OBS|NOOP|NA") String estado,
            List<String> details,              // ej: ["COMBUSTIBLE"] si aplica
            String comment                     // requerido si estado != OK (regla en servicio)
    ) {}
    public record InstanceAttachment(
            @NotBlank String fileUrl,
            String mimeType,
            String caption
    ) {}
}
