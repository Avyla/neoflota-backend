package org.avyla.checklists.application.service;

import lombok.RequiredArgsConstructor;
import org.avyla.checklists.api.dto.InstanceDetailsResponse;
import org.avyla.checklists.api.dto.PendingPayloadResponse;
import org.avyla.checklists.api.dto.SaveResponsesRequest;
import org.avyla.checklists.api.dto.SubmitRequest;
import org.avyla.checklists.config.ChecklistProperties;
import org.avyla.checklists.domain.model.*;
import org.avyla.checklists.domain.repo.*;
import org.avyla.checklists.infrastructure.InstanceStatus;
import org.avyla.checklists.infrastructure.ResponseState;
import org.avyla.checklists.infrastructure.SeverityOptions;
import org.avyla.common.exceptions.BadRequestException;
import org.avyla.common.exceptions.NotFoundException;
import org.avyla.vehicles.domain.repo.VehicleConditionRepository;
import org.avyla.vehicles.domain.repo.VehicleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio alineado a:
 * - status String en ChecklistInstance
 * - enums en lógica (ResponseState / SeverityOptions / InstanceStatus) con mapping central
 * - BLOB evidencias por respuesta
 */
@Service
@RequiredArgsConstructor
public class ChecklistService {

    private final ChecklistProperties props;

    private final ChecklistVersionRepository versionRepo;
    private final ChecklistItemRepository itemRepo;

    private final ChecklistInstanceRepository instanceRepo;
    private final ChecklistResponseRepository responseRepo;
    private final ChecklistAttachmentRepository attachmentRepo;
    private final OptionItemRepository optionRepo;

    private final VehicleRepository vehicleRepo; // repo para current_odometer
    private final VehicleConditionRepository vehicleConditionRepo;

    /* =========================
       Crear instancia (TTL + cooldown opcional)
       ========================= */
    @Transactional
    public ChecklistInstance createInstance(String templateCode, Long driverId) {
        if (!props.getGeneration().isEnabled()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La generación de instancias está deshabilitada");
        }
        if (templateCode == null || templateCode.isBlank() || driverId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "templateCode y driverId son obligatorios");
        }

