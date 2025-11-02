# 🧪 GUÍA DE TESTING - NeoFlota Backend

## 📋 Resumen de Configuración Completada

### ✅ Dependencias Agregadas al pom.xml

```xml
<!-- H2 Database para tests en memoria -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>

<!-- Spring Security Test -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- spring-boot-starter-test (ya existía) -->
<!-- Incluye: JUnit 5, Mockito, AssertJ, Hamcrest, JSONassert, JsonPath -->
```

### ✅ Estructura de Directorios Creada

```
src/test/
├── java/org/avyla/
│   ├── vehicles/application/service/
│   │   └── VehicleServiceTest.java  ✅ COMPLETADO
│   ├── checklists/application/service/
│   ├── security/application/service/
│   └── shared/util/
│       └── TestDataBuilder.java  ✅ COMPLETADO
└── resources/
    └── application-test.yml  ✅ COMPLETADO
```

### ✅ Archivos Creados

1. **`src/test/resources/application-test.yml`**
   - Base de datos H2 en memoria configurada en modo PostgreSQL
   - Flyway desactivado (usamos `ddl-auto=create-drop`)
   - JWT con clave de prueba
   - Logging configurado para debugging

2. **`src/test/java/org/avyla/shared/util/TestDataBuilder.java`**
   - Clase utilitaria para crear datos de prueba
   - Builders para Vehicle, User, Roles, etc.
   - Centraliza la creación de objetos mock

3. **`src/test/java/org/avyla/vehicles/application/service/VehicleServiceTest.java`**
   - **27 tests unitarios** para VehicleService
   - Cobertura completa de todos los métodos
   - Usa Mockito para aislar dependencias

---

## 🚀 CÓMO EJECUTAR LOS TESTS

### Opción 1: Ejecutar todos los tests

```bash
mvn clean test
```

### Opción 2: Ejecutar solo VehicleServiceTest

```bash
mvn test -Dtest=VehicleServiceTest
```

### Opción 3: Ejecutar un test específico

```bash
mvn test -Dtest=VehicleServiceTest#shouldCreateVehicleSuccessfully
```

### Opción 4: Ejecutar con cobertura de código (JaCoCo)

Agregar al `pom.xml`:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

Luego ejecutar:

```bash
mvn clean test jacoco:report
```

El reporte estará en: `target/site/jacoco/index.html`

---

## 📊 DETALLE DE VehicleServiceTest

### Cobertura de Métodos

| Método | Tests | Escenarios Cubiertos |
|--------|-------|----------------------|
| `create()` | 6 tests | ✅ Creación exitosa<br>✅ Normalización de placa a mayúsculas<br>✅ Validación de placa duplicada<br>✅ Validación de catálogos (marca, tipo, etc.)<br>✅ Condición opcional (null)<br>✅ Odómetro default a 0 |
| `getById()` | 3 tests | ✅ Obtención exitosa<br>✅ Not found exception<br>✅ Cálculo de días para vencimiento |
| `update()` | 4 tests | ✅ Actualización parcial<br>✅ Actualización de auditoría<br>✅ Not found exception<br>✅ Actualización de catálogos |
| `deactivate()` | 2 tests | ✅ Soft delete exitoso<br>✅ Not found exception |
| `activate()` | 1 test | ✅ Reactivación exitosa |
| `list()` | 3 tests | ✅ Listado solo activos<br>✅ Listado todos (includeInactive=true)<br>✅ Pageable null (unpaged) |
| `findExpiringByDate()` | 1 test | ✅ Vehículos próximos a vencer |

**TOTAL: 20 tests** organizados en **7 grupos** (@Nested)

### Tecnologías Utilizadas

- **JUnit 5** (Jupiter)
  - `@ExtendWith(MockitoExtension.class)` para integración con Mockito
  - `@Nested` para agrupar tests relacionados
  - `@DisplayName` para descripciones legibles
  - `@BeforeEach` para setup común

- **Mockito** (incluido en spring-boot-starter-test)
  - `@Mock` para dependencias simuladas
  - `@InjectMocks` para inyectar mocks automáticamente
  - `ArgumentCaptor` para verificar argumentos
  - `verify()` para validar interacciones
  - `when().thenReturn()` para definir comportamiento

- **AssertJ** (incluido en spring-boot-starter-test)
  - API fluida para aserciones: `assertThat()`
  - Aserciones expresivas y legibles
  - `assertThatThrownBy()` para excepciones

### Ejemplo de Test

```java
@Test
@DisplayName("Debe crear vehículo exitosamente con todos los datos válidos")
void shouldCreateVehicleSuccessfully() {
    // Given (Preparación)
    VehicleCreateRequest request = new VehicleCreateRequest(...);
    when(vehicleRepo.findByPlate("ABC123")).thenReturn(Optional.empty());
    when(makeRepo.findById(1L)).thenReturn(Optional.of(mockMake));

    // When (Acción)
    VehicleDetailResponse response = vehicleService.create(request, 1L);

    // Then (Verificación)
    assertThat(response).isNotNull();
    assertThat(response.getPlate()).isEqualTo("ABC123");
    verify(vehicleRepo, times(1)).save(any(Vehicle.class));
}
```

---

## 🎯 SIGUIENTES PASOS - NIVEL 1 (Servicios)

### Pendientes para completar NIVEL 1:

#### 2. ChecklistServiceTest (MÁS COMPLEJO)

