package org.avyla.checklists.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.avyla.checklists.api.dto.request.SaveResponsesRequest;
import org.avyla.checklists.api.dto.response.CreateInstanceResponse;
import org.avyla.checklists.api.dto.response.InstanceDetailsResponse;
import org.avyla.checklists.api.dto.response.PendingPayloadResponse;
import org.avyla.checklists.application.service.ChecklistService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

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
    public void submit(@PathVariable Long id) {
        service.submit(id);
    }


}
