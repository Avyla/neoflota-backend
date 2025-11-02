package org.avyla.vehicles.application.service;

import org.avyla.checklists.domain.enums.ConditionOptions;
import org.avyla.shared.exception.BadRequestException;
import org.avyla.shared.exception.NotFoundException;
import org.avyla.shared.util.TestDataBuilder;
import org.avyla.vehicles.api.dto.request.VehicleCreateRequest;
import org.avyla.vehicles.api.dto.request.VehicleUpdateRequest;
import org.avyla.vehicles.api.dto.response.VehicleDetailResponse;
import org.avyla.vehicles.domain.entity.*;
import org.avyla.vehicles.domain.repo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para VehicleService.
 * <p>
 * Cobertura:
 * - create(): Creación de vehículos, validación de duplicados y catálogos
 * - getById(): Obtención por ID, manejo de no encontrado
 * - update(): Actualización parcial de campos
 * - deactivate(): Soft delete
 * - activate(): Reactivación
 * - list(): Paginación y filtros
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VehicleService - Tests Unitarios")
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepo;

    @Mock
    private VehicleMakeRepository makeRepo;

    @Mock
    private VehicleTypeRepository typeRepo;

    @Mock
    private VehicleCategoryRepository categoryRepo;

    @Mock
    private VehicleFuelTypeRepository fuelTypeRepo;

    @Mock
    private VehicleStatusRepository statusRepo;

    @Mock
    private VehicleConditionRepository conditionRepo;

    @InjectMocks
    private VehicleService vehicleService;

    private VehicleMake mockMake;
    private VehicleType mockType;
    private VehicleCategory mockCategory;
    private VehicleFuelType mockFuelType;
    private VehicleStatus mockStatus;
    private VehicleCondition mockCondition;
    private Vehicle mockVehicle;

    @BeforeEach
    void setUp() {
        // Preparar catálogos mock
        mockMake = TestDataBuilder.createVehicleMake();
        mockType = TestDataBuilder.createVehicleType();
        mockCategory = TestDataBuilder.createVehicleCategory();
        mockFuelType = TestDataBuilder.createVehicleFuelType();
        mockStatus = TestDataBuilder.createVehicleStatus();
        mockCondition = TestDataBuilder.createVehicleCondition();

        // Vehículo mock para reutilizar
        mockVehicle = TestDataBuilder.createVehicle();
    }

    // ==================== CREATE ====================

    @Nested
    @DisplayName("create() - Creación de vehículos")
    class CreateTests {

        @Test
        @DisplayName("Debe crear vehículo exitosamente con todos los datos válidos")
        void shouldCreateVehicleSuccessfully() {
            // Given
            VehicleCreateRequest request = new VehicleCreateRequest("ABC123", 1L, // makeId
                    "Hilux", 2023, 1L, // typeId
                    1L, // categoryId
                    1L, // fuelTypeId
                    1L, // statusId
                    1L, // conditionId
                    "1HGBH41JXMN109186", "Blanco", 50000, LocalDate.now().plusMonths(6), LocalDate.now().plusMonths(3));
            Long currentUserId = 100L;

            // Simular que la placa no existe
            when(vehicleRepo.findByPlate("ABC123")).thenReturn(Optional.empty());

            // Simular que todos los catálogos existen
            when(makeRepo.findById(1L)).thenReturn(Optional.of(mockMake));
            when(typeRepo.findById(1L)).thenReturn(Optional.of(mockType));
            when(categoryRepo.findById(1L)).thenReturn(Optional.of(mockCategory));
            when(fuelTypeRepo.findById(1L)).thenReturn(Optional.of(mockFuelType));
            when(statusRepo.findById(1L)).thenReturn(Optional.of(mockStatus));
            when(conditionRepo.findById(1L)).thenReturn(Optional.of(mockCondition));

            // Simular el guardado
            when(vehicleRepo.save(any(Vehicle.class))).thenAnswer(invocation -> {
                Vehicle v = invocation.getArgument(0);
                v.setVehicleId(1L); // Simular ID generado
                return v;
            });

            // When
            VehicleDetailResponse response = vehicleService.create(request, currentUserId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.plate()).isEqualTo("ABC123");
            assertThat(response.makeName()).isEqualTo("Toyota");
            assertThat(response.modelName()).isEqualTo("Hilux");
            assertThat(response.modelYear()).isEqualTo(2023);
            assertThat(response.active()).isTrue();
            assertThat(response.createdByUserId()).isEqualTo(currentUserId);

            // Verificar que se guardó el vehículo
            verify(vehicleRepo, times(1)).save(any(Vehicle.class));

            // Verificar que la placa se normalizó a mayúsculas
            ArgumentCaptor<Vehicle> vehicleCaptor = ArgumentCaptor.forClass(Vehicle.class);
            verify(vehicleRepo).save(vehicleCaptor.capture());
            assertThat(vehicleCaptor.getValue().getPlate()).isEqualTo("ABC123");
        }

        @Test
        @DisplayName("Debe normalizar placa a mayúsculas al crear")
        void shouldNormalizePlateToUppercase() {
            // Given
            VehicleCreateRequest request = VehicleCreateRequest.builder().plate("abc123").makeId(1L).modelName("Model").modelYear(2023).typeId(1L).categoryId(1L).fuelTypeId(1L).statusId(1L).build();

            when(vehicleRepo.findByPlate("ABC123")).thenReturn(Optional.empty());
            when(makeRepo.findById(anyLong())).thenReturn(Optional.of(mockMake));
            when(typeRepo.findById(anyLong())).thenReturn(Optional.of(mockType));
            when(categoryRepo.findById(anyLong())).thenReturn(Optional.of(mockCategory));
            when(fuelTypeRepo.findById(anyLong())).thenReturn(Optional.of(mockFuelType));
            when(statusRepo.findById(anyLong())).thenReturn(Optional.of(mockStatus));
            //when(conditionRepo.findById(anyLong())).thenReturn(Optional.of(mockCondition));
            when(vehicleRepo.save(any())).thenAnswer(i -> i.getArgument(0));

            // When
            vehicleService.create(request, 1L);

            // Then
            ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
            verify(vehicleRepo).save(captor.capture());
            assertThat(captor.getValue().getPlate()).isEqualTo("ABC123");
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si la placa ya existe")
        void shouldThrowBadRequestExceptionWhenPlateExists() {

            // Given
            VehicleCreateRequest request = VehicleCreateRequest.builder().plate("ABC123").makeId(1L).modelName("Model").modelYear(2023).typeId(1L).categoryId(1L).fuelTypeId(1L).statusId(1L).build();

            // Simular que la placa ya existe
            when(vehicleRepo.findByPlate("ABC123")).thenReturn(Optional.of(mockVehicle));

            // When & Then
            assertThatThrownBy(() -> vehicleService.create(request, 1L)).isInstanceOf(BadRequestException.class).hasMessageContaining("Ya existe un vehículo con la placa: ABC123");

            // No debe intentar guardar
            verify(vehicleRepo, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar NotFoundException si la marca no existe")
        void shouldThrowNotFoundExceptionWhenMakeNotFound() {
            // Given
            VehicleCreateRequest request = VehicleCreateRequest.builder().plate("ABC123").makeId(999L).modelName("Model").modelYear(2023).typeId(1L).categoryId(1L).fuelTypeId(1L).statusId(1L).build();

            when(vehicleRepo.findByPlate(anyString())).thenReturn(Optional.empty());
            when(makeRepo.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> vehicleService.create(request, 1L)).isInstanceOf(NotFoundException.class).hasMessageContaining("Marca no encontrada: 999");

            verify(vehicleRepo, never()).save(any());
        }

        @Test
        @DisplayName("Debe crear vehículo con condición null (opcional)")
        void shouldCreateVehicleWithoutCondition() {
            // Given
            VehicleCreateRequest request = VehicleCreateRequest.builder().plate("ABC123").makeId(1L).modelName("Model").modelYear(2023).typeId(1L).categoryId(1L).fuelTypeId(1L).conditionId(null).statusId(1L).build();

            when(vehicleRepo.findByPlate(anyString())).thenReturn(Optional.empty());
            when(makeRepo.findById(anyLong())).thenReturn(Optional.of(mockMake));
            when(typeRepo.findById(anyLong())).thenReturn(Optional.of(mockType));
            when(categoryRepo.findById(anyLong())).thenReturn(Optional.of(mockCategory));
            when(fuelTypeRepo.findById(anyLong())).thenReturn(Optional.of(mockFuelType));
            when(statusRepo.findById(anyLong())).thenReturn(Optional.of(mockStatus));
            when(vehicleRepo.save(any())).thenAnswer(i -> i.getArgument(0));

            // When
            VehicleDetailResponse response = vehicleService.create(request, 1L);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.conditionId()).isNull();

            // No debe buscar condición
            verify(conditionRepo, never()).findById(any());
        }

        @Test
        @DisplayName("Debe establecer odómetro en 0 si es null")
        void shouldSetOdometerToZeroWhenNull() {
            // Given
            VehicleCreateRequest request = VehicleCreateRequest.builder().plate("ABC123").makeId(1L).modelName("Model").modelYear(2023).typeId(1L).categoryId(1L).fuelTypeId(1L).statusId(1L).currentOdometer(null).build();

            when(vehicleRepo.findByPlate("ABC123")).thenReturn(Optional.empty());
            when(makeRepo.findById(anyLong())).thenReturn(Optional.of(mockMake));
            when(typeRepo.findById(anyLong())).thenReturn(Optional.of(mockType));
            when(categoryRepo.findById(anyLong())).thenReturn(Optional.of(mockCategory));
            when(fuelTypeRepo.findById(anyLong())).thenReturn(Optional.of(mockFuelType));
            when(statusRepo.findById(anyLong())).thenReturn(Optional.of(mockStatus));
            //when(conditionRepo.findById(anyLong())).thenReturn(Optional.of(mockCondition));
            when(vehicleRepo.save(any())).thenAnswer(i -> i.getArgument(0));

            // When
            vehicleService.create(request, 1L);

            // Then
            ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
            verify(vehicleRepo).save(captor.capture());
            assertThat(captor.getValue().getCurrentOdometer()).isEqualTo(0);
        }
    }

    // ==================== GET BY ID ====================

    @Nested
    @DisplayName("getById() - Obtener vehículo por ID")
    class GetByIdTests {

        @Test
        @DisplayName("Debe obtener vehículo existente con todos los datos expandidos")
        void shouldGetVehicleById() {
            // Given
            Long vehicleId = 1L;
            when(vehicleRepo.findById(vehicleId)).thenReturn(Optional.of(mockVehicle));

            // When
            VehicleDetailResponse response = vehicleService.getById(vehicleId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(vehicleId);
            assertThat(response.plate()).isEqualTo(mockVehicle.getPlate());
            assertThat(response.makeName()).isEqualTo("Toyota");
            assertThat(response.typeName()).isEqualTo("Camioneta");

            verify(vehicleRepo, times(1)).findById(vehicleId);
        }

        @Test
        @DisplayName("Debe lanzar NotFoundException si el vehículo no existe")
        void shouldThrowNotFoundExceptionWhenVehicleNotFound() {
            // Given
            Long vehicleId = 999L;
            when(vehicleRepo.findById(vehicleId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> vehicleService.getById(vehicleId)).isInstanceOf(NotFoundException.class).hasMessageContaining("Vehículo no encontrado: 999");
        }

        @Test
        @DisplayName("Debe calcular días para vencimiento de SOAT correctamente")
        void shouldCalculateDaysToSoatExpiration() {
            // Given
            Vehicle vehicle = TestDataBuilder.createVehicle();
            vehicle.setSoatExpirationDate(LocalDate.now().plusDays(30));
            when(vehicleRepo.findById(1L)).thenReturn(Optional.of(vehicle));

            // When
            VehicleDetailResponse response = vehicleService.getById(1L);

            // Then
            assertThat(response.daysToSoatExpiration()).isNotNull();
            assertThat(response.daysToSoatExpiration()).isGreaterThanOrEqualTo(29L);
            assertThat(response.daysToSoatExpiration()).isLessThanOrEqualTo(30L);
        }
    }

    // ==================== UPDATE ====================

    @Nested
    @DisplayName("update() - Actualizar vehículo")
    class UpdateTests {

        @Test
        @DisplayName("Debe actualizar solo los campos proporcionados (actualización parcial)")
        void shouldUpdateOnlyProvidedFields() {
            // Given
            Long vehicleId = 1L;

            VehicleUpdateRequest request = VehicleUpdateRequest.builder().modelName("Hilux 4x4").modelYear(2024).build();

            when(vehicleRepo.findById(vehicleId)).thenReturn(Optional.of(mockVehicle));
            when(vehicleRepo.save(any())).thenAnswer(i -> i.getArgument(0));

            // When
            VehicleDetailResponse response = vehicleService.update(vehicleId, request, 1L);

            // Then
            assertThat(response.modelName()).isEqualTo("Hilux 4x4");
            assertThat(response.modelYear()).isEqualTo(2024);

            // Verificar que se guardó
            ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
            verify(vehicleRepo).save(captor.capture());

            Vehicle saved = captor.getValue();
            assertThat(saved.getModelName()).isEqualTo("Hilux 4x4");
            assertThat(saved.getModelYear()).isEqualTo(2024);
            assertThat(saved.getMake()).isEqualTo(mockMake);
            assertThat(saved.getType()).isEqualTo(mockType);
        }

        @Test
        @DisplayName("Debe actualizar tipo si viene en el request")
        void shouldUpdateTypeWhenProvided() {
            //Given
            Long vehicleId = 1L;

            VehicleType newType = TestDataBuilder.createVehicleType(2L, "Sedán");

            VehicleUpdateRequest request = VehicleUpdateRequest.builder().typeId(2L).build();

            when(vehicleRepo.findById(vehicleId)).thenReturn(Optional.of(mockVehicle));
            when(typeRepo.findById(2L)).thenReturn(Optional.of(newType));
            when(vehicleRepo.save(any())).thenAnswer(i -> i.getArgument(0));

            // When
            vehicleService.update(vehicleId, request, 1L);

            // Then
            ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
            verify(vehicleRepo).save(captor.capture());
            assertThat(captor.getValue().getType()).isEqualTo(newType);
        }

        @Test
        @DisplayName("Debe lanzar NotFoundException si un tipo a actualizar no existe")
        void shouldThrowNotFoundExceptionWhenVehicleTypeNotFound() {
            //Given
            VehicleUpdateRequest request = VehicleUpdateRequest.builder().typeId(999L).build();

            when(vehicleRepo.findById(1L)).thenReturn(Optional.of(mockVehicle));
            when(typeRepo.findById(999L)).thenReturn(Optional.empty());

            //When y Then
            assertThatThrownBy(() -> vehicleService.update(1L, request, 999L)).isInstanceOf(NotFoundException.class).hasMessageContaining("Tipo de vehículo no encontrado: 999");

            verify(vehicleRepo, never()).save(any());
        }

        @Test
        @DisplayName("Debe actualizar categoria si viene en el request")
        void shouldUpdateCategoryWhenProvided() {

            //Given
            Long vehicleId = 1L;
            VehicleCategory newCategory = TestDataBuilder.createVehicleCategory(1L, "Pesado");

            VehicleUpdateRequest request = VehicleUpdateRequest.builder().categoryId(1L).build();

            when(vehicleRepo.findById(vehicleId)).thenReturn(Optional.of(mockVehicle));
            when(categoryRepo.findById(1L)).thenReturn(Optional.of(newCategory));
            when(vehicleRepo.save(any())).thenAnswer(i -> i.getArgument(0));

            // When
            vehicleService.update(vehicleId, request, 1L);

            // Then
            ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
            verify(vehicleRepo).save(captor.capture());
            assertThat(captor.getValue().getCategory()).isEqualTo(newCategory);

        }

        @Test
        @DisplayName("Debe lanzar NotFoundException si una categoría a actualizar no existe")
        void shouldThrowNotFoundExceptionWhenVehicleCategoryNotFound() {
            //Given
            VehicleUpdateRequest request = VehicleUpdateRequest.builder().categoryId(999L).build();

            when(vehicleRepo.findById(1L)).thenReturn(Optional.of(mockVehicle));
            when(categoryRepo.findById(999L)).thenReturn(Optional.empty());

            //When y Then
            assertThatThrownBy(() -> vehicleService.update(1L, request, 999L)).isInstanceOf(NotFoundException.class).hasMessageContaining("Categoría no encontrada: 999");

            verify(vehicleRepo, never()).save(any());
        }

        @Test
        @DisplayName("Debe actualizar combustible si viene en el request")
        void shouldUpdateFuelTypeWhenProvided() {
            //Given
            Long vehicleId = 1L;
            VehicleFuelType newFuelType = TestDataBuilder.createVehicleFuelType(2L, "Diésel");

            VehicleUpdateRequest request = VehicleUpdateRequest.builder().fuelTypeId(2L).build();

            when(vehicleRepo.findById(vehicleId)).thenReturn(Optional.of(mockVehicle));
            when(fuelTypeRepo.findById(2L)).thenReturn(Optional.of(newFuelType));
            when(vehicleRepo.save(any())).thenAnswer(i -> i.getArgument(0));

            // When
            vehicleService.update(vehicleId, request, 1L);

            // Then
            ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
            verify(vehicleRepo).save(captor.capture());
            assertThat(captor.getValue().getFuelType()).isEqualTo(newFuelType);
        }

        @Test
        @DisplayName("Debe lanzar NotFoundException si un tipo de combustible a actualizar no existe")
        void shouldThrowNotFoundExceptionWhenVehicleFuelTypeNotFound() {
            //Given
            VehicleUpdateRequest request = VehicleUpdateRequest.builder().fuelTypeId(999L).build();
            when(vehicleRepo.findById(1L)).thenReturn(Optional.of(mockVehicle));
            when(fuelTypeRepo.findById(999L)).thenReturn(Optional.empty());

            //When y Then
            assertThatThrownBy(() -> vehicleService.update(1L, request, 999L)).isInstanceOf(NotFoundException.class).hasMessageContaining("Tipo de combustible no encontrado: 999");
            verify(vehicleRepo, never()).save(any());
        }

        @Test
        @DisplayName("Debe actualizar estado si viene en el request")
        void shouldUpdateStatusWhenProvided() {
            //Given
            Long vehicleId = 1L;
            VehicleStatus newStatus = TestDataBuilder.createVehicleStatus(2L, "IN_REPAIR", "En mantenimiento", "El vehículo se encuentra en taller o reparación.");

            VehicleUpdateRequest request = VehicleUpdateRequest.builder().statusId(2L).build();

            when(vehicleRepo.findById(vehicleId)).thenReturn(Optional.of(mockVehicle));
            when(vehicleRepo.save(any())).thenAnswer(i -> i.getArgument(0));
            when(statusRepo.findById(2L)).thenReturn(Optional.of(newStatus));

            //When
            vehicleService.update(vehicleId, request, 2L);

            //Then
            ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
            verify(vehicleRepo).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(newStatus);
        }

        @Test
        @DisplayName("Debe lanzar NotFoundException si un estado a actualizar no existe")
        void shouldThrowNotFoundExceptionWhenVehicleStatusNotFound() {
            //Given
            VehicleUpdateRequest request = VehicleUpdateRequest.builder()
                    .statusId(999L)
                    .build();
            when(vehicleRepo.findById(1L)).thenReturn(Optional.of(mockVehicle));
            when(statusRepo.findById(999L)).thenReturn(Optional.empty());
            //When y Then
            assertThatThrownBy(() -> vehicleService.update(1L, request, 999L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Estado no encontrado: 999");
            verify(vehicleRepo, never()).save(any());
        }

        @Test
        @DisplayName("Debe actualizar condición si viene en el request")
        void shouldUpdateConditionWhenProvided() {
            //Given
            Long vehicleId = 1L;
            VehicleCondition newCondition = TestDataBuilder.createVehicleCondition(2L, ConditionOptions.NO_APTO, "No Apto");

            VehicleUpdateRequest request = VehicleUpdateRequest.builder()
                    .conditionId(2L)
                    .build();

            when(vehicleRepo.findById(vehicleId)).thenReturn(Optional.of(mockVehicle));
            when(conditionRepo.findById(2L)).thenReturn(Optional.of(newCondition));
            when(vehicleRepo.save(any())).thenAnswer(i -> i.getArgument(0));

            // When
            vehicleService.update(vehicleId, request, 1L);

            // Then
            ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
            verify(vehicleRepo).save(captor.capture());
            assertThat(captor.getValue().getCondition()).isEqualTo(newCondition);
        }

        @Test
        @DisplayName("Debe lanzar NotFoundException si una condición a actualizar no existe")
        void shouldThrowNotFoundExceptionWhenVehicleConditionNotFound()
        {
            //Given
            VehicleUpdateRequest request = VehicleUpdateRequest.builder()
                    .conditionId(999L)
                    .build();

            when(vehicleRepo.findById(1L)).thenReturn(Optional.of(mockVehicle));
            when(conditionRepo.findById(999L)).thenReturn(Optional.empty());

            //When y Then
            assertThatThrownBy(() -> vehicleService.update(1L, request, 999L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Condición no encontrada: 999");

            verify(vehicleRepo, never()).save(any());
        }

        @Test
        @DisplayName("Debe actualizar vin si viene en el request")
        void shouldUpdateVinWhenProvided() {
            // Given
            Long vehicleId = 1L;

            VehicleUpdateRequest request = VehicleUpdateRequest.builder()
                    .vin("2HGBH41JXMN109187")
                    .build();

            when(vehicleRepo.findById(vehicleId)).thenReturn(Optional.of(mockVehicle));
            when(vehicleRepo.save(any())).thenAnswer(i -> i.getArgument(0));

            // When
            vehicleService.update(vehicleId, request, 1L);

            // Then
            ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
            verify(vehicleRepo).save(captor.capture());
            assertThat(captor.getValue().getVin()).isEqualTo("2HGBH41JXMN109187");
        }

        @Test
        @DisplayName("Debe actualizar color si viene en el request")
        void shouldUpdateColorWhenProvided() {
            // Given
            Long vehicleId = 1L;
            VehicleUpdateRequest request = VehicleUpdateRequest.builder()
                    .color("Rojo")
                    .build();

            when(vehicleRepo.findById(vehicleId)).thenReturn(Optional.of(mockVehicle));
            when(vehicleRepo.save(any())).thenAnswer(i -> i.getArgument(0));

            // When
            vehicleService.update(vehicleId, request, 1L);

            // Then
            ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
            verify(vehicleRepo).save(captor.capture());
            assertThat(captor.getValue().getColor()).isEqualTo("Rojo");
        }

        @Test
        @DisplayName("Debe actualizar currentOdometer si viene en el request")
        void shouldUpdateCurrentOdometerWhenProvided() {
            // Given
            Long vehicleId = 1L;
            VehicleUpdateRequest request = VehicleUpdateRequest.builder()
                    .currentOdometer(75000)
                    .build();

            when(vehicleRepo.findById(vehicleId)).thenReturn(Optional.of(mockVehicle));
            when(vehicleRepo.save(any())).thenAnswer(i -> i.getArgument(0));

            // When
            vehicleService.update(vehicleId, request, 1L);

            // Then
            ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
            verify(vehicleRepo).save(captor.capture());
            assertThat(captor.getValue().getCurrentOdometer()).isEqualTo(75000);
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si se intenta actualizar currentOdometer a un valor menor al actual")
        void shouldThrowBadRequestExceptionWhenUpdatingOdometerToLowerValue() {
            // Given
            Long vehicleId = 1L;
            VehicleUpdateRequest request = VehicleUpdateRequest.builder()
                    .currentOdometer(40000) // Menor al actual (50000)
                    .build();

            when(vehicleRepo.findById(vehicleId)).thenReturn(Optional.of(mockVehicle));

            // When & Then
            assertThatThrownBy(() -> vehicleService.update(vehicleId, request, 1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("El odómetro no puede ser menor al valor actual (" + mockVehicle.getCurrentOdometer() + " km)");

            verify(vehicleRepo, never()).save(any());
        }

        @Test
        @DisplayName("Debe actualizar soatExpirationDate si viene en el request")
        void shouldUpdateSoatExpirationDateWhenProvided() {
            // Given
            Long vehicleId = 1L;
            VehicleUpdateRequest request = VehicleUpdateRequest.builder()
                    .soatExpirationDate(LocalDate.now().plusMonths(12))
                    .build();

            when(vehicleRepo.findById(vehicleId)).thenReturn(Optional.of(mockVehicle));
            when(vehicleRepo.save(any())).thenAnswer(i -> i.getArgument(0));

            // When
            vehicleService.update(vehicleId, request, 1L);

            // Then
            ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
            verify(vehicleRepo).save(captor.capture());
            assertThat(captor.getValue().getSoatExpirationDate()).isEqualTo(request.soatExpirationDate());
        }

        @Test
        @DisplayName("Debe actualizar technicalInspectionExpirationDate si viene en el request")
        void shouldUpdateTechnicalInspectionExpirationDateWhenProvided() {
            // Given
            Long vehicleId = 1L;
            VehicleUpdateRequest request = VehicleUpdateRequest.builder()
                    .rtmExpirationDate(LocalDate.now().plusMonths(9))
                    .build();

            when(vehicleRepo.findById(vehicleId)).thenReturn(Optional.of(mockVehicle));
            when(vehicleRepo.save(any())).thenAnswer(i -> i.getArgument(0));
            // When
            vehicleService.update(vehicleId, request, 1L);
            // Then
            ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
            verify(vehicleRepo).save(captor.capture());
            assertThat(captor.getValue().getRtmExpirationDate()).isEqualTo(request.rtmExpirationDate());
        }

        @Test
        @DisplayName("Debe actualizar auditoría (updatedBy y updatedAt)")
        void shouldUpdateAuditFields() {
            // Given
            Long vehicleId = 1L;
            Long currentUserId = 999L;

            VehicleUpdateRequest request = VehicleUpdateRequest.builder().modelName("NewModel").build();

            when(vehicleRepo.findById(vehicleId)).thenReturn(Optional.of(mockVehicle));
            when(vehicleRepo.save(any())).thenAnswer(i -> i.getArgument(0));

            // When
            vehicleService.update(vehicleId, request, currentUserId);

            // Then
            ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
            verify(vehicleRepo).save(captor.capture());

            Vehicle saved = captor.getValue();
            assertThat(saved.getUpdatedByUserId()).isEqualTo(currentUserId);
            assertThat(saved.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Debe lanzar NotFoundException si el vehículo a actualizar no existe")
        void shouldThrowNotFoundExceptionWhenVehicleNotFound() {
            // Given
            Long vehicleId = 999L;

            VehicleUpdateRequest request = VehicleUpdateRequest.builder()
                    .modelName("NewModel")
                    .build();

            when(vehicleRepo.findById(vehicleId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> vehicleService.update(vehicleId, request, 1L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Vehículo no encontrado: 999");

            verify(vehicleRepo, never()).save(any());
        }

        @Test
        @DisplayName("Debe actualizar catálogos si vienen en el request")
        void shouldUpdateCatalogsWhenProvided() {
            // Given
            VehicleMake newMake = TestDataBuilder.createVehicleMake(2L, "Ford");
            VehicleUpdateRequest request = VehicleUpdateRequest.builder()
                    .makeId(2L)
                    .build();

            when(vehicleRepo.findById(1L)).thenReturn(Optional.of(mockVehicle));
            when(makeRepo.findById(2L)).thenReturn(Optional.of(newMake));
            when(vehicleRepo.save(any())).thenAnswer(i -> i.getArgument(0));

            // When
            vehicleService.update(1L, request, 1L);

            // Then
            ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
            verify(vehicleRepo).save(captor.capture());
            assertThat(captor.getValue().getMake()).isEqualTo(newMake);
        }

        @Test
        @DisplayName("Debe lanzar NotFoundException si una marca a actualizar no existe")
        void shouldThrowNotFoundExceptionWhenCatalogNotFound() {
            // Given
            VehicleUpdateRequest request = VehicleUpdateRequest.builder()
                    .makeId(999L)
                    .build();

            when(vehicleRepo.findById(1L)).thenReturn(Optional.of(mockVehicle));
            when(makeRepo.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> vehicleService.update(1L, request, 1L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Marca no encontrada: 999");

            verify(vehicleRepo, never()).save(any());
        }


    }

    // ==================== DEACTIVATE / ACTIVATE ====================

    @Nested
    @DisplayName("deactivate() y activate() - Soft delete")
    class DeactivateActivateTests {

        @Test
        @DisplayName("Debe desactivar vehículo (soft delete)")
        void shouldDeactivateVehicle() {
            // Given
            Long vehicleId = 1L;
            Long currentUserId = 1L;
            mockVehicle.setActive(true); // Inicialmente activo

            when(vehicleRepo.findById(vehicleId)).thenReturn(Optional.of(mockVehicle));
            when(vehicleRepo.save(any())).thenAnswer(i -> i.getArgument(0));

            // When
            vehicleService.deactivate(vehicleId, currentUserId);

            // Then
            ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
            verify(vehicleRepo).save(captor.capture());

            Vehicle saved = captor.getValue();
            assertThat(saved.isActive()).isFalse();
            assertThat(saved.getUpdatedByUserId()).isEqualTo(currentUserId);
        }

        @Test
        @DisplayName("Debe reactivar vehículo previamente desactivado")
        void shouldActivateVehicle() {
            // Given
            Long vehicleId = 1L;
            Long currentUserId = 1L;
            mockVehicle.setActive(false); // Inicialmente inactivo

            when(vehicleRepo.findById(vehicleId)).thenReturn(Optional.of(mockVehicle));
            when(vehicleRepo.save(any())).thenAnswer(i -> i.getArgument(0));

            // When
            vehicleService.activate(vehicleId, currentUserId);

            // Then
            ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
            verify(vehicleRepo).save(captor.capture());

            Vehicle saved = captor.getValue();
            assertThat(saved.isActive()).isTrue();
            assertThat(saved.getUpdatedByUserId()).isEqualTo(currentUserId);
        }

        @Test
        @DisplayName("Debe lanzar NotFoundException al desactivar vehículo inexistente")
        void shouldThrowNotFoundExceptionWhenDeactivatingNonExistent() {
            // Given
            when(vehicleRepo.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> vehicleService.deactivate(999L, 1L)).isInstanceOf(NotFoundException.class).hasMessageContaining("Vehículo no encontrado: 999");
        }
    }

    // ==================== LIST ====================

    @Nested
    @DisplayName("list() - Listar con paginación")
    class ListTests {

        @Test
        @DisplayName("Debe listar solo vehículos activos por defecto")
        void shouldListOnlyActiveVehiclesByDefault() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Vehicle vehicle1 = TestDataBuilder.createVehicle("ABC123");
            Vehicle vehicle2 = TestDataBuilder.createVehicle("DEF456");
            Page<Vehicle> page = new PageImpl<>(List.of(vehicle1, vehicle2));

            when(vehicleRepo.findByActive(true, pageable)).thenReturn(page);

            // When
            Page<VehicleDetailResponse> result = vehicleService.list(pageable, false);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);

            verify(vehicleRepo, times(1)).findByActive(true, pageable);
            verify(vehicleRepo, never()).findAll(pageable);
        }

        @Test
        @DisplayName("Debe listar todos los vehículos si includeInactive es true")
        void shouldListAllVehiclesWhenIncludeInactiveIsTrue() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Vehicle> page = new PageImpl<>(List.of(mockVehicle));

            when(vehicleRepo.findAll(pageable)).thenReturn(page);

            // When
            Page<VehicleDetailResponse> result = vehicleService.list(pageable, true);

            // Then
            assertThat(result).isNotNull();
            verify(vehicleRepo, times(1)).findAll(pageable);
            verify(vehicleRepo, never()).findByActive(anyBoolean(), any());
        }

        @Test
        @DisplayName("Debe usar Pageable.unpaged() si pageable es null")
        void shouldUseUnpagedWhenPageableIsNull() {
            // Given
            Page<Vehicle> page = new PageImpl<>(List.of(mockVehicle));
            when(vehicleRepo.findByActive(eq(true), any(Pageable.class))).thenReturn(page);

            // When
            vehicleService.list(null, false);

            // Then
            verify(vehicleRepo).findByActive(eq(true), any(Pageable.class));
        }
    }

    // ==================== FIND EXPIRING ====================

    @Nested
    @DisplayName("findExpiringByDate() - Vehículos próximos a vencer")
    class FindExpiringTests {

        @Test
        @DisplayName("Debe retornar vehículos con SOAT o RTM próximos a vencer")
        void shouldFindExpiringVehicles() {
            // Given
            int days = 30;
            Vehicle expiringSoon = TestDataBuilder.createVehicle();
            expiringSoon.setSoatExpirationDate(LocalDate.now().plusDays(15));

            LocalDate limit = LocalDate.now().plusDays(days);
            when(vehicleRepo.findExpiringByDate(limit)).thenReturn(List.of(expiringSoon));

            // When
            var result = vehicleService.findExpiringByDate(days);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).daysToSoat()).isNotNull();
            assertThat(result.get(0).daysToSoat()).isLessThanOrEqualTo(30L);

            verify(vehicleRepo).findExpiringByDate(limit);
        }
    }
}
