package org.avyla.checklists.api.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.avyla.checklists.api.dto.CreateInstanceRequest;
import org.avyla.checklists.api.dto.CreateInstanceResponse;
import org.avyla.checklists.api.dto.InstanceSummaryResponse;
import org.avyla.checklists.api.dto.SaveResponsesRequest;
import org.avyla.checklists.api.dto.SubmitRequest;
import org.avyla.checklists.application.service.ChecklistService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/checklists")
@RequiredArgsConstructor
@Validated
public class ChecklistController {

    private final ChecklistService service;

    /** POST /checklists/instances -> crear instancia */
    @PostMapping("/instances")
    public ResponseEntity<CreateInstanceResponse> createInstance(@Valid @RequestBody CreateInstanceRequest req) {
        var res = service.createInstance(req);
        return ResponseEntity.created(URI.create("/checklists/instances/" + res.instanceId())).body(res);
    }

    /** POST /checklists/instances/{id}/responses -> guardar respuestas (batch) */
    @PostMapping("/instances/{id}/responses")
    public ResponseEntity<Void> saveResponses(@PathVariable @Positive long id,
                                              @Valid @RequestBody SaveResponsesRequest req) {
        service.saveResponses(id, req);
        return ResponseEntity.noContent().build(); // 204
    }

    /** POST /checklists/instances/{id}/submit -> cerrar y calcular resultado */
    @PostMapping("/instances/{id}/submit")
    public InstanceSummaryResponse submit(@PathVariable @Positive long id,
                                          @RequestBody(required = false) SubmitRequest ignored) {
        return service.submit(id);
    }

    /** GET /checklists/instances/{id} -> ver resumen */
    @GetMapping("/instances/{id}")
    public InstanceSummaryResponse get(@PathVariable @Positive long id) {
        return service.getSummary(id);
    }
}
