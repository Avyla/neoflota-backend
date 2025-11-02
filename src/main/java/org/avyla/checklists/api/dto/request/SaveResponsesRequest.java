package org.avyla.checklists.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.avyla.checklists.domain.enums.ResponseState;

import java.util.List;

/**
 * Guardado de progreso y/o asignación tardía de vehículo/odómetro.
 * - vehicleId es OPCIONAL (permite asignar vehículo por primera vez desde responses).
 * - odometer opcional; si se asigna vehicleId por primera vez, odometer es obligatorio (regla de servicio).
 */
public record SaveResponsesRequest(
        Integer odometer, // opcional (si llega, se actualiza)
        @NotNull List<ItemResponse> responses,
        List<InstanceAttachment> attachments, // evidencias a nivel instancia (opcional, legado URL)
        Long vehicleId // <-- NUEVO (opcional)
) {
    public record ItemResponse(
            @NotBlank String itemCode,         // ej: "TAB_INSTRUMENTOS"
            @NotNull ResponseState state,      // OK | OBS | NOOP | NA
            List<String> details,              // catálogos de detalle (códigos)
            String comment
    ) {}

    public record InstanceAttachment(
            @NotBlank String fileUrl,
            String mimeType,
            String caption
    ) {}

    // (Opcional) Clase antigua redundante -> mantener por compatibilidad (no usada)
//    @Data
//    public static class ItemResponseDto{
//        private String itemCode;
//        private String state;
//        private List<String> details;
//        private String comment;
//    }
}
