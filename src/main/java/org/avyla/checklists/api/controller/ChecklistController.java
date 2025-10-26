package org.avyla.checklists.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.avyla.checklists.api.dto.InstanceDetailsResponse;
import org.avyla.checklists.api.dto.PendingPayloadResponse;
import org.avyla.checklists.api.dto.SaveResponsesRequest;
import org.avyla.checklists.api.dto.SubmitRequest;
import org.avyla.checklists.application.service.ChecklistService;
import org.avyla.checklists.infrastructure.InstanceStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;

@RestController
@RequestMapping("/api/checklists")
@RequiredArgsConstructor
public class ChecklistController {

    private final ChecklistService service;

    @PostMapping("/instances")
    public ResponseEntity<CreateInstanceResponse> createInstance(@RequestParam String templateCode,
                                                                 @RequestParam Long driverId) {
        var inst = service.createInstance(templateCode, driverId);
        var body = new CreateInstanceResponse(
                inst.getId(),
                inst.getStatus(),      // status es String en la entidad
                inst.getStartedAt(),
                inst.getDueAt()
        );
        return ResponseEntity.created(URI.create("/api/checklists/instances/" + inst.getId()))
                .body(body);
    }

    @PostMapping("/instances/{id}/responses")
    public ResponseEntity<Void> saveResponses(@PathVariable Long id,
                                              @Valid @RequestBody SaveResponsesRequest req) {
        service.saveResponses(id, req);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/drivers/{driverId}/instances/pending/payload")
    public ResponseEntity<PendingPayloadResponse> pendingPayload(@PathVariable Long driverId) {
        var payload = service.getPendingPayload(driverId);
        if (payload == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(payload);
    }

    @GetMapping("/instances/{id}/details")
    public InstanceDetailsResponse instanceDetails(@PathVariable Long id) {
        return service.getInstanceDetails(id);
    }

    @PostMapping("/instances/{id}/submit")
    @ResponseStatus(HttpStatus.OK)
    public void submit(@PathVariable Long id, @RequestBody @Valid SubmitRequest req) {
        service.submit(id, req);
    }

    private record CreateInstanceResponse(Long instanceId, InstanceStatus status, Instant startedAt, Instant dueAt) {}
}
