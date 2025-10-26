package org.avyla.checklists.application.service;

import lombok.RequiredArgsConstructor;
import org.avyla.vehicles.api.dto.VehiclePublishedMetadata;
import org.avyla.vehicles.api.dto.VehiclePublishedMetadata.*;
import org.avyla.vehicles.domain.model.*;
import org.avyla.vehicles.domain.repo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio que genera la metadata publicada para el frontend.
 * Incluye todos los catálogos activos y reglas de validación.
 */
@Service
@RequiredArgsConstructor
public class VehiclePublishedService {

    private final VehicleMakeRepository makeRepo;
    private final VehicleTypeRepository typeRepo;
    private final VehicleCategoryRepository categoryRepo;
    private final VehicleFuelTypeRepository fuelTypeRepo;
    private final VehicleStatusRepository statusRepo;
    private final VehicleConditionRepository conditionRepo;

    @Transactional(readOnly = true)
    public VehiclePublishedMetadata getPublishedMetadata() {
        // Cargar todos los catálogos activos
        List<VehicleMake> makes = makeRepo.findAllActive();
        List<VehicleType> types = typeRepo.findAllActive();
        List<VehicleCategory> categories = categoryRepo.findAllActive();
        List<VehicleFuelType> fuelTypes = fuelTypeRepo.findAllActive();
        List<VehicleStatus> statuses = statusRepo.findAllActive();
        List<VehicleCondition> conditions = conditionRepo.findAll(); // Todas las condiciones

        // Mapear a DTOs
        CatalogsDTO catalogs = CatalogsDTO.builder()
                .makes(makes.stream()
                        .map(m -> CatalogItemDTO.builder()
                                .id(m.getId())
                                .name(m.getName())
                                .build())
                        .collect(Collectors.toList()))
                .types(types.stream()
                        .map(t -> CatalogItemDTO.builder()
                                .id(t.getId())
                                .name(t.getName())
                                .build())
                        .collect(Collectors.toList()))
                .categories(categories.stream()
                        .map(c -> CatalogItemDTO.builder()
                                .id(c.getId())
                                .name(c.getName())
                                .build())
                        .collect(Collectors.toList()))
                .fuelTypes(fuelTypes.stream()
                        .map(f -> CatalogItemDTO.builder()
                                .id(f.getId())
                                .name(f.getName())
                                .build())
                        .collect(Collectors.toList()))
                .statuses(statuses.stream()
                        .map(s -> StatusItemDTO.builder()
                                .id(s.getId())
                                .code(s.getCode())
                                .name(s.getName())
                                .description(s.getDescription())
                                .build())
                        .collect(Collectors.toList()))
                .conditions(conditions.stream()
                        .map(c -> ConditionItemDTO.builder()
                                .id(c.getId())
                                .code(c.getCode().name())
                                .name(c.getName())
                                .order(c.getOrderIndex())
                                .build())
                        .collect(Collectors.toList()))
                .build();

        // Reglas de validación
        ValidationRulesDTO validationRules = ValidationRulesDTO.builder()
                .plate(PlateValidationDTO.builder()
                        .pattern("^(?:[A-Z]{3}[0-9]{3}|[A-Z]{3}[0-9]{2}[A-Z])$")
                        .format("Formato Colombia sin guion")
                        .description("Placa debe ser ABC123 (vehículos) o ABC12D (motos), solo mayúsculas sin guion")
                        .examples(List.of("ABC123", "XYZ456", "DEF12G"))
                        .build())
                .modelYear(ModelYearValidationDTO.builder()
                        .min(1950)
                        .max(2099)
                        .build())
                .odometer(OdometerValidationDTO.builder()
                        .min(0)
                        .unit("kilómetros")
                        .build())
                .requiredFields(List.of(
                        "plate",
                        "makeId",
                        "modelName",
                        "typeId",
                        "categoryId",
                        "fuelTypeId",
                        "statusId"
                ))
                .build();

        // Generar versión (hash de catálogos)
        String version = computeVersionHash(catalogs);

        return VehiclePublishedMetadata.builder()
                .catalogs(catalogs)
                .validationRules(validationRules)
                .version(version)
                .build();
    }

    /**
     * Genera un hash SHA-256 basado en los catálogos activos.
     * Si los catálogos cambian, el hash cambia y el frontend sabe que debe recargar.
     */
    private String computeVersionHash(CatalogsDTO catalogs) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            StringBuilder sb = new StringBuilder();

            // Concatenar todos los catálogos para el hash
            catalogs.makes().forEach(m -> sb.append(m.id()).append(m.name()).append("|"));
            catalogs.types().forEach(t -> sb.append(t.id()).append(t.name()).append("|"));
            catalogs.categories().forEach(c -> sb.append(c.id()).append(c.name()).append("|"));
            catalogs.fuelTypes().forEach(f -> sb.append(f.id()).append(f.name()).append("|"));
            catalogs.statuses().forEach(s -> sb.append(s.id()).append(s.code()).append("|"));
            catalogs.conditions().forEach(c -> sb.append(c.id()).append(c.code()).append("|"));

            byte[] hash = md.digest(sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString().substring(0, 16); // Primeros 16 caracteres
        } catch (Exception e) {
            return "v1.0.0"; // Fallback
        }
    }
}
