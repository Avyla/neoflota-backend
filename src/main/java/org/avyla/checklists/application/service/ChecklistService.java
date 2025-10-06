package org.avyla.checklists.application.service;

import lombok.RequiredArgsConstructor;
import org.avyla.checklists.api.dto.CreateInstanceRequest;
import org.avyla.checklists.api.dto.CreateInstanceResponse;
import org.avyla.checklists.api.dto.InstanceSummaryResponse;
import org.avyla.checklists.api.dto.SaveResponsesRequest;
import org.avyla.checklists.domain.model.ChecklistAttachment;
import org.avyla.checklists.domain.model.ChecklistInstance;
import org.avyla.checklists.domain.model.ChecklistItem;
import org.avyla.checklists.domain.model.ChecklistResponse;
import org.avyla.checklists.domain.model.ChecklistResponseOption;
import org.avyla.checklists.domain.model.ChecklistVersion;
import org.avyla.checklists.domain.model.OptionItem;
import org.avyla.checklists.domain.repo.ChecklistAttachmentRepository;
import org.avyla.checklists.domain.repo.ChecklistInstanceRepository;
import org.avyla.checklists.domain.repo.ChecklistItemRepository;
import org.avyla.checklists.domain.repo.ChecklistResponseOptionRepository;
import org.avyla.checklists.domain.repo.ChecklistResponseRepository;
import org.avyla.checklists.domain.repo.ChecklistVersionRepository;
import org.avyla.checklists.domain.repo.OptionItemRepository;
import org.avyla.common.exceptions.BadRequestException;
import org.avyla.common.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChecklistService {

    private final ChecklistVersionRepository versionRepo;
    private final ChecklistItemRepository itemRepo;
    private final ChecklistInstanceRepository instanceRepo;
    private final ChecklistResponseRepository responseRepo;
    private final ChecklistResponseOptionRepository responseOptionRepo;
    private final ChecklistAttachmentRepository attachmentRepo;
    private final OptionItemRepository optionRepo;

    /* =========================
       Crear instancia
       ========================= */
    @Transactional
    public CreateInstanceResponse createInstance(CreateInstanceRequest req) {
        Long versionId = versionRepo
                .findPublishedVersionId("CHK_PREOP_VEH_GEN", "1.1")
                .orElseThrow(() -> new IllegalStateException("Plantilla publicada 1.1 no encontrada"));

        if (req.vehicleId() != null && (req.odometer() == null || req.odometer() < 0)) {
            throw new BadRequestException("El odómetro es obligatorio y debe ser >= 0 cuando hay vehículo.");
        }

        ChecklistVersion versionRef = new ChecklistVersion();
        versionRef.setId(versionId);

        ChecklistInstance inst = new ChecklistInstance();
        inst.setVersion(versionRef);
        inst.setVehicleId(req.vehicleId());
        inst.setServiceId(req.serviceId());
        inst.setDriverId(req.driverId());
        inst.setMaintenanceOrderId(req.maintenanceOrderId());
        inst.setStatus("InProgress");
        inst.setStartedAt(Instant.now());
        inst.setCreatedAt(Instant.now());
        inst.setOdometer(req.odometer());
        inst.setPerformedByUserId(currentUserId());

        instanceRepo.save(inst);
        return new CreateInstanceResponse(inst.getId());
    }

    /* =========================
       Guardar respuestas (batch)
       ========================= */
    @Transactional
    public void saveResponses(long instanceId, SaveResponsesRequest req) {
        ChecklistInstance inst = instanceRepo.findByIdForUpdate(instanceId)
                .orElseThrow(() -> new NotFoundException("ChecklistInstance no existe: " + instanceId));

        if (!"InProgress".equals(inst.getStatus()) && !"Pending".equals(inst.getStatus())) {
            throw new BadRequestException("La instancia no permite edición en estado " + inst.getStatus());
        }

        if (req.odometer() != null) {
            if (inst.getVehicleId() != null && req.odometer() < 0) {
                throw new BadRequestException("El odómetro debe ser >= 0.");
            }
            inst.setOdometer(req.odometer());
        }

        // Ítems de la versión (map por code)
        List<ChecklistItem> items = itemRepo
                .findByVersion_IdOrderBySection_OrderIndexAscOrderIndexAsc(inst.getVersion().getId());
        Map<String, ChecklistItem> itemByCode = items.stream()
                .collect(Collectors.toMap(ChecklistItem::getCode, it -> it));

        // IDs de EstadoGeneral
        Map<String, Long> estadoIds = getEstadoIds();

        for (var r : req.responses()) {
            ChecklistItem item = Optional.ofNullable(itemByCode.get(r.itemCode()))
                    .orElseThrow(() -> new BadRequestException("itemCode inválido: " + r.itemCode()));

            Long estadoId = estadoIds.get(r.estado());
            if (estadoId == null) {
                throw new BadRequestException("Estado inválido en " + r.itemCode());
            }

            if ("NA".equals(r.estado()) && !item.isAllowNa()) {
                throw new BadRequestException("El ítem " + item.getCode() + " no admite N/A.");
            }

            if (!"OK".equals(r.estado())) {
                if (r.comment() == null || r.comment().trim().length() < 5) {
                    throw new BadRequestException("Comentario mínimo (>=5) requerido en " + item.getCode());
                }
            }

            if (item.getDetailOptionGroup() != null && !"OK".equals(r.estado())) {
                if (r.details() == null || r.details().isEmpty()) {
                    throw new BadRequestException("Seleccione al menos un detalle en " + item.getCode());
                }
            }

            // UPSERT respuesta
            ChecklistResponse resp = responseRepo
                    .findOneByInstanceAndItem(inst.getId(), item.getId())
                    .orElseGet(() -> {
                        ChecklistResponse nr = new ChecklistResponse();
                        nr.setInstance(inst);
                        nr.setItem(item);
                        nr.setCreatedAt(Instant.now());
                        nr.setCreatedByUserId(currentUserId());
                        return nr;
                    });

            OptionItem selected = new OptionItem(); // referencia por id
            selected.setId(estadoId);
            resp.setSelectedOption(selected);
            resp.setComment(r.comment());
            responseRepo.save(resp);

            // Detalles: limpiar y reinsertar
            responseOptionRepo.deleteByResponseId(resp.getId());
            if (item.getDetailOptionGroup() != null && r.details() != null) {
                for (String detCode : r.details()) {
                    Long optId = optionRepo
                            .findIdByGroupIdAndCode(item.getDetailOptionGroup().getId(), detCode)
                            .orElseThrow(() -> new BadRequestException(
                                    "Detalle inválido '" + detCode + "' para " + item.getCode()));

                    ChecklistResponseOption ro = new ChecklistResponseOption();
                    ro.setResponse(resp);

                    OptionItem opt = new OptionItem();
                    opt.setId(optId);
                    ro.setOption(opt);

                    responseOptionRepo.save(ro);
                }
            }
        }

        // Evidencias a nivel instancia
        if (req.attachments() != null && !req.attachments().isEmpty()) {
            for (var a : req.attachments()) {
                ChecklistAttachment att = new ChecklistAttachment();
                att.setInstance(inst);
                att.setFileUrl(a.fileUrl());
                att.setMimeType(a.mimeType());
                att.setCaption(a.caption());
                att.setCreatedByUserId(currentUserId());
                att.setCreatedAt(Instant.now());
                attachmentRepo.save(att);
            }
        }
    }

    /* =========================
       Submit (cerrar y calcular)
       ========================= */
    @Transactional
    public InstanceSummaryResponse submit(long instanceId) {
        ChecklistInstance inst = instanceRepo.findByIdForUpdate(instanceId)
                .orElseThrow(() -> new NotFoundException("ChecklistInstance no existe: " + instanceId));

        if (inst.getVehicleId() != null && (inst.getOdometer() == null || inst.getOdometer() < 0)) {
            throw new BadRequestException("Odómetro obligatorio y >= 0 para vehículo.");
        }

        List<ChecklistItem> items = itemRepo
                .findByVersion_IdOrderBySection_OrderIndexAscOrderIndexAsc(inst.getVersion().getId());
        Map<Long, ChecklistItem> itemById = items.stream()
                .collect(Collectors.toMap(ChecklistItem::getId, it -> it));

        List<ChecklistResponse> responses = responseRepo.findByInstance_Id(inst.getId());
        Map<Long, ChecklistResponse> respByItemId = responses.stream()
                .collect(Collectors.toMap(r -> r.getItem().getId(), r -> r));

        // Requeridos respondidos
        for (ChecklistItem it : items) {
            if (it.isRequired() && !respByItemId.containsKey(it.getId())) {
                throw new BadRequestException("Falta responder ítem requerido: " + it.getCode());
            }
            if (respByItemId.containsKey(it.getId())) {
                // Revalidación rápida de NA (defensivo)
                ChecklistResponse r = respByItemId.get(it.getId());
                String st = estadoCodeOf(r, getEstadoCodeById());
                if ("NA".equals(st) && !it.isAllowNa()) {
                    throw new BadRequestException("El ítem " + it.getCode() + " no admite N/A.");
                }
            }
        }

        // Evidencia obligatoria: crítico en NOOP
        Map<Long, String> estadoById = getEstadoCodeById();
        for (ChecklistResponse r : responses) {
            ChecklistItem it = itemById.get(r.getItem().getId());
            String st = estadoCodeOf(r, estadoById);
            if ("Critical".equals(it.getSeverity()) && "NOOP".equals(st)) {
                if (!attachmentRepo.existsByResponse_Id(r.getId())) {
                    throw new BadRequestException("Se requiere evidencia (foto) para " + it.getCode() + " crítico en NOOP.");
                }
            }
        }

        // Calcular condición general
        String condition = computeCondition(items, responses, estadoById);

        inst.setConditionGeneral(condition);
        inst.setOverallPass("APTO".equals(condition));
        inst.setStatus("Submitted");
        inst.setCompletedAt(Instant.now());
        instanceRepo.save(inst);

        return new InstanceSummaryResponse(
                inst.getId(), inst.getVehicleId(), inst.getOdometer(),
                inst.getConditionGeneral(), Boolean.TRUE.equals(inst.getOverallPass())
        );
    }

    @Transactional(readOnly = true)
    public InstanceSummaryResponse getSummary(long id) {
        ChecklistInstance inst = instanceRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("ChecklistInstance no existe: " + id));
        return new InstanceSummaryResponse(
                inst.getId(), inst.getVehicleId(), inst.getOdometer(),
                inst.getConditionGeneral(), Boolean.TRUE.equals(inst.getOverallPass())
        );
    }

  /* =========================
     Helpers internos
     ========================= */

    private Long currentUserId() {
        // TODO: integrar con seguridad/auth
        return 1L;
    }

    /** Devuelve {OK, OBS, NOOP, NA} -> option_id */
    private Map<String, Long> getEstadoIds() {
        return Map.of(
                "OK",   optionRepo.findEstadoGeneralId("OK").orElseThrow(),
                "OBS",  optionRepo.findEstadoGeneralId("OBS").orElseThrow(),
                "NOOP", optionRepo.findEstadoGeneralId("NOOP").orElseThrow(),
                "NA",   optionRepo.findEstadoGeneralId("NA").orElseThrow()
        );
    }

    /** Devuelve {option_id -> "OK"/"OBS"/"NOOP"/"NA"} para evaluar respuestas. */
    private Map<Long, String> getEstadoCodeById() {
        Map<String, Long> ids = getEstadoIds();
        return ids.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    private String estadoCodeOf(ChecklistResponse r, Map<Long, String> estadoById) {
        Long id = r.getSelectedOption().getId();
        String code = estadoById.get(id);
        if (code == null) throw new IllegalStateException("EstadoGeneral desconocido: option_id=" + id);
        return code;
    }

    private String computeCondition(List<ChecklistItem> items,
                                    List<ChecklistResponse> responses,
                                    Map<Long, String> estadoById) {
        Map<Long, ChecklistItem> byId = items.stream()
                .collect(Collectors.toMap(ChecklistItem::getId, it -> it));

        boolean anyCriticalNOOP = false;
        boolean anyRestrictive  = false;

        for (ChecklistResponse r : responses) {
            ChecklistItem it = byId.get(r.getItem().getId());
            String sev = it.getSeverity();               // Low/Medium/High/Critical
            String st  = estadoCodeOf(r, estadoById);    // OK/OBS/NOOP/NA

            if ("Critical".equals(sev) && "NOOP".equals(st)) {
                anyCriticalNOOP = true; break;
            }
            if (("Critical".equals(sev) && "OBS".equals(st)) ||
                    ("High".equals(sev)     && ("OBS".equals(st) || "NOOP".equals(st)))) {
                anyRestrictive = true;
            }
        }

        if (anyCriticalNOOP) return "NO_APTO";
        if (anyRestrictive)  return "APTO_RESTRICCIONES";
        return "APTO";
    }
}
