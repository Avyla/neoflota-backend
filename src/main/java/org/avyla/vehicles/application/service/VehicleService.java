package org.avyla.vehicles.application.service;

import lombok.RequiredArgsConstructor;
import org.avyla.common.exceptions.BadRequestException;
import org.avyla.common.exceptions.NotFoundException;
import org.avyla.vehicles.api.dto.VehicleCreateRequest;
import org.avyla.vehicles.api.dto.VehicleDetailResponse;
import org.avyla.vehicles.api.dto.VehicleUpdateRequest;
import org.avyla.vehicles.domain.model.*;
import org.avyla.vehicles.domain.repo.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepo;
    private final VehicleMakeRepository makeRepo;
    private final VehicleTypeRepository typeRepo;
    private final VehicleCategoryRepository categoryRepo;
    private final VehicleFuelTypeRepository fuelTypeRepo;
    private final VehicleStatusRepository statusRepo;
    private final VehicleConditionRepository conditionRepo;

    /**
     * Crea un nuevo vehículo en el sistema.
     * Valida que todos los catálogos existan y que la placa no esté duplicada.
     */
    @Transactional
    public VehicleDetailResponse create(VehicleCreateRequest request, Long currentUserId) {
        // Validar que la placa no exista
        vehicleRepo.findByPlate(request.plate().toUpperCase()).ifPresent(v -> {
            throw new BadRequestException("Ya existe un vehículo con la placa: " + request.plate());
        });

        // Validar y cargar catálogos
        VehicleMake make = makeRepo.findById(request.makeId())
                .orElseThrow(() -> new NotFoundException("Marca no encontrada: " + request.makeId()));

        VehicleType type = typeRepo.findById(request.typeId())
                .orElseThrow(() -> new NotFoundException("Tipo de vehículo no encontrado: " + request.typeId()));

        VehicleCategory category = categoryRepo.findById(request.categoryId())
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada: " + request.categoryId()));

        VehicleFuelType fuelType = fuelTypeRepo.findById(request.fuelTypeId())
                .orElseThrow(() -> new NotFoundException("Tipo de combustible no encontrado: " + request.fuelTypeId()));

        VehicleStatus status = statusRepo.findById(request.statusId())
                .orElseThrow(() -> new NotFoundException("Estado no encontrado: " + request.statusId()));

        VehicleCondition condition = null;
        if (request.conditionId() != null) {
            condition = conditionRepo.findById(request.conditionId())
                    .orElseThrow(() -> new NotFoundException("Condición no encontrada: " + request.conditionId()));
        }

        // Construir entidad
        Instant now = Instant.now();
        Vehicle vehicle = Vehicle.builder()
                .plate(request.plate().toUpperCase()) // Normalizar aunque el trigger también lo hace
                .make(make)
                .modelName(request.modelName())
                .modelYear(request.modelYear())
                .type(type)
                .category(category)
                .fuelType(fuelType)
                .status(status)
                .condition(condition)
                .vin(request.vin())
                .color(request.color())
                .currentOdometer(request.currentOdometer() != null ? request.currentOdometer() : 0)
                .soatExpirationDate(request.soatExpirationDate())
                .rtmExpirationDate(request.rtmExpirationDate())
                .active(true) // Siempre activo al crear
                .createdByUserId(currentUserId)
                .createdAt(now)
                .updatedByUserId(currentUserId)
                .updatedAt(now)
                .build();

        vehicle = vehicleRepo.save(vehicle);

        return toDetailResponse(vehicle);
    }

    /**
     * Obtiene un vehículo por ID con toda su información expandida.
     */
    @Transactional(readOnly = true)
    public VehicleDetailResponse getById(Long id) {
        Vehicle vehicle = vehicleRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Vehículo no encontrado: " + id));

        return toDetailResponse(vehicle);
    }

    /**
     * Actualiza un vehículo existente.
     * Solo actualiza los campos que vienen no nulos en el request.
     */
    @Transactional
    public VehicleDetailResponse update(Long id, VehicleUpdateRequest request, Long currentUserId) {
        Vehicle vehicle = vehicleRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Vehículo no encontrado: " + id));

        // Actualizar marca si viene
        if (request.makeId() != null) {
            VehicleMake make = makeRepo.findById(request.makeId())
                    .orElseThrow(() -> new NotFoundException("Marca no encontrada: " + request.makeId()));
            vehicle.setMake(make);
        }

        // Actualizar tipo si viene
        if (request.typeId() != null) {
            VehicleType type = typeRepo.findById(request.typeId())
                    .orElseThrow(() -> new NotFoundException("Tipo de vehículo no encontrado: " + request.typeId()));
            vehicle.setType(type);
        }

        // Actualizar categoría si viene
        if (request.categoryId() != null) {
            VehicleCategory category = categoryRepo.findById(request.categoryId())
                    .orElseThrow(() -> new NotFoundException("Categoría no encontrada: " + request.categoryId()));
            vehicle.setCategory(category);
        }

        // Actualizar combustible si viene
        if (request.fuelTypeId() != null) {
            VehicleFuelType fuelType = fuelTypeRepo.findById(request.fuelTypeId())
                    .orElseThrow(() -> new NotFoundException("Tipo de combustible no encontrado: " + request.fuelTypeId()));
            vehicle.setFuelType(fuelType);
        }

        // Actualizar estado si viene
        if (request.statusId() != null) {
            VehicleStatus status = statusRepo.findById(request.statusId())
                    .orElseThrow(() -> new NotFoundException("Estado no encontrado: " + request.statusId()));
            vehicle.setStatus(status);
        }

        // Actualizar condición si viene
        if (request.conditionId() != null) {
            VehicleCondition condition = conditionRepo.findById(request.conditionId())
                    .orElseThrow(() -> new NotFoundException("Condición no encontrada: " + request.conditionId()));
            vehicle.setCondition(condition);
        }

        // Actualizar campos simples si vienen
        if (request.modelName() != null && !request.modelName().isBlank()) {
            vehicle.setModelName(request.modelName());
        }
        if (request.modelYear() != null) {
            vehicle.setModelYear(request.modelYear());
        }
        if (request.vin() != null) {
            vehicle.setVin(request.vin());
        }
        if (request.color() != null) {
            vehicle.setColor(request.color());
        }
        if (request.currentOdometer() != null) {
            vehicle.setCurrentOdometer(request.currentOdometer());
        }
        if (request.soatExpirationDate() != null) {
            vehicle.setSoatExpirationDate(request.soatExpirationDate());
        }
        if (request.rtmExpirationDate() != null) {
            vehicle.setRtmExpirationDate(request.rtmExpirationDate());
        }
        if (request.active() != null) {
            vehicle.setActive(request.active());
        }

        // Auditoría
        vehicle.setUpdatedByUserId(currentUserId);
        vehicle.setUpdatedAt(Instant.now());

        vehicle = vehicleRepo.save(vehicle);

        return toDetailResponse(vehicle);
    }

    /**
     * Desactiva un vehículo (soft delete).
     */
    @Transactional
    public void deactivate(Long id, Long currentUserId) {
        Vehicle vehicle = vehicleRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Vehículo no encontrado: " + id));

        vehicle.setActive(false);
        vehicle.setUpdatedByUserId(currentUserId);
        vehicle.setUpdatedAt(Instant.now());

        vehicleRepo.save(vehicle);
    }

    /**
     * Reactiva un vehículo previamente desactivado.
     */
    @Transactional
    public void activate(Long id, Long currentUserId) {
        Vehicle vehicle = vehicleRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Vehículo no encontrado: " + id));

        vehicle.setActive(true);
        vehicle.setUpdatedByUserId(currentUserId);
        vehicle.setUpdatedAt(Instant.now());

        vehicleRepo.save(vehicle);
    }

    /**
     * Lista vehículos con paginación.
     * Por defecto solo trae activos, pero se puede incluir inactivos.
     */
    @Transactional(readOnly = true)
    public Page<VehicleDetailResponse> list(Pageable pageable, Boolean includeInactive) {
        Page<Vehicle> vehicles;

        if (Boolean.TRUE.equals(includeInactive)) {
            vehicles = vehicleRepo.findAll(pageable);
        } else {
            vehicles = vehicleRepo.findAll(pageable); // TODO: crear query findByActiveTrue
        }

        return vehicles.map(this::toDetailResponse);
    }

    /**
     * Convierte una entidad Vehicle a VehicleDetailResponse con todos los datos expandidos.
     */
    private VehicleDetailResponse toDetailResponse(Vehicle v) {
        Long soatDays = v.getSoatExpirationDate() == null ? null
                : ChronoUnit.DAYS.between(LocalDate.now(), v.getSoatExpirationDate());

        Long rtmDays = v.getRtmExpirationDate() == null ? null
                : ChronoUnit.DAYS.between(LocalDate.now(), v.getRtmExpirationDate());

        return VehicleDetailResponse.builder()
                .id(v.getVehicleId())
                .plate(v.getPlate())
                // Marca
                .makeId(v.getMake() != null ? v.getMake().getId() : null)
                .makeName(v.getMake() != null ? v.getMake().getName() : null)
                // Modelo
                .modelName(v.getModelName())
                .modelYear(v.getModelYear())
                // Tipo
                .typeId(v.getType() != null ? v.getType().getId() : null)
                .typeName(v.getType() != null ? v.getType().getName() : null)
                // Categoría
                .categoryId(v.getCategory() != null ? v.getCategory().getId() : null)
                .categoryName(v.getCategory() != null ? v.getCategory().getName() : null)
                // Combustible
                .fuelTypeId(v.getFuelType() != null ? v.getFuelType().getId() : null)
                .fuelTypeName(v.getFuelType() != null ? v.getFuelType().getName() : null)
                // Estado
                .statusId(v.getStatus() != null ? v.getStatus().getId() : null)
                .statusCode(v.getStatus() != null ? v.getStatus().getCode() : null)
                .statusName(v.getStatus() != null ? v.getStatus().getName() : null)
                // Condición
                .conditionId(v.getCondition() != null ? v.getCondition().getId() : null)
                .conditionCode(v.getCondition() != null ? v.getCondition().getCode().name() : null)
                .conditionName(v.getCondition() != null ? v.getCondition().getName() : null)
                // Datos opcionales
                .vin(v.getVin())
                .color(v.getColor())
                .currentOdometer(v.getCurrentOdometer())
                .soatExpirationDate(v.getSoatExpirationDate())
                .rtmExpirationDate(v.getRtmExpirationDate())
                // Cálculos
                .daysToSoatExpiration(soatDays)
                .daysToRtmExpiration(rtmDays)
                // Metadatos
                .active(v.isActive())
                .createdByUserId(v.getCreatedByUserId())
                .createdAt(v.getCreatedAt())
                .updatedByUserId(v.getUpdatedByUserId())
                .updatedAt(v.getUpdatedAt())
                .build();
    }
}