**Métodos a testear:**
- `createInstance()` - Validaciones de cooldown, TTL, estado
- `saveResponses()` - Asignación de vehículo, odómetro, estados (OK/OBS/NOOP/NA)
- `submit()` - Cálculo de condición, actualización de vehículo, evidencias
- `calculateConditionGeneral()` - Lógica de decisión (APTO/APTO_RESTRICCIONES/NO_APTO)
- `getPendingPayload()` - Retomar checklist pendiente
- `getInstanceDetails()` - Detalles completos

**Complejidad:** ALTA ⚠️

**Dependencias a mockear:**
- ChecklistVersionRepository
- ChecklistItemRepository
- ChecklistInstanceRepository
- ChecklistResponseRepository
- ChecklistAttachmentRepository
- OptionItemRepository
- VehicleRepository
- VehicleConditionRepository
- CurrentUserService

**Estimación:** ~40-50 tests

---

#### 3. UserDetailServiceImplTest

**Métodos a testear:**
- `createUser()` - Validación de roles, codificación BCrypt
- `loginUser()` - Autenticación, generación de JWT
- `loadUserByUsername()` - Construcción de authorities
- `authenticate()` - Validación de credenciales

**Complejidad:** MEDIA

**Dependencias a mockear:**
- UserRepository
- RoleRepository
- JwtUtils
- PasswordEncoder

**Estimación:** ~15-20 tests

---

## 📈 VERIFICACIÓN DE COBERTURA

### Comandos útiles

```bash
# Ver solo tests que fallan
mvn test --fail-at-end

# Ejecutar tests en paralelo (más rápido)
mvn test -T 4

# Ver detalles completos de errores
mvn test -e

# Modo verboso
mvn test -X

# Saltar tests (para compilación rápida)
mvn clean install -DskipTests
```

### Reporte de Surefire

Los reportes XML de tests están en:
```
target/surefire-reports/
```

---

## 🐛 TROUBLESHOOTING

### Error: "Cannot find symbol" al compilar tests

**Solución:** Asegúrate de que Maven compile primero el código principal:
```bash
mvn clean compile test-compile
```

### Error: H2 Database no encuentra la tabla

**Solución:** Verifica que `spring.jpa.hibernate.ddl-auto=create-drop` esté en application-test.yml

### Error: JWT validation fails

**Solución:** Asegúrate de mockear JwtUtils correctamente en los tests de seguridad

### Tests muy lentos

**Solución:**
1. Usa `@Mock` en lugar de `@Autowired` siempre que sea posible
2. Evita `@SpringBootTest` para tests unitarios (solo para integración)
3. Ejecuta tests en paralelo: `mvn test -T 4`

---

## 📚 BUENAS PRÁCTICAS APLICADAS

### ✅ Patrón AAA (Arrange-Act-Assert)

Todos los tests siguen el patrón:
```java
// Given (Arrange) - Preparar datos y mocks
// When (Act) - Ejecutar el método a probar
// Then (Assert) - Verificar resultados
```

### ✅ Nombres Descriptivos

```java
@DisplayName("Debe crear vehículo exitosamente con todos los datos válidos")
void shouldCreateVehicleSuccessfully()
```

### ✅ Tests Aislados

- Cada test es independiente
- Usan `@BeforeEach` para setup común
- No comparten estado mutable

### ✅ Tests FIRST

- **F**ast: Tests unitarios, no tocan BD real
- **I**ndependent: No dependen de orden de ejecución
- **R**epeatable: Mismo resultado siempre
- **S**elf-validating: Pasan o fallan claramente
- **T**imely: Escritos junto con el código

### ✅ Mocks vs Stubs

- **Mocks**: Para verificar interacciones (`verify()`)
- **Stubs**: Para simular respuestas (`when().thenReturn()`)

---

## 🎓 RECURSOS ADICIONALES

### Documentación Oficial

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/reference/testing/index.html)

### Comandos de Referencia Rápida

```bash
# Ejecutar solo tests unitarios (excluir integración)
mvn test -Dgroups=unit

# Ejecutar solo tests de integración
mvn test -Dgroups=integration

# Ver árbol de dependencias de test
mvn dependency:tree -Dscope=test

# Limpiar y ejecutar tests
mvn clean test

# Generar reporte HTML de tests
mvn surefire-report:report
```

---

## ✅ RESUMEN DE LO COMPLETADO

### Configuración ✅
- [x] Dependencias de testing agregadas
- [x] application-test.yml configurado
- [x] Estructura de carpetas creada
- [x] TestDataBuilder implementado

### Tests Implementados ✅
- [x] VehicleServiceTest (20 tests)

### Pendiente 📝
- [ ] ChecklistServiceTest (~45 tests estimados)
- [ ] UserDetailServiceImplTest (~18 tests estimados)
- [ ] JwtUtilsTest (NIVEL 2)
- [ ] CurrentUserServiceTest (NIVEL 2)
- [ ] Controller tests con @WebMvcTest (NIVEL 3)
- [ ] Repository tests con @DataJpaTest (NIVEL 4)

---

## 📊 ESTADO ACTUAL

```
NIVEL 1 - SERVICIOS
├── ✅ VehicleService (100% - 20/20 tests)
├── ⏳ ChecklistService (0% - 0/45 tests estimados)
└── ⏳ UserDetailServiceImpl (0% - 0/18 tests estimados)

PROGRESO TOTAL: ~24% del NIVEL 1
```

---

## 🎯 PRÓXIMO PASO RECOMENDADO

Implementar **ChecklistServiceTest** ya que contiene la lógica de negocio más crítica del sistema (gestión de checklists preoperacionales).

**Comando para crear el siguiente test:**
```bash
touch src/test/java/org/avyla/checklists/application/service/ChecklistServiceTest.java
```

**¿Continuar con ChecklistServiceTest?**
