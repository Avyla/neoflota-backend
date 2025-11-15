package org.avyla.vehicles.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.avyla.security.application.service.CurrentUserService;
import org.avyla.vehicles.api.dto.request.VehicleCreateRequest;
import org.avyla.vehicles.api.dto.response.VehicleDetailResponse;
import org.avyla.vehicles.api.dto.response.VehicleSummaryResponse;
import org.avyla.vehicles.api.dto.request.VehicleUpdateRequest;
import org.avyla.vehicles.application.service.VehicleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para operaciones CRUD de vehículos.
 * Endpoints:
 *   POST   /api/vehicles              - Crear vehículo
 *   GET    /api/vehicles/{id}         - Obtener por ID
 *   PUT    /api/vehicles/{id}         - Actualizar vehículo
 *   DELETE /api/vehicles/{id}         - Desactivar vehículo
 *   PATCH  /api/vehicles/{id}/activate - Reactivar vehículo
 *   GET    /api/vehicles              - Listar con paginación
 *   GET    /api/vehicles?vencenEn=30  - Vehículos próximos a vencer
 */
@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class   VehicleController {

    private final VehicleService vehicleService;
    private final CurrentUserService currentUserService;

    /**
     * Crea un nuevo vehículo.
     * POST /api/vehicles
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleDetailResponse create(@Valid @RequestBody VehicleCreateRequest request) {
        Long currentUserId = currentUserService.getCurrentUserId();
        return vehicleService.create(request, currentUserId);
    }

    /**
     * Obtiene un vehículo por ID con todos sus detalles.
     * GET /api/vehicles/{id}
     */
    @GetMapping("/{id}")
    public VehicleDetailResponse getById(@PathVariable Long id) {
        return vehicleService.getById(id);
    }

    /**
     * Actualiza un vehículo existente.
     * PUT /api/vehicles/{id}
     */
    @PutMapping("/{id}")
    public VehicleDetailResponse update(
            @PathVariable Long id,
            @Valid @RequestBody VehicleUpdateRequest request
    ) {
        Long currentUserId = currentUserService.getCurrentUserId();
        return vehicleService.update(id, request, currentUserId);
    }

    /**
     * Desactiva un vehículo (soft delete).
     * DELETE /api/vehicles/{id}
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {
        Long currentUserId = currentUserService.getCurrentUserId();
        vehicleService.deactivate(id, currentUserId);
    }

    /**
     * Reactiva un vehículo previamente desactivado.
     * PATCH /api/vehicles/{id}/activate
     */
    @PatchMapping("/{id}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void activate(@PathVariable Long id) {
        Long currentUserId = currentUserService.getCurrentUserId();
        vehicleService.activate(id, currentUserId);
    }

    /**
     * Lista vehículos con paginación.
     * GET /api/vehicles
     * Parámetros opcionales:
     *   - page: número de página (default: 0)
     *   - size: tamaño de página (default: 20)
     *   - sort: ordenamiento (default: plate,asc)
     *   - includeInactive: incluir inactivos (default: false)
     */
    @GetMapping
    public Page<VehicleDetailResponse> list(
            @PageableDefault(size = 20, sort = "plate", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(required = false, defaultValue = "false") Boolean includeInactive
    ) {
        return vehicleService.list(pageable, includeInactive);
    }

    /**
     * Obtiene vehículos próximos a vencer SOAT o RTM.
     * GET /api/vehicles?vencenEn=30
     *
     * Este endpoint tiene prioridad sobre el listado general por el parámetro específico.
     */
    @GetMapping(params = "vencenEn")
    public List<VehicleSummaryResponse> getExpiring(@RequestParam("vencenEn") int days) {
        return vehicleService.findExpiringByDate(days);
    }

}