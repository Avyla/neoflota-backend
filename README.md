# NeoFlota Backend – Guía de Integración Frontend

Documentación de endpoints REST para integración con el frontend.

**Backend**: Spring Boot | **Base URL**: `http://localhost:8080`

---

## 📚 Índice

1. [Quick Start](#quick-start)
2. [Módulo de Vehículos](#módulo-de-vehículos)
3. [Módulo de Checklists](#módulo-de-checklists)
4. [Flujos End-to-End](#flujos-end-to-end)

---

## Quick Start

### Base URL y Autenticación

* **Base URL**: `http://localhost:8080`
* **Autenticación**: Header `Authorization: Bearer {{token}}`
* **Content-Type**: `application/json` (excepto uploads: `multipart/form-data`)

### Variables de Postman

```json
{
  "baseUrl": "http://localhost:8080",
  "token": "tu_token_aqui"
}
```

---

## Módulo de Vehículos

### 1) Obtener Catálogos (Metadata Publicada)

#### GET `/api/vehicles/published`

Retorna todos los catálogos necesarios para construir formularios de vehículos (marcas, tipos, categorías, combustibles, estados, condiciones) y reglas de validación.

**Headers**:

```
Authorization: Bearer {{token}}
```

**Respuesta** (200 OK):

```json
{
  "catalogs": {
    "makes": [
      { "id": 1, "name": "Toyota" },
      { "id": 2, "name": "Chevrolet" },
      { "id": 3, "name": "Mazda" }
    ],
    "types": [
      { "id": 1, "name": "Automóvil" },
      { "id": 2, "name": "Camioneta" },
      { "id": 3, "name": "Camión" }
    ],
    "categories": [
      { "id": 1, "name": "Particular" },
      { "id": 2, "name": "Público" },
      { "id": 3, "name": "Oficial" }
    ],
    "fuelTypes": [
      { "id": 1, "name": "Gasolina" },
      { "id": 2, "name": "Diesel" },
      { "id": 3, "name": "Eléctrico" },
      { "id": 4, "name": "Híbrido" }
    ],
    "statuses": [
      { "id": 1, "code": "ACTIVE", "name": "Activo", "description": "Vehículo en operación" },
      { "id": 2, "code": "MAINTENANCE", "name": "Mantenimiento", "description": "En taller" },
      { "id": 3, "code": "OUT_OF_SERVICE", "name": "Fuera de servicio", "description": "No operativo" }
    ],
    "conditions": [
      { "id": 1, "code": "EXCELLENT", "name": "Excelente", "order": 1 },
      { "id": 2, "code": "GOOD", "name": "Bueno", "order": 2 },
      { "id": 3, "code": "FAIR", "name": "Regular", "order": 3 },
      { "id": 4, "code": "POOR", "name": "Malo", "order": 4 }
    ]
  },
  "validationRules": {
    "plate": {
      "pattern": "^(?:[A-Z]{3}[0-9]{3}|[A-Z]{3}[0-9]{2}[A-Z])$",
      "format": "ABC123 o ABC12D",
      "description": "3 letras seguidas de 3 números, o 3 letras, 2 números y 1 letra",
      "examples": ["ABC123", "XYZ45D"]
    },
    "modelYear": { "min": 1950, "max": 2099 },
    "odometer": { "min": 0, "unit": "km" },
    "requiredFields": ["plate", "makeId", "modelName", "typeId", "categoryId", "fuelTypeId", "statusId"]
  },
  "version": "abc123def456"
}
```

**Caché**: Incluye ETag y Cache-Control (1 hora). Frontend puede usar `If-None-Match` para caché condicional.

---

### 2) Crear Vehículo

#### POST `/api/vehicles`

**Body** (ejemplo con todos los campos):

```json
{
  "plate": "ABC123",
  "makeId": 1,
  "modelName": "Corolla",
  "modelYear": 2020,
  "typeId": 1,
  "categoryId": 1,
  "fuelTypeId": 1,
  "statusId": 1,
  "conditionId": 2,
  "vin": "1HGBH41JXMN109186",
  "color": "Blanco",
  "currentOdometer": 45000,
  "soatExpirationDate": "2025-12-31",
  "rtmExpirationDate": "2025-11-30"
}
```

**Campos obligatorios**: `plate`, `makeId`, `modelName`, `typeId`, `categoryId`, `fuelTypeId`, `statusId`

**Campos opcionales**: `modelYear`, `conditionId`, `vin`, `color`, `currentOdometer`, `soatExpirationDate`, `rtmExpirationDate`

**Respuesta** (201 Created):

```json
{
  "id": 2001,
  "plate": "ABC123",
  "makeId": 1,
  "makeName": "Toyota",
  "modelName": "Corolla",
  "modelYear": 2020,
  "typeId": 1,
  "typeName": "Automóvil",
  "categoryId": 1,
  "categoryName": "Particular",
  "fuelTypeId": 1,
  "fuelTypeName": "Gasolina",
  "statusId": 1,
  "statusCode": "ACTIVE",
  "statusName": "Activo",
  "conditionId": 2,
  "conditionCode": "GOOD",
  "conditionName": "Bueno",
  "vin": "1HGBH41JXMN109186",
  "color": "Blanco",
  "currentOdometer": 45000,
  "soatExpirationDate": "2025-12-31",
  "rtmExpirationDate": "2025-11-30",
  "daysToSoatExpiration": 68,
  "daysToRtmExpiration": 37,
  "active": true,
  "createdByUserId": 1,
  "createdAt": "2025-10-26T10:30:00Z",
  "updatedByUserId": null,
  "updatedAt": null
}
```

**Validaciones**:

* Placa: `ABC123` o `ABC12D` (sin guion, mayúsculas)
* Año: entre 1950 y 2099
* Odómetro: mayor o igual a 0

**Errores comunes**:

* `400` - Placa inválida, año fuera de rango, campos requeridos faltantes
* `409` - Placa duplicada

---

### 3) Listar Vehículos (Paginado)

#### GET `/api/vehicles?page=0&size=20&sort=plate,asc&includeInactive=false`

**Parámetros**:

* `page`: número de página (default: 0)
* `size`: tamaño de página (default: 20)
* `sort`: ordenamiento (default: `plate,asc`)
* `includeInactive`: incluir inactivos (default: `false`)

**Ejemplo**: `GET /api/vehicles?page=0&size=10&sort=modelYear,desc`

**Respuesta** (200 OK):

```json
{
  "content": [
    {
      "id": 2001,
      "plate": "ABC123",
      "makeName": "Toyota",
      "modelName": "Corolla",
      "modelYear": 2020,
      "statusCode": "ACTIVE",
      "conditionCode": "GOOD",
      "daysToSoatExpiration": 68,
      "daysToRtmExpiration": 37
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 45,
  "totalPages": 3,
  "last": false
}
```

---

### 4) Obtener Vehículo por ID

#### GET `/api/vehicles/{id}`

**Ejemplo**: `GET /api/vehicles/2001`

**Respuesta** (200 OK): igual estructura que respuesta de crear vehículo

**Errores**:

* `404` - Vehículo no encontrado

---

### 5) Actualizar Vehículo

#### PUT `/api/vehicles/{id}`

**Body**: misma estructura que crear vehículo (todos los campos obligatorios deben enviarse)

**Respuesta** (200 OK): vehículo actualizado

**Errores**:

* `400` - Validaciones fallidas
* `404` - Vehículo no encontrado
* `409` - Placa duplicada (si se cambió)

---

### 6) Desactivar/Reactivar Vehículo

#### Desactivar (soft delete)

**DELETE** `/api/vehicles/{id}`

**Respuesta**: `204 No Content`

#### Reactivar

**PATCH** `/api/vehicles/{id}/activate`

**Respuesta**: `204 No Content`

---

### 7) Vehículos Próximos a Vencer

#### GET `/api/vehicles?vencenEn=30`

Obtiene vehículos cuyo SOAT o RTM vence en los próximos N días.

**Ejemplo**: `GET /api/vehicles?vencenEn=7` (próximos 7 días)

**Respuesta** (200 OK):

```json
[
  {
    "id": 2001,
    "plate": "ABC123",
    "make": "Toyota",
    "model": "Corolla",
    "statusCode": "ACTIVE",
    "conditionCode": "GOOD",
    "soatExpirationDate": "2025-12-31",
    "rtmExpirationDate": "2025-11-05",
    "daysToSoat": 68,
    "daysToRtm": 5
  }
]
```

**Uso**: ideal para dashboards de alertas. Mostrar badge rojo si `daysToSoat < 7` o `daysToRtm < 7`.

---

### 8) Subir Documentos (SOAT/RTM)

#### POST `/api/vehicles/{id}/documents`

**Content-Type**: `multipart/form-data`

**Form Data**:

```
docType: SOAT
issuer: Seguros ABC
issuedAt: 2025-01-15
expirationDate: 2025-12-31
file: (archivo PDF/JPG/PNG, máx 5MB)
```

**Campos**:

* `docType`: `SOAT` o `RTM` (obligatorio)
* `issuer`: emisor del documento (opcional)
* `issuedAt`: fecha de emisión (opcional, formato: `YYYY-MM-DD`)
* `expirationDate`: fecha de vencimiento (opcional, formato: `YYYY-MM-DD`)
* `file`: archivo (obligatorio, máx 5MB)

**Formatos aceptados**: JPG, PNG, PDF

**Respuesta** (200 OK):

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "vehicleId": 2001,
  "docType": "SOAT",
  "filename": "soat_abc123.pdf",
  "mimeType": "application/pdf",
  "size": 245678,
  "issuer": "Seguros ABC",
  "issuedAt": "2025-01-15",
  "expirationDate": "2025-12-31",
  "uploadedAt": "2025-10-26T10:45:00Z",
  "uploadedByUserId": 1
}
```

**Errores**:

* `400` - Tipo de archivo no permitido, archivo muy grande
* `404` - Vehículo no encontrado

---

### 9) Listar Documentos

#### GET `/api/vehicles/{vehicleId}/documents?docType=SOAT`

**Parámetros**:

* `docType`: `SOAT` o `RTM` (obligatorio)

**Ejemplo**: `GET /api/vehicles/2001/documents?docType=SOAT`

**Respuesta** (200 OK):

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "docType": "SOAT",
    "filename": "soat_abc123.pdf",
    "expirationDate": "2025-12-31",
    "uploadedAt": "2025-10-26T10:45:00Z"
  }
]
```

---

### 10) Descargar Documento

#### GET `/api/vehicles/{id}/documents/{docId}`

**Ejemplo**: `GET /api/vehicles/2001/documents/550e8400-e29b-41d4-a716-446655440000`

**Respuesta**: archivo binario con headers:

* `Content-Disposition: attachment; filename="soat_abc123.pdf"`
* `Content-Type: application/pdf`
* `Content-Length: 245678`

**Errores**:

* `404` - Documento no encontrado o no pertenece al vehículo especificado

---

## Módulo de Checklists

Esta guía documenta **los endpoints reales** del backend (Spring Boot) para el **módulo de Checklists**, con ejemplos y reglas de negocio para que Frontend pueda simular **situaciones reales** en Postman/HTTP.

**Template publicado** soportado en este build:

* `CHK_PREOP_VEH_GEN` (Checklist Pre-operacional – Vehículo, versión `1.1`, estado `Published`).

**Estados y catálogos clave**:

* Estado de respuesta (`ResponseState`): `OK | OBS | NOOP | NA` (JSON en mayúsculas).
* Severidad (`SeverityOptions`): `LOW | MEDIUM | HIGH | CRITICAL`.
* Estado de instancia (`InstanceStatus`): `PENDING | IN_PROGRESS | SUBMITTED | APPROVED | REJECTED | EXPIRED`.

---

### 1) Plantillas publicadas

#### GET `/api/checklists/templates/{templateCode}/versions/published`

Devuelve el **diseño publicado** (secciones, ítems, catálogos) con `ETag (versionHash)` y `Last-Modified (publishedAt)` para cache condicional.

**Path vars**:

* `templateCode`: usa `CHK_PREOP_VEH_GEN`.

**Respuesta (campos principales)**:

* `templateCode`, `versionId`, `versionLabel`, `publishedAt`, `versionHash`
* `sections[]`: `code`, `title`, `order`, `items[]` (ver tabla abajo)
* `optionGroups`: mapa `groupCode -> [{ code, label, order }]`

**Tabla de ítems (plantilla v1.1)**:

| Sección                         | Item Code             | Título                                       | Severidad | Req | NA | Detalle (Grupo)   |
| ------------------------------- | --------------------- | -------------------------------------------- | --------- | --: | -: | ----------------- |
| Presentación y confort          | CONF_ASEO             | Aseo y presentación (int/ext)                | LOW       |     |    |                   |
| Presentación y confort          | CONF_CLIMA            | Climatización (A/C y ventilación)            | LOW       |     |    |                   |
| Presentación y confort          | CONF_CABINA           | Cabina (sillas y luces internas)             | LOW       |     |    |                   |
| Fluidos                         | FLU_ACEITE_MOTOR      | Aceite de motor                              | HIGH      |  ✔️ |    |                   |
| Fluidos                         | FLU_LIQ_FRENOS        | Líquido de frenos                            | CRITICAL  |  ✔️ |    |                   |
| Fluidos                         | FLU_REFRIGERANTE      | Refrigerante (radiador)                      | HIGH      |  ✔️ |    |                   |
| Fluidos                         | FLU_OTROS             | Otros fluidos                                | MEDIUM    |     |    | OtherFluids       |
| Luces                           | LUZ_EXTERNAS          | Luces externas (conjunto)                    | HIGH      |  ✔️ |    | ExternalLights    |
| Luces                           | LUZ_FRENO             | Luz de freno                                 | CRITICAL  |  ✔️ |    |                   |
| Tablero e instrumentos          | TAB_INSTRUMENTOS      | Instrumentos / indicadores                   | HIGH      |  ✔️ |    | InstrumentFaults  |
| Tablero e instrumentos          | TAB_PITO              | Pito (bocina)                                | CRITICAL  |  ✔️ |    |                   |
| Seguridad activa/pasiva         | SEG_AIRBAGS           | Airbags (si aplica)                          | HIGH      |     | ✔️ |                   |
| Seguridad activa/pasiva         | SEG_CINTURONES        | Cinturones de seguridad                      | CRITICAL  |  ✔️ |    | SeatbeltPositions |
| Seguridad activa/pasiva         | SEG_DIRECCION         | Dirección                                    | CRITICAL  |  ✔️ |    |                   |
| Seguridad activa/pasiva         | SEG_ESPEJOS_CRISTALES | Espejos y cristales (visibilidad)            | MEDIUM    |  ✔️ |    |                   |
| Seguridad activa/pasiva         | SEG_LIMPIA            | Limpiaparabrisas (plumillas + lava)          | MEDIUM    |  ✔️ |    |                   |
| Seguridad activa/pasiva         | SEG_SUSPENSION        | Suspensión                                   | HIGH      |  ✔️ |    | SuspensionAreas   |
| Rodadura y frenos               | ROD_FRENO_MANO        | Freno de mano                                | CRITICAL  |  ✔️ |    |                   |
| Rodadura y frenos               | ROD_FRENOS_SISTEMA    | Frenos (sistema)                             | CRITICAL  |  ✔️ |    |                   |
| Rodadura y frenos               | ROD_LLANTAS           | Llantas (estado general)                     | HIGH      |  ✔️ |    | WheelPositions    |
| Rodadura y frenos               | ROD_RINES             | Rines (deformación/daño)                     | MEDIUM    |  ✔️ |    | WheelPositions    |
| Equipo reglamentario y botiquín | REG_BOTIQUIN          | Botiquín vehicular                           | MEDIUM    |     |    | FirstAidItems     |
| Equipo reglamentario y botiquín | REG_EQUIPO            | Equipo reglamentario                         | HIGH      |  ✔️ |    | RegulatoryItems   |
| Equipo reglamentario y botiquín | REG_EXTINTOR          | Extintor (presencia/vigencia)                | CRITICAL  |  ✔️ |    |                   |
| Otros componentes               | OTR_ALARMA_REVERSA    | Alarma de reversa (si aplica)                | MEDIUM    |     | ✔️ |                   |
| Otros componentes               | OTR_ELECTRICO         | Sistema eléctrico (general)                  | HIGH      |  ✔️ |    |                   |
| Otros componentes               | OTR_EXOSTO            | Escape (exosto)                              | MEDIUM    |     |    |                   |
| Otros componentes               | OTR_PLACAS            | Placas (legibilidad/presencia)               | HIGH      |  ✔️ |    |                   |
| Otros componentes               | OTR_TREN_MOTRIZ       | Tren motriz (transmisión/embrague/encendido) | HIGH      |  ✔️ |    |                   |

**Catálogos de detalles disponibles (optionGroups)**:

| Grupo             | Código         | Label                    |
| ----------------- | -------------- | ------------------------ |
| EstadoGeneral     | OK             | OK                       |
| EstadoGeneral     | OBS            | Observación              |
| EstadoGeneral     | NOOP           | No operativo             |
| EstadoGeneral     | NA             | N/A                      |
| ExternalLights    | BAJAS          | Bajas                    |
| ExternalLights    | MEDIAS_ALTAS   | Medias/Altas             |
| ExternalLights    | DIR_DEL        | Direccionales delanteras |
| ExternalLights    | DIR_TRAS       | Direccionales traseras   |
| ExternalLights    | PARQUEO        | Parqueo                  |
| ExternalLights    | EXPLORADORAS   | Exploradoras/Antiniebla  |
| ExternalLights    | REVERSA        | Reversa                  |
| FirstAidItems     | ALCOHOL        | Alcohol antiséptico      |
| FirstAidItems     | BAJALENGUAS    | Depresores linguales     |
| FirstAidItems     | ESPARADRAPO    | Esparadrapo              |
| FirstAidItems     | GASAS          | Gasas estériles          |
| FirstAidItems     | VENDAJES       | Vendajes                 |
| FirstAidItems     | CURAS          | Curas                    |
| FirstAidItems     | AGUA           | Agua potable             |
| InstrumentFaults  | VELOCIMETRO    | Velocímetro              |
| InstrumentFaults  | TACOMETRO      | Tacómetro                |
| InstrumentFaults  | ACEITE         | Indicador de aceite      |
| InstrumentFaults  | TEMPERATURA    | Indicador de temperatura |
| InstrumentFaults  | COMBUSTIBLE    | Nivel de combustible     |
| OtherFluids       | HIDRAULICO     | Aceite hidráulico        |
| OtherFluids       | AGUA_PLUMILLAS | Agua para plumillas      |
| OtherFluids       | BATERIA        | Agua de batería          |
| RegulatoryItems   | CRUCETA        | Cruceta/Copa             |
| RegulatoryItems   | TACOS          | 2 tacos de bloqueo       |
| RegulatoryItems   | SENALES        | 2 señales de carretera   |
| RegulatoryItems   | LINTERNA       | Linterna                 |
| RegulatoryItems   | HERRAMIENTAS   | Caja de herramientas     |
| RegulatoryItems   | CHALECO        | Chaleco reflectivo       |
| RegulatoryItems   | GUANTES        | Guantes de vaqueta       |
| SeatbeltPositions | PILOTO         | Piloto                   |
| SeatbeltPositions | COPILOTO       | Copiloto                 |
| SeatbeltPositions | TRASEROS       | Traseros                 |
| SuspensionAreas   | DEL            | Delantera                |
| SuspensionAreas   | TRAS           | Trasera                  |
| WheelPositions    | DEL_IZQ        | Delantera izquierda      |
| WheelPositions    | DEL_DER        | Delantera derecha        |
| WheelPositions    | TRAS_IZQ       | Trasera izquierda        |
| WheelPositions    | TRAS_DER       | Trasera derecha          |
| WheelPositions    | EJE2_IZQ       | Eje 2 izquierda          |
| WheelPositions    | EJE2_DER       | Eje 2 derecha            |

---

### 2) Instancias

#### POST `/api/checklists/instances?templateCode={code}&driverId={id}`

Crea una **instancia** para el conductor con TTL/cooldown configurado.

**Respuesta**: `{ instanceId, status, startedAt, dueAt }`

#### GET `/api/checklists/drivers/{driverId}/instances/pending/payload`

Devuelve la instancia abierta (si existe) + tiempo restante y progreso:

`{ instanceId, status, startedAt, dueAt, timeRemainingSec, responses[] }`

#### GET `/api/checklists/instances/{id}/details`

Devuelve el **detalle** completo incluyendo resumen:

* `responses[]`: `{ id, itemCode, state, comment, details[], attachments[] }`
* `summary`: `{ total, okCount, oobCount, noopCount, criticalNoopCount, overall }`
  con `overall ∈ { "APTO", "APTO_RESTRICCIONES", "NO_APTO" }`

---

### 3) Guardar respuestas (y asignación tardía de vehículo/odómetro)

#### POST `/api/checklists/instances/{id}/responses`

**Body (JSON)**

```json
{
  "vehicleId": 2001,               // opcional (permitido solo la PRIMERA vez)
  "odometer": 124000,              // obligatorio si se asigna vehicleId por primera vez
  "responses": [
    {
      "itemCode": "ROD_LLANTAS",
      "state": "OK",
      "comment": "Estado normal",
      "details": ["DEL_IZQ"]      // opcional (este ítem tiene catálogo WheelPositions)
    },
    {
      "itemCode": "ROD_RINES",
      "state": "OBS",
      "comment": "Fisura leve en el rin delantero",
      "details": ["DEL_IZQ", "DEL_DER"]  // OBLIGATORIO: tiene catálogo y state != OK
    },
    {
      "itemCode": "FLU_LIQ_FRENOS",
      "state": "NOOP",
      "comment": "Fuga masiva de líquido de frenos",
      "details": []               // sin catálogo: no aplica detalles
    }
  ]
}
```

**Reglas de validación (backend)**

* `NA` solo permitido si el ítem tiene `allowNA = true` (p. ej., `SEG_AIRBAGS`).
* Si `state ∈ {OBS, NOOP}`:

    * **Comentario obligatorio** (≥ 5 chars).
    * Si el ítem define `detailCatalog` ⇒ **al menos 1 `detail`**.
* **Asignación de vehículo**:

    * Solo **la primera vez** (si ya hay `vehicleId`, un cambio ⇒ **409**).
    * Si llega `vehicleId` por primera vez ⇒ **odometer es obligatorio**.
    * El odómetro no puede retroceder (regla en servicio/vehículo).

**Códigos de error típicos**

* `400/422` → NA inválido, falta comentario, faltan detalles, formatos incorrectos.
* `409` → Reasignación de vehículo a una instancia que ya tenía vehículo.

---

### 4) Evidencias (Adjuntos)

#### POST `/api/checklists/responses/{responseId}/attachments` (multipart)

* `file`: `jpg|png|pdf` (**5MB** máx).
* **1 evidencia por respuesta** (si ya existe ⇒ `409`).
* Detección de MIME por **magic number** + fallback.

**Respuesta**: `{ id (UUID), filename, type, size, url }`

#### POST `/api/checklists/instances/{instanceId}/attachments` (multipart)

Adjuntos **generales** para la instancia (sin límite de cantidad).

#### GET `/api/attachments/{id}`

Descarga el archivo. Devuelve cabeceras `Content-Disposition` y `ETag` (por tamaño).

#### DELETE `/api/attachments/{id}`

Elimina el adjunto. (Recomendado bloquear en UI tras `SUBMITTED`).

---

### 5) Submit (cierre de checklist)

#### POST `/api/checklists/instances/{id}/submit`

Valida y sella la instancia:

* Si hay **ítems CRITICAL en NOOP** ⇒ **evidencia obligatoria** por respuesta.
* Marca `completedAt = now`, `status = SUBMITTED`.
* Si hay vehículo, actualiza `vehicle.currentOdometer`.

**Códigos de resultado**

* `200/201`: ok
* `400/422`: faltan evidencias para NOOP crítico, faltan respuestas requeridas
* `410`: instancia expirada (`dueAt` vencido)

---

### 6) Recetas de prueba (end-to-end)

1. **APTO**

    * Guardar todo en `OK` (o `NA` válido donde aplique).
    * `submit` ⇒ `overall = APTO`.

2. **APTO_RESTRICCIONES**

    * Poner uno o más ítems en `OBS` (con comentario + detalles si aplica).
    * `submit` ⇒ `overall = APTO_RESTRICCIONES`.

3. **NO_APTO (con validación de evidencia)**

    * Poner `FLU_LIQ_FRENOS` o `ROD_FRENOS_SISTEMA` en `NOOP`.
    * `submit` (debe **fallar** por falta de evidencia).
    * Subir evidencia en esa respuesta.
    * `submit` ⇒ `overall = NO_APTO`.

4. **Errores esperables**

    * `NA` inválido p.ej. en `FLU_LIQ_FRENOS` ⇒ `400/422`.
    * `OBS` sin `details` en `ROD_RINES` ⇒ `400/422`.
    * Reasignar `vehicleId` ⇒ `409`.

---

## Flujos End-to-End

### Flujo 1: Registrar Vehículo Nuevo

1. **Obtener catálogos**: `GET /api/vehicles/published`
2. **Frontend construye formulario** con dropdowns de marcas, tipos, etc.
3. **Crear vehículo**: `POST /api/vehicles` con datos del formulario
4. **Subir SOAT**: `POST /api/vehicles/{id}/documents` (docType=SOAT)
5. **Subir RTM**: `POST /api/vehicles/{id}/documents` (docType=RTM)

---

### Flujo 2: Checklist Pre-operacional con Vehículo

1. **Obtener diseño de checklist**: `GET /api/checklists/templates/CHK_PREOP_VEH_GEN/versions/published`
2. **Crear instancia**: `POST /api/checklists/instances?templateCode=CHK_PREOP_VEH_GEN&driverId=100`
3. **Conductor completa checklist** en app móvil/web
4. **Guardar respuestas**: `POST /api/checklists/instances/{id}/responses`
   - Incluir `vehicleId` y `odometer` en la primera llamada
   - Si hay ítems con `OBS` o `NOOP`, incluir `comment` y `details`
5. **Si hay críticos NOOP**: `POST /api/checklists/responses/{responseId}/attachments` (foto de evidencia)
6. **Cerrar checklist**: `POST /api/checklists/instances/{id}/submit`
7. **Backend actualiza** `vehicle.currentOdometer` automáticamente

---

### Flujo 3: Dashboard de Alertas de Vencimiento

1. **Obtener vehículos próximos a vencer**: `GET /api/vehicles?vencenEn=30`
2. **Frontend muestra**:
   - Badge rojo para vehículos con `daysToSoat < 7` o `daysToRtm < 7`
   - Badge amarillo para `7 <= days < 15`
   - Badge verde para `days >= 15`
3. **Click en vehículo**: `GET /api/vehicles/{id}` → mostrar detalle completo
4. **Botón "Subir nuevo SOAT/RTM"**: abre modal de upload
5. **Upload**: `POST /api/vehicles/{id}/documents`
6. **Actualizar lista**: refresh del dashboard

---

### Flujo 4: Historial de Documentos

1. **Ver documentos SOAT**: `GET /api/vehicles/{id}/documents?docType=SOAT`
2. **Frontend muestra tabla** con:
   - Filename
   - Fecha de vencimiento
   - Fecha de carga
   - Botón "Descargar"
3. **Click en descargar**: `GET /api/vehicles/{id}/documents/{docId}`
4. **Browser descarga** archivo automáticamente

---

## Notas Finales

* **Paginación**: Usar parámetros `page`, `size`, `sort` estándar de Spring Data
* **Caché HTTP**: Aprovechar `ETag` y `Cache-Control` para optimizar requests
* **Validaciones**: El backend retorna mensajes descriptivos en español para errores 400/422
* **Soft Delete**: Los vehículos desactivados no aparecen por defecto (usar `includeInactive=true` para incluirlos)
* **Normalización**: Placas y VIN se convierten automáticamente a MAYÚSCULAS
* **Odómetro**: No puede retroceder - validación en backend
* **Documentos**: Detección de MIME por magic number (más seguro que extensión)