        // cooldown (opcional)
        var lastExpiredOpt = instanceRepo.findLastExpiredByDriver(driverId);
        if (lastExpiredOpt.isPresent()) {
            var lastExpired = lastExpiredOpt.get();
            var base = Optional.ofNullable(lastExpired.getCompletedAt()).orElse(lastExpired.getDueAt());
            var mins = props.getInstance().getCooldownMinutes();
            if (base != null && Instant.now().isBefore(base.plus(mins, ChronoUnit.MINUTES))) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                        "Debes esperar " + mins + " minutos para iniciar otra preoperacional");
            }
        }

        // ya existe una abierta sin expirar
        var open = instanceRepo.findOpenNotExpiredByDriver(
                driverId,
                List.of(InstanceStatus.PENDING.name(), InstanceStatus.IN_PROGRESS.name()),
                Instant.now());
        if (!open.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya tienes una instancia abierta sin expirar");
        }

        // versión publicada por templateCode
        var version = versionRepo
                .findLatestPublishedByTemplateCode(templateCode)
                .orElseThrow(() -> new NotFoundException("No hay versión publicada para la plantilla: " + templateCode));

        var now = Instant.now();
        var due = now.plus(props.getInstance().getTtlMinutes(), ChronoUnit.MINUTES);

        var inst = new ChecklistInstance();
        inst.setVersion(version);
        inst.setDriverId(driverId);
        inst.setStatus(InstanceStatus.IN_PROGRESS); // String en entidad
        inst.setStartedAt(now);
        inst.setDueAt(due);
        inst.setCreatedAt(now);
        inst.setPerformedByUserId(currentUserId());

        return instanceRepo.save(inst);
    }

    /* =========================
       Guardar responses + vehículo/odómetro
       ========================= */
    @Transactional
    public void saveResponses(Long instanceId, SaveResponsesRequest req) {
        var inst = instanceRepo.findByIdForUpdate(instanceId)
                .orElseThrow(() -> new NotFoundException("Instancia no encontrada"));

        ensureNotExpired(inst);

        // Asignar vehículo por primera vez (si llega en request)
        if (req.vehicleId() != null) {
            if (inst.getVehicleId() != null && !Objects.equals(inst.getVehicleId(), req.vehicleId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "La instancia ya tiene un vehículo asignado");
            }
            if (inst.getVehicleId() == null) {
                if (req.odometer() == null) {
                    throw new BadRequestException("Se requiere odómetro al asignar el vehículo");
                }
                var veh = vehicleRepo.findById(req.vehicleId())
                        .orElseThrow(() -> new BadRequestException("Vehículo inválido"));
                int last = Optional.ofNullable(veh.getCurrentOdometer()).orElse(0);
                if (req.odometer() < last) {
                    throw new BadRequestException("El odómetro debe ser mayor o igual a " + last);
                }
                inst.setVehicleId(req.vehicleId());
                inst.setOdometer(req.odometer());
            }
        }

        // Actualizar odómetro (sin reasignar vehículo)
        if (req.odometer() != null) {
            if (inst.getVehicleId() == null) {
                throw new BadRequestException("No puedes reportar odómetro sin un vehículo asignado");
            }
            var veh = vehicleRepo.findById(inst.getVehicleId())
                    .orElseThrow(() -> new BadRequestException("Vehículo inválido"));
            int last = Optional.ofNullable(veh.getCurrentOdometer()).orElse(0);
            if (req.odometer() < last) {
                throw new BadRequestException("El odómetro debe ser mayor o igual a " + last);
            }
            inst.setOdometer(req.odometer());
        }

        // Ítems de la versión (map por code)
        var items = itemRepo.findByVersion_IdOrderBySection_OrderIndexAscOrderIndexAsc(inst.getVersion().getId());
        var itemByCode = items.stream().collect(Collectors.toMap(ChecklistItem::getCode, it -> it));

        // Mapa enum -> option_id (OK/OBS/NOOP/NA)
        var estadoIds = getEstadoIdsByEnum();

        for (var r : req.responses()) {
            var item = Optional.ofNullable(itemByCode.get(r.itemCode()))
                    .orElseThrow(() -> new BadRequestException("itemCode inválido: " + r.itemCode()));

            ResponseState state = r.state();
            Long estadoId = estadoIds.get(state);
            if (estadoId == null) {
                throw new BadRequestException("Estado inválido en " + r.itemCode());
            }

            if (state == ResponseState.NA && !item.isAllowNa()) {
                throw new BadRequestException("El ítem " + item.getCode() + " no admite N/A.");
            }

            boolean requiresComment = (state == ResponseState.OBS || state == ResponseState.NOOP);
            if (requiresComment && (r.comment() == null || r.comment().trim().length() < 5)) {
                throw new BadRequestException("Comentario mínimo (>=5) requerido en " + item.getCode());
            }

            boolean requiresDetails = item.getDetailOptionGroup() != null
                    && (state == ResponseState.OBS || state == ResponseState.NOOP);
            if (requiresDetails && (r.details() == null || r.details().isEmpty())) {
                throw new BadRequestException("Seleccione al menos un detalle en " + item.getCode());
            }

            // UPSERT de la respuesta (relación por instancia+ítem)
            var resp = responseRepo.findOneByInstanceAndItem(inst.getId(), item.getId())
                    .orElseGet(() -> {
                        var nr = new ChecklistResponse();
                        nr.setInstance(inst);
                        nr.setItem(item);
                        nr.setCreatedAt(Instant.now());
                        nr.setCreatedByUserId(currentUserId());
                        return nr;
                    });

            var selected = new OptionItem();
            selected.setId(estadoId);
            resp.setSelectedOption(selected);
            resp.setComment(r.comment());
            responseRepo.save(resp);


            // Construir la lista de OptionItem "ligeros" (solo id) para los detalles
            List<OptionItem> newOptions = java.util.Collections.emptyList();
            if (item.getDetailOptionGroup() != null && r.details() != null && !r.details().isEmpty()) {
                newOptions = new java.util.ArrayList<>(r.details().size());
                for (String detCode : r.details()) {
                    Long optId = optionRepo
                            .findIdByGroupIdAndCode(item.getDetailOptionGroup().getId(), detCode)
                            .orElseThrow(() -> new BadRequestException(
                                    "Detalle inválido '" + detCode + "' para " + item.getCode()));
                    OptionItem oi = new OptionItem();
                    oi.setId(optId);
                    newOptions.add(oi);
                }
            }

            // Reemplazar detalles con la relación (borra los previos y añade los nuevos)
            resp.replaceOptions(newOptions);

            // Persistir cambios de la respuesta y sus hijos (cascade = ALL)
            responseRepo.save(resp);

        }

        instanceRepo.save(inst);
    }

    /* =========================
       Payload para retomar (pendiente)
       ========================= */
    @Transactional(readOnly = true)
    public PendingPayloadResponse getPendingPayload(Long driverId) {
        var open = instanceRepo.findOpenNotExpiredByDriver(
                driverId,
                List.of(InstanceStatus.PENDING.name(), InstanceStatus.IN_PROGRESS.name()),
                Instant.now());
        if (open.isEmpty()) return null;

        var inst = open.get(0);
        long remaining = (inst.getDueAt() == null) ? 0
                : Math.max(0, ChronoUnit.SECONDS.between(Instant.now(), inst.getDueAt()));

        // Respuestas parciales
        var estadoById = getEstadoById();
        var partial = responseRepo.findGraphByInstanceId(inst.getId()).stream()
                .map(r -> PendingPayloadResponse.ItemSnapshotDto.builder()
                        .itemCode(r.getItem().getCode())
                        .state(estadoOf(r, estadoById).name()) // enum->String
                        .details(
                                r.getOptions().stream()
                                        .map(o -> o.getOption().getCode())
                                        .toList()
                        )
                        .comment(r.getComment())
                        .attachments(attachmentsLite(r.getId()))
                        .build())
                .toList();

        // Template code si lo necesitas: inst.getVersion().getTemplate().getCode()
        String templateCode = (inst.getVersion() != null && inst.getVersion().getTemplate() != null)
                ? inst.getVersion().getTemplate().getCode() : null;

        return PendingPayloadResponse.builder()
                .instanceId(inst.getId())
                .status(inst.getStatus())     // String
                .startedAt(inst.getStartedAt())
                .dueAt(inst.getDueAt())
                .timeRemainingSec(remaining)
                .templateCode(templateCode)
                .partialResponses(partial)
                .build();
    }

    /* =========================
       Detalles completos
       ========================= */
    @Transactional(readOnly = true)
    public InstanceDetailsResponse getInstanceDetails(Long instanceId) {
        var inst = instanceRepo.findById(instanceId)
                .orElseThrow(() -> new NotFoundException("Instancia no encontrada"));

        var estadoById = getEstadoById();
        var items = responseRepo.findGraphByInstanceId(instanceId).stream()
                .map(r -> InstanceDetailsResponse.ItemDetailDto.builder()
                        .itemCode(r.getItem().getCode())
                        .state(estadoOf(r, estadoById).name())
                        .details(r.getOptions().stream().map(o -> o.getOption().getCode()).toList())
                        .comment(r.getComment())
                        .attachments(attachmentsDetails(r.getId()))
                        .build())
                .toList();

        long total = items.size();
        long ok = items.stream().filter(i -> "OK".equals(i.getState())).count();
        long obs = items.stream().filter(i -> "OBS".equals(i.getState())).count();
        long noop = items.stream().filter(i -> "NOOP".equals(i.getState())).count();

        // Si tienes severidad por ítem, cuenta críticos NOOP
        long criticalNoop = countCriticalNoop(instanceId);

        String overall = computeOverall(ok, obs, noop);

        return InstanceDetailsResponse.builder()
                .instanceId(inst.getId())
                .status(inst.getStatus()) // String
                .driverId(inst.getDriverId())
                .vehicleId(inst.getVehicleId())
                .odometer(inst.getOdometer())
                .templateCode(inst.getVersion() != null && inst.getVersion().getTemplate() != null
                        ? inst.getVersion().getTemplate().getCode() : null)
                .startedAt(inst.getStartedAt())
                .completedAt(inst.getCompletedAt())
                .responses(items)
                .summary(InstanceDetailsResponse.Summary.builder()
                        .total(total)
                        .okCount(ok)
                        .oobCount(obs)
                        .noopCount(noop)
                        .criticalNoopCount(criticalNoop)
                        .overall(overall)
                        .build())
                .build();
    }

    /* =========================
       Submit (cerrar + evidencias + actualizar odómetro)
       ========================= */
    @Transactional
    public void submit(Long instanceId, SubmitRequest req) {
        var inst = instanceRepo.findByIdForUpdate(instanceId)
                .orElseThrow(() -> new NotFoundException("Instancia no encontrada"));

        ensureNotExpired(inst);
        enforceEvidenceRules(inst);

        String calculatedCondition = calculateConditionGeneral(inst);
        inst.setConditionGeneral(calculatedCondition);

        // 3) Se marca el estado de la instancia
        inst.setStatus(InstanceStatus.SUBMITTED);
        inst.setCompletedAt(Instant.now());


        // 4) Actualizar odómetro y condición operativa en vehículo (si aplica)
        if (inst.getVehicleId() != null) {
            var veh = vehicleRepo.findByIdForUpdate(inst.getVehicleId())
                    .orElseThrow(() -> new BadRequestException("Vehiculo inválido"));

            // 4a) Odómetro: solo si vino (monótono)
            if (inst.getOdometer() != null) {
                int current = Optional.ofNullable(veh.getCurrentOdometer()).orElse(0);
                int newVal  = inst.getOdometer();
                if (newVal < current) {
                    throw new BadRequestException("No se puede actualizar el odómetro: valor menor al actual");
                }
                veh.setCurrentOdometer(newVal);
            } // <- sin else (no fallar por odómetro nulo)

            // 4b) Condición operativa SIEMPRE (derivada de la instancia)
            var cond = vehicleConditionRepo.findByCode(inst.getConditionGeneral())
                    .orElseThrow(() -> new BadRequestException(
                            "Catálogo de condición no configurado: " + inst.getConditionGeneral()));
            veh.setCondition(cond);

            vehicleRepo.save(veh);
        }

        instanceRepo.save(inst);
    }

    //Calculo de condicion

    private String calculateConditionGeneral(ChecklistInstance inst){
        var responses =  responseRepo.findByInstance_Id(inst.getId());

        long criticalNoop = responses.stream()
                .filter(r -> r.getItem().getSeverity().equals("CRITICAL") &&
                        r.getSelectedOption().getCode().equals("NOOP"))
                .count();
        if (criticalNoop > 0){
            return "NO_APTO";
        }
        long anyObs = responses.stream()
                .filter(r -> r.getSelectedOption().getCode().equals("OBS"))
                .count();

        long anyNoop = responses.stream()
                .filter(r -> r.getSelectedOption().getCode().equals("NOOP"))
                .count();

        if (anyObs > 0 || anyNoop > 0) {
            return "APTO_RESTRICCIONES";
        }

        return "APTO";
    }

    /* =========================
       Helpers
       ========================= */

    private final PlatformTransactionManager txManager;

    private void ensureNotExpired(ChecklistInstance inst) {
        if (inst.getDueAt() != null && Instant.now().isAfter(inst.getDueAt())) {
            var tt = new TransactionTemplate(txManager);
            tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW); // <- clave
            tt.executeWithoutResult(status -> {
                var attached = instanceRepo.getReferenceById(inst.getId());
                // Idempotente: no sobreescribir si ya estaba expirado
                if (attached.getStatus() != InstanceStatus.EXPIRED) {
                    attached.setStatus(InstanceStatus.EXPIRED);
                    attached.setCompletedAt(Instant.now());
                    instanceRepo.save(attached);
                }
            });
            throw new ResponseStatusException(HttpStatus.GONE, "La instancia ha expirado");
        }
    }

    private List<PendingPayloadResponse.AttachmentLite> attachmentsLite(Long responseId) {
        return attachmentRepo.findAllByResponse_Id(responseId).stream()
                .map(a -> PendingPayloadResponse.AttachmentLite.builder()
                        .id(a.getId().toString())
                        .filename(a.getFilename())
                        .type(a.getType())
                        .size(a.getSize())
                        .url(ServletUriComponentsBuilder.fromCurrentContextPath()
                                .path("/api/attachments/").path(a.getId().toString()).toUriString())
                        .build())
                .toList();
    }

    private List<InstanceDetailsResponse.AttachmentLite> attachmentsDetails(Long responseId) {
        return attachmentRepo.findAllByResponse_Id(responseId).stream()
                .map(a -> InstanceDetailsResponse.AttachmentLite.builder()
                        .id(a.getId().toString())
                        .filename(a.getFilename())
                        .type(a.getType())
                        .size(a.getSize())
                        .url(ServletUriComponentsBuilder.fromCurrentContextPath()
                                .path("/api/attachments/").path(a.getId().toString()).toUriString())
                        .build())
                .toList();
    }


    private long countCriticalNoop(Long instanceId) {
        var estados = getEstadoById();
        return responseRepo.findByInstance_IdWithItem(instanceId).stream()
                .filter(r -> {
                    var sev = SeverityOptions.from(r.getItem().getSeverity());
                    var st = estadoOf(r, estados);
                    return sev == SeverityOptions.CRITICAL && st == ResponseState.NOOP;
                })
                .count();
    }

    // Sencilla: si hay NOOP -> NO_APTO; si hay OBS -> APTO_RESTRICCIONES; si no -> APTO
    private String computeOverall(long ok, long obs, long noop) {
        if (noop > 0) return "NO_APTO";
        if (obs > 0) return "APTO_RESTRICCIONES";
        return "APTO";
    }

    private void enforceEvidenceRules(ChecklistInstance inst) {
        var items = itemRepo.findByVersion_IdOrderBySection_OrderIndexAscOrderIndexAsc(inst.getVersion().getId())
                .stream().collect(Collectors.toMap(ChecklistItem::getId, it -> it));
        var estados = getEstadoById();
        var responses = responseRepo.findByInstance_Id(inst.getId());

        for (var r : responses) {
            var it = items.get(r.getItem().getId());
            var st = estadoOf(r, estados);
            if (SeverityOptions.from(it.getSeverity()) == SeverityOptions.CRITICAL &&
                    st == ResponseState.NOOP) {
                if (!attachmentRepo.existsByResponse_Id(r.getId())) {
                    throw new BadRequestException("Se requiere evidencia (foto) para " + it.getCode() + " crítico en NOOP.");
                }
            }
        }
    }

    private Long currentUserId() {
        // TODO: integrar con contexto de seguridad
        return 1L;
    }

    /**
     * {ResponseState -> option_id}
     */
    private EnumMap<ResponseState, Long> getEstadoIdsByEnum() {
        var map = new EnumMap<ResponseState, Long>(ResponseState.class);
        for (ResponseState st : ResponseState.values()) {
            Long id = optionRepo.findEstadoGeneralId(st.name())
                    .orElseThrow(() -> new IllegalStateException("No existe OptionItem para estado: " + st.name()));
            map.put(st, id);
        }
        return map;
    }

    /**
     * {option_id -> ResponseState}
     */
    private Map<Long, ResponseState> getEstadoById() {
        var byEnum = getEstadoIdsByEnum();
        return byEnum.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    /**
     * Estado (enum) de una respuesta a partir del option_id guardado
     */
    private ResponseState estadoOf(ChecklistResponse r, Map<Long, ResponseState> estadoById) {
        Long id = r.getSelectedOption().getId();
        var st = estadoById.get(id);
        if (st == null) throw new IllegalStateException("EstadoGeneral desconocido: option_id=" + id);
        return st;
    }
}
