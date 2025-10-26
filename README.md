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

Esta guía documenta **los endpoints reales** del backend (Spring Boot) para el **módulo de Checklists**, con ejemplos detallados para Postman y reglas de negocio para que Frontend pueda simular **situaciones reales**.

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

#### Request

**Método:** `GET`

**URL:** `{{baseUrl}}/api/checklists/templates/CHK_PREOP_VEH_GEN/versions/published`

**Path Parameters:**
- `templateCode`: `CHK_PREOP_VEH_GEN`

**Headers:**
```
Authorization: Bearer {{token}}
Accept: application/json
If-None-Match: "abc123hash456"  (opcional - para cache)
```

---

#### Response 200 OK - Primera carga (sin cache)

**Headers:**
```
Content-Type: application/json
ETag: "sha256-a1b2c3d4e5f6g7h8i9j0"
Last-Modified: Fri, 26 Oct 2024 10:00:00 GMT
Cache-Control: max-age=3600
```

**Body:**
```json
{
  "templateCode": "CHK_PREOP_VEH_GEN",
  "versionId": 100,
  "versionLabel": "1.1",
  "publishedAt": "2024-10-26T10:00:00Z",
  "versionHash": "sha256-a1b2c3d4e5f6g7h8i9j0",
  "stateOptions": [
    {
      "code": "OK",
      "label": "OK",
      "order": 0
    },
    {
      "code": "OBS",
      "label": "Observación",
      "order": 1
    },
    {
      "code": "NOOP",
      "label": "No operativo",
      "order": 2
    },
    {
      "code": "NA",
      "label": "N/A",
      "order": 3
    }
  ],
  "sections": [
    {
      "id": 50,
      "code": "PRESENTACION_CONFORT",
      "title": "Presentación y confort",
      "order": 0,
      "items": [
        {
          "id": 501,
          "code": "CONF_ASEO",
          "label": "Aseo y presentación (int/ext)",
          "severity": "LOW",
          "required": false,
          "allowNA": false,
          "hasDetails": false,
          "detailCatalog": null,
          "helpText": null,
          "order": 0
        }
      ]
    },
    {
      "id": 51,
      "code": "FLUIDOS",
      "title": "Fluidos",
      "order": 1,
      "items": [
        {
          "id": 510,
          "code": "FLU_ACEITE_MOTOR",
          "label": "Aceite de motor",
          "severity": "HIGH",
          "required": true,
          "allowNA": false,
          "hasDetails": false,
          "detailCatalog": null,
          "helpText": "Verificar nivel en varilla, debe estar entre MIN-MAX",
          "order": 0
        },
        {
          "id": 511,
          "code": "FLU_LIQ_FRENOS",
          "label": "Líquido de frenos",
          "severity": "CRITICAL",
          "required": true,
          "allowNA": false,
          "hasDetails": false,
          "detailCatalog": null,
          "helpText": "Nivel debe estar en depósito. Si está en NOOP requiere evidencia.",
          "order": 1
        },
        {
          "id": 512,
          "code": "FLU_OTROS",
          "label": "Otros fluidos",
          "severity": "MEDIUM",
          "required": false,
          "allowNA": false,
          "hasDetails": true,
          "detailCatalog": "OtherFluids",
          "helpText": null,
          "order": 3
        }
      ]
    },
    {
      "id": 56,
      "code": "RODADURA_FRENOS",
      "title": "Rodadura y frenos",
      "order": 6,
      "items": [
        {
          "id": 560,
          "code": "ROD_LLANTAS",
          "label": "Llantas (estado general)",
          "severity": "HIGH",
          "required": true,
          "allowNA": false,
          "hasDetails": true,
          "detailCatalog": "WheelPositions",
          "helpText": "Verificar profundidad de labrado, desgaste irregular, grietas",
          "order": 2
        }
      ]
    }
  ],
  "detailCatalogs": {
    "ExternalLights": [
      { "code": "BAJAS", "label": "Bajas", "order": 0 },
      { "code": "MEDIAS_ALTAS", "label": "Medias/Altas", "order": 1 },
      { "code": "DIR_DEL", "label": "Direccionales delanteras", "order": 2 },
      { "code": "DIR_TRAS", "label": "Direccionales traseras", "order": 3 },
      { "code": "PARQUEO", "label": "Parqueo", "order": 4 },
      { "code": "EXPLORADORAS", "label": "Exploradoras/Antiniebla", "order": 5 },
      { "code": "REVERSA", "label": "Reversa", "order": 6 }
    ],
    "WheelPositions": [
      { "code": "DEL_IZQ", "label": "Delantera izquierda", "order": 0 },
      { "code": "DEL_DER", "label": "Delantera derecha", "order": 1 },
      { "code": "TRAS_IZQ", "label": "Trasera izquierda", "order": 2 },
      { "code": "TRAS_DER", "label": "Trasera derecha", "order": 3 },
      { "code": "EJE2_IZQ", "label": "Eje 2 izquierda", "order": 4 },
      { "code": "EJE2_DER", "label": "Eje 2 derecha", "order": 5 }
    ],
    "OtherFluids": [
      { "code": "HIDRAULICO", "label": "Aceite hidráulico", "order": 0 },
      { "code": "AGUA_PLUMILLAS", "label": "Agua para plumillas", "order": 1 },
      { "code": "BATERIA", "label": "Agua de batería", "order": 2 }
    ],
    "FirstAidItems": [
      { "code": "ALCOHOL", "label": "Alcohol antiséptico", "order": 0 },
      { "code": "GASAS", "label": "Gasas estériles", "order": 3 },
      { "code": "VENDAJES", "label": "Vendajes", "order": 4 }
    ],
    "InstrumentFaults": [
      { "code": "VELOCIMETRO", "label": "Velocímetro", "order": 0 },
      { "code": "ACEITE", "label": "Indicador de aceite", "order": 2 },
      { "code": "TEMPERATURA", "label": "Indicador de temperatura", "order": 3 }
    ],
    "RegulatoryItems": [
      { "code": "CRUCETA", "label": "Cruceta/Copa", "order": 0 },
      { "code": "TACOS", "label": "2 tacos de bloqueo", "order": 1 },
      { "code": "CHALECO", "label": "Chaleco reflectivo", "order": 5 }
    ],
    "SeatbeltPositions": [
      { "code": "PILOTO", "label": "Piloto", "order": 0 },
      { "code": "COPILOTO", "label": "Copiloto", "order": 1 },
      { "code": "TRASEROS", "label": "Traseros", "order": 2 }
    ],
    "SuspensionAreas": [
      { "code": "DEL", "label": "Delantera", "order": 0 },
      { "code": "TRAS", "label": "Trasera", "order": 1 }
    ]
  }
}
```

---

#### Response 304 Not Modified - Cache válido

**Cuando el cliente envía `If-None-Match` con el mismo hash:**

**Headers:**
```
ETag: "sha256-a1b2c3d4e5f6g7h8i9j0"
Last-Modified: Fri, 26 Oct 2024 10:00:00 GMT
Cache-Control: max-age=3600
```

**Body:** (vacío)

> **💡 Tip Postman:** Usa Tests para guardar el ETag y enviarlo en próximas requests:
> ```javascript
> pm.environment.set("checklistETag", pm.response.headers.get("ETag"));
> ```

---

#### Response 404 Not Found

**Body:**
```json
{
  "type": "about:blank",
  "title": "No encontrado",
  "status": 404,
  "detail": "No existe versión publicada para el template 'CHK_INVALID_CODE'",
  "instance": "/api/checklists/templates/CHK_INVALID_CODE/versions/published",
  "timestamp": "2024-10-26T14:30:00Z"
}
```

---

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

### POST `/api/checklists/instances`

Crea una **nueva instancia** de checklist para el conductor especificado.

#### Request

**Método:** `POST`

**URL:** `{{baseUrl}}/api/checklists/instances?templateCode=CHK_PREOP_VEH_GEN&driverId=42`

**Query Params:**
- `templateCode`: `CHK_PREOP_VEH_GEN`
- `driverId`: `42`

**Headers:**
```
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Body:** (ninguno)

---

#### Response 201 Created - Instancia creada exitosamente

**Headers:**
```
Location: /api/checklists/instances/1001
```

**Body:**
```json
{
  "instanceId": 1001,
  "status": "IN_PROGRESS",
  "startedAt": "2024-10-26T14:30:00Z",
  "dueAt": "2024-10-26T15:30:00Z"
}
```

> **⏱️ Nota:** TTL = 60 minutos. El checklist debe completarse antes de `dueAt`.

> **💡 Tip Postman:** Guarda el instanceId en Tests:
> ```javascript
> pm.environment.set("instanceId", pm.response.json().instanceId);
> ```

---

#### Response 409 Conflict - Ya existe instancia abierta

**Body:**
```json
{
  "type": "about:blank",
  "title": "Conflicto",
  "status": 409,
  "detail": "El conductor 42 ya tiene una instancia abierta (ID: 998) para este template. Debe completarla o esperar a que expire.",
  "instance": "/api/checklists/instances",
  "timestamp": "2024-10-26T14:30:00Z",
  "existingInstanceId": 998,
  "existingInstanceDueAt": "2024-10-26T15:15:00Z"
}
```

---

#### Response 409 Conflict - Cooldown activo

**Body:**
```json
{
  "type": "about:blank",
  "title": "Conflicto",
  "status": 409,
  "detail": "El conductor 42 tuvo una instancia expirada recientemente. Debe esperar 8 minutos más antes de crear otra.",
  "instance": "/api/checklists/instances",
  "timestamp": "2024-10-26T14:30:00Z",
  "cooldownRemainingSeconds": 480,
  "lastExpiredInstanceId": 995,
  "cooldownEndsAt": "2024-10-26T14:38:00Z"
}
```

---

#### Response 400 Bad Request - Generación deshabilitada

**Body:**
```json
{
  "type": "about:blank",
  "title": "Solicitud inválida",
  "status": 400,
  "detail": "La generación de checklists está deshabilitada en este momento. Contacte al administrador.",
  "instance": "/api/checklists/instances",
  "timestamp": "2024-10-26T14:30:00Z"
}
```

---

#### GET `/api/checklists/drivers/{driverId}/instances/pending/payload`

Recupera la instancia abierta del conductor con progreso actual y tiempo restante. **Útil para retomar un checklist en curso.**

#### Request

**Método:** `GET`

**URL:** `{{baseUrl}}/api/checklists/drivers/42/instances/pending/payload`

**Path Variables:**
- `driverId`: `42`

**Headers:**
```
Authorization: Bearer {{token}}
```

---

#### Response 200 OK - Instancia en progreso encontrada

**Body:**
```json
{
  "instanceId": 1001,
  "status": "IN_PROGRESS",
  "templateCode": "CHK_PREOP_VEH_GEN",
  "versionLabel": "1.1",
  "startedAt": "2024-10-26T14:30:00Z",
  "dueAt": "2024-10-26T15:30:00Z",
  "timeRemainingSec": 2400,
  "vehicleId": 2001,
  "vehiclePlate": "ABC123",
  "odometer": 124500,
  "responses": [
    {
      "itemCode": "FLU_ACEITE_MOTOR",
      "state": "OK",
      "comment": null,
      "details": [],
      "attachments": [],
      "answeredAt": "2024-10-26T14:32:15Z"
    },
    {
      "itemCode": "FLU_LIQ_FRENOS",
      "state": "OBS",
      "comment": "Nivel bajo, requiere revisión en taller",
      "details": [],
      "attachments": [
        {
          "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
          "filename": "liquido_frenos.jpg",
          "type": "image/jpeg",
          "size": 2048576,
          "url": "/api/attachments/f47ac10b-58cc-4372-a567-0e02b2c3d479",
          "uploadedAt": "2024-10-26T14:33:20Z"
        }
      ],
      "answeredAt": "2024-10-26T14:33:00Z"
    },
    {
      "itemCode": "ROD_LLANTAS",
      "state": "OBS",
      "comment": "Desgaste irregular detectado en llanta delantera izquierda",
      "details": ["DEL_IZQ"],
      "attachments": [
        {
          "id": "a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d",
          "filename": "llanta_desgaste.jpg",
          "type": "image/jpeg",
          "size": 1523400,
          "url": "/api/attachments/a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d",
          "uploadedAt": "2024-10-26T14:35:45Z"
        }
      ],
      "answeredAt": "2024-10-26T14:35:30Z"
    }
  ],
  "progressSummary": {
    "totalItems": 30,
    "answeredItems": 3,
    "pendingItems": 27,
    "percentComplete": 10
  }
}
```

---

#### Response 404 Not Found - No hay instancia abierta

**Body:**
```json
{
  "type": "about:blank",
  "title": "No encontrado",
  "status": 404,
  "detail": "El conductor 42 no tiene ninguna instancia de checklist abierta.",
  "instance": "/api/checklists/drivers/42/instances/pending/payload",
  "timestamp": "2024-10-26T14:30:00Z"
}
```

---

### GET `/api/checklists/instances/{id}/details`

Obtiene el **detalle completo** de una instancia con todas las respuestas y resumen de estado.

#### Request

**Método:** `GET`

**URL:** `{{baseUrl}}/api/checklists/instances/{{instanceId}}/details`

**Path Variables:**
- `id`: `{{instanceId}}`

**Headers:**
```
Authorization: Bearer {{token}}
```

---

#### Response 200 OK - Detalles completos

**Body:**
```json
{
  "instanceId": 1001,
  "status": "SUBMITTED",
  "templateCode": "CHK_PREOP_VEH_GEN",
  "versionLabel": "1.1",
  "driverId": 42,
  "driverName": "Juan Pérez",
  "vehicleId": 2001,
  "vehiclePlate": "ABC123",
  "vehicleMake": "Toyota",
  "vehicleModel": "Hilux",
  "odometer": 124500,
  "startedAt": "2024-10-26T14:30:00Z",
  "completedAt": "2024-10-26T14:58:30Z",
  "dueAt": "2024-10-26T15:30:00Z",
  "conditionGeneral": "APTO_RESTRICCIONES",
  "responses": [
    {
      "id": 5001,
      "itemCode": "CONF_ASEO",
      "itemLabel": "Aseo y presentación (int/ext)",
      "section": "Presentación y confort",
      "severity": "LOW",
      "state": "OK",
      "comment": null,
      "details": [],
      "attachments": []
    },
    {
      "id": 5002,
      "itemCode": "FLU_ACEITE_MOTOR",
      "itemLabel": "Aceite de motor",
      "section": "Fluidos",
      "severity": "HIGH",
      "state": "OK",
      "comment": null,
      "details": [],
      "attachments": []
    },
    {
      "id": 5003,
      "itemCode": "FLU_LIQ_FRENOS",
      "itemLabel": "Líquido de frenos",
      "section": "Fluidos",
      "severity": "CRITICAL",
      "state": "OBS",
      "comment": "Nivel bajo, requiere revisión en taller",
      "details": [],
      "attachments": [
        {
          "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
          "filename": "liquido_frenos.jpg",
          "type": "image/jpeg",
          "size": 2048576,
          "url": "/api/attachments/f47ac10b-58cc-4372-a567-0e02b2c3d479"
        }
      ]
    },
    {
      "id": 5010,
      "itemCode": "ROD_LLANTAS",
      "itemLabel": "Llantas (estado general)",
      "section": "Rodadura y frenos",
      "severity": "HIGH",
      "state": "OBS",
      "comment": "Desgaste irregular detectado en llanta delantera izquierda",
      "details": ["DEL_IZQ"],
      "detailsExpanded": [
        {
          "code": "DEL_IZQ",
          "label": "Delantera izquierda"
        }
      ],
      "attachments": [
        {
          "id": "a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d",
          "filename": "llanta_desgaste.jpg",
          "type": "image/jpeg",
          "size": 1523400,
          "url": "/api/attachments/a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d"
        }
      ]
    },
    {
      "id": 5015,
      "itemCode": "SEG_AIRBAGS",
      "itemLabel": "Airbags (si aplica)",
      "section": "Seguridad activa/pasiva",
      "severity": "HIGH",
      "state": "NA",
      "comment": "Vehículo no cuenta con airbags",
      "details": [],
      "attachments": []
    }
  ],
  "summary": {
    "totalItems": 30,
    "answeredItems": 30,
    "okCount": 27,
    "oobCount": 2,
    "noopCount": 0,
    "naCount": 1,
    "criticalNoopCount": 0,
    "overall": "APTO_RESTRICCIONES"
  },
  "generalAttachments": []
}
```

> **📊 Nota sobre `overall`:**
> - `APTO`: Todos los ítems en OK o NA válido
> - `APTO_RESTRICCIONES`: Al menos un ítem en OBS
> - `NO_APTO`: Al menos un ítem en NOOP

---

#### Response 404 Not Found

**Body:**
```json
{
  "type": "about:blank",
  "title": "No encontrado",
  "status": 404,
  "detail": "No se encontró la instancia con ID 1001",
  "instance": "/api/checklists/instances/1001/details",
  "timestamp": "2024-10-26T14:30:00Z"
}
```

## 4) Guardar respuestas (y asignación tardía de vehículo/odómetro)

### POST `/api/checklists/instances/{id}/responses`

Guarda respuestas de forma **progresiva** (puede llamarse múltiples veces). Permite asignar vehículo en la **primera llamada** o en llamadas posteriores.

#### Request Base

**Método:** `POST`

**URL:** `{{baseUrl}}/api/checklists/instances/{{instanceId}}/responses`

**Path Variables:**
- `id`: `{{instanceId}}`

**Headers:**
```
Authorization: Bearer {{token}}
Content-Type: application/json
```

---

#### **Ejemplo 1: Primera guardada CON asignación de vehículo**

**Body:**
```json
{
  "vehicleId": 2001,
  "odometer": 124500,
  "responses": [
    {
      "itemCode": "CONF_ASEO",
      "state": "OK",
      "comment": null,
      "details": []
    },
    {
      "itemCode": "CONF_CLIMA",
      "state": "OK",
      "comment": null,
      "details": []
    },
    {
      "itemCode": "FLU_ACEITE_MOTOR",
      "state": "OK",
      "comment": null,
      "details": []
    }
  ]
}
```

**Response 200 OK:**
```json
{
  "message": "Respuestas guardadas exitosamente",
  "savedCount": 3,
  "vehicleAssigned": true,
  "vehiclePlate": "ABC123",
  "odometer": 124500
}
```

---

#### **Ejemplo 2: Guardadas posteriores SIN vehículo (progreso incremental)**

**Body:**
```json
{
  "responses": [
    {
      "itemCode": "FLU_LIQ_FRENOS",
      "state": "OBS",
      "comment": "Nivel bajo detectado en reservorio, requiere atención",
      "details": []
    },
    {
      "itemCode": "FLU_REFRIGERANTE",
      "state": "OK",
      "comment": null,
      "details": []
    }
  ]
}
```

**Response 200 OK:**
```json
{
  "message": "Respuestas guardadas exitosamente",
  "savedCount": 2,
  "vehicleAssigned": false
}
```

---

#### **Ejemplo 3: Respuesta con detalles (multi-select de catálogo)**

**Body:**
```json
{
  "responses": [
    {
      "itemCode": "ROD_LLANTAS",
      "state": "OBS",
      "comment": "Desgaste irregular en llantas delanteras, profundidad de labrado en límite mínimo",
      "details": ["DEL_IZQ", "DEL_DER"]
    },
    {
      "itemCode": "ROD_RINES",
      "state": "OBS",
      "comment": "Pequeñas abolladuras en rin trasero izquierdo",
      "details": ["TRAS_IZQ"]
    },
    {
      "itemCode": "LUZ_EXTERNAS",
      "state": "OBS",
      "comment": "Luces direccionales delanteras intermitentes presentan falla",
      "details": ["DIR_DEL"]
    }
  ]
}
```

**Response 200 OK:**
```json
{
  "message": "Respuestas guardadas exitosamente",
  "savedCount": 3,
  "vehicleAssigned": false
}
```

---

#### **Ejemplo 4: Respuesta NOOP crítica (requerirá evidencia)**

**Body:**
```json
{
  "responses": [
    {
      "itemCode": "FLU_LIQ_FRENOS",
      "state": "NOOP",
      "comment": "Fuga masiva detectada en manguera principal de frenos delanteros, pérdida total de presión",
      "details": []
    },
    {
      "itemCode": "ROD_FRENOS_SISTEMA",
      "state": "NOOP",
      "comment": "Frenos no responden, pedal llega hasta el fondo sin resistencia",
      "details": []
    }
  ]
}
```

**Response 200 OK:**
```json
{
  "message": "Respuestas guardadas exitosamente",
  "savedCount": 2,
  "vehicleAssigned": false,
  "warnings": [
    {
      "itemCode": "FLU_LIQ_FRENOS",
      "severity": "CRITICAL",
      "message": "Ítem crítico en NOOP. Se requiere evidencia fotográfica antes de submit."
    },
    {
      "itemCode": "ROD_FRENOS_SISTEMA",
      "severity": "CRITICAL",
      "message": "Ítem crítico en NOOP. Se requiere evidencia fotográfica antes de submit."
    }
  ]
}
```

> **⚠️ Importante:** Estos ítems CRITICAL en NOOP bloquearán el submit hasta que se suba evidencia.

---

#### **Ejemplo 5: Respuesta con NA (permitido)**

**Body:**
```json
{
  "responses": [
    {
      "itemCode": "SEG_AIRBAGS",
      "state": "NA",
      "comment": "El vehículo no cuenta con sistema de airbags",
      "details": []
    },
    {
      "itemCode": "OTR_ALARMA_REVERSA",
      "state": "NA",
      "comment": "No aplica para este modelo de vehículo",
      "details": []
    }
  ]
}
```

**Response 200 OK:**
```json
{
  "message": "Respuestas guardadas exitosamente",
  "savedCount": 2,
  "vehicleAssigned": false
}
```

---

#### **Ejemplo 6: Actualización de respuesta existente**

**Escenario:** Cambiar FLU_ACEITE_MOTOR de OK a OBS

**Body:**
```json
{
  "responses": [
    {
      "itemCode": "FLU_ACEITE_MOTOR",
      "state": "OBS",
      "comment": "Nivel ligeramente por debajo del mínimo, requiere adición antes de operar",
      "details": []
    }
  ]
}
```

**Response 200 OK:**
```json
{
  "message": "Respuestas guardadas exitosamente",
  "savedCount": 1,
  "vehicleAssigned": false,
  "updated": [
    {
      "itemCode": "FLU_ACEITE_MOTOR",
      "previousState": "OK",
      "newState": "OBS"
    }
  ]
}
```

---

#### **Ejemplo 7: Múltiples ítems con catálogo de detalles**

**Body:**
```json
{
  "responses": [
    {
      "itemCode": "FLU_OTROS",
      "state": "OBS",
      "comment": "Nivel de agua de plumillas bajo, requiere recarga",
      "details": ["AGUA_PLUMILLAS"]
    },
    {
      "itemCode": "REG_BOTIQUIN",
      "state": "OBS",
      "comment": "Faltan algunos elementos del botiquín",
      "details": ["GASAS", "VENDAJES", "ALCOHOL"]
    },
    {
      "itemCode": "TAB_INSTRUMENTOS",
      "state": "OBS",
      "comment": "Indicador de temperatura presenta lectura errática",
      "details": ["TEMPERATURA"]
    }
  ]
}
```

**Response 200 OK:**
```json
{
  "message": "Respuestas guardadas exitosamente",
  "savedCount": 3,
  "vehicleAssigned": false
}
```

---

### **Errores Comunes**

#### Error 400 - NA no permitido

**Body:**
```json
{
  "type": "about:blank",
  "title": "Solicitud inválida",
  "status": 400,
  "detail": "El ítem 'FLU_LIQ_FRENOS' no admite estado N/A",
  "instance": "/api/checklists/instances/1001/responses",
  "timestamp": "2024-10-26T14:35:00Z",
  "itemCode": "FLU_LIQ_FRENOS",
  "rejectedState": "NA",
  "allowNA": false
}
```

---

#### Error 400 - Comentario faltante o muy corto

**Body:**
```json
{
  "type": "about:blank",
  "title": "Solicitud inválida",
  "status": 400,
  "detail": "El ítem 'ROD_LLANTAS' en estado OBS requiere un comentario de al menos 5 caracteres",
  "instance": "/api/checklists/instances/1001/responses",
  "timestamp": "2024-10-26T14:35:00Z",
  "itemCode": "ROD_LLANTAS",
  "state": "OBS",
  "commentLength": 2,
  "minimumRequired": 5
}
```

---

#### Error 400 - Detalles faltantes cuando se requieren

**Body:**
```json
{
  "type": "about:blank",
  "title": "Solicitud inválida",
  "status": 400,
  "detail": "El ítem 'ROD_RINES' en estado OBS requiere al menos un detalle del catálogo 'WheelPositions'",
  "instance": "/api/checklists/instances/1001/responses",
  "timestamp": "2024-10-26T14:35:00Z",
  "itemCode": "ROD_RINES",
  "state": "OBS",
  "requiredCatalog": "WheelPositions",
  "providedDetails": []
}
```

---

#### Error 409 - Intento de reasignar vehículo

**Body:**
```json
{
  "type": "about:blank",
  "title": "Conflicto",
  "status": 409,
  "detail": "La instancia 1001 ya tiene asignado el vehículo ABC123 (ID: 2001). No se permite reasignación.",
  "instance": "/api/checklists/instances/1001/responses",
  "timestamp": "2024-10-26T14:35:00Z",
  "currentVehicleId": 2001,
  "currentVehiclePlate": "ABC123",
  "attemptedVehicleId": 2005
}
```

---

#### Error 400 - Odómetro faltante al asignar vehículo

**Body:**
```json
{
  "type": "about:blank",
  "title": "Solicitud inválida",
  "status": 400,
  "detail": "Al asignar un vehículo por primera vez, el campo 'odometer' es obligatorio",
  "instance": "/api/checklists/instances/1001/responses",
  "timestamp": "2024-10-26T14:35:00Z",
  "vehicleId": 2001
}
```

---

#### Error 400 - Odómetro menor al actual del vehículo

**Body:**
```json
{
  "type": "about:blank",
  "title": "Solicitud inválida",
  "status": 400,
  "detail": "El odómetro ingresado (120000 km) no puede ser menor al último registrado para el vehículo ABC123 (124500 km)",
  "instance": "/api/checklists/instances/1001/responses",
  "timestamp": "2024-10-26T14:35:00Z",
  "vehicleId": 2001,
  "vehiclePlate": "ABC123",
  "providedOdometer": 120000,
  "currentOdometer": 124500
}
```

---

#### Error 410 - Instancia expirada

**Body:**
```json
{
  "type": "about:blank",
  "title": "Instancia expirada",
  "status": 410,
  "detail": "La instancia 1001 expiró el 2024-10-26T15:30:00Z. TTL de 60 minutos excedido.",
  "instance": "/api/checklists/instances/1001/responses",
  "timestamp": "2024-10-26T15:45:00Z",
  "instanceId": 1001,
  "dueAt": "2024-10-26T15:30:00Z",
  "status": "EXPIRED"
}
```

---

#### Error 404 - Ítem no existe en versión

**Body:**
```json
{
  "type": "about:blank",
  "title": "No encontrado",
  "status": 404,
  "detail": "El ítem con código 'INVALID_ITEM_CODE' no existe en la versión 1.1 del checklist",
  "instance": "/api/checklists/instances/1001/responses",
  "timestamp": "2024-10-26T14:35:00Z",
  "itemCode": "INVALID_ITEM_CODE",
  "versionLabel": "1.1"
}
```

---

### **Tabla de Validaciones**

| Condición | Validación | Error |
|-----------|-----------|-------|
| `state = NA` | Ítem debe tener `allowNA = true` | 400 |
| `state = OBS` o `NOOP` | Comentario obligatorio (≥ 5 chars) | 400 |
| `state = OBS` o `NOOP` + ítem con catálogo | Al menos 1 detalle en `details[]` | 400 |
| Primera asignación de `vehicleId` | Campo `odometer` obligatorio | 400 |
| Reasignación de `vehicleId` | No permitido (inmutable) | 409 |
| Odómetro | Debe ser ≥ `vehicle.currentOdometer` | 400 |
| Instancia expirada (`dueAt` pasado) | Rechazado | 410 |
| Ítem no existe en versión | Código inválido | 404 |

## 5) Evidencias (Adjuntos)

### POST `/api/checklists/responses/{responseId}/attachments`

Sube una evidencia (foto o PDF) asociada a una respuesta específica.

#### Request

**Método:** `POST`

**URL:** `{{baseUrl}}/api/checklists/responses/5003/attachments`

**Path Variables:**
- `responseId`: `5003`

**Headers:**
```
Authorization: Bearer {{token}}
Content-Type: multipart/form-data
```

**Body (form-data en Postman):**
- Key: `file`
- Type: File
- Value: Seleccionar archivo JPG, PNG o PDF (máx 5MB)

> **💡 Tip Postman:** En la pestaña Body, selecciona `form-data`, agrega key `file`, cambia el tipo a `File` y selecciona tu archivo.

---

#### Response 201 Created - Evidencia subida exitosamente

**Headers:**
```
Location: /api/attachments/f47ac10b-58cc-4372-a567-0e02b2c3d479
```

**Body:**
```json
{
  "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "filename": "fuga_frenos.jpg",
  "type": "image/jpeg",
  "size": 2048576,
  "url": "/api/attachments/f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "responseId": 5003,
  "itemCode": "FLU_LIQ_FRENOS",
  "uploadedBy": "Juan Pérez",
  "uploadedAt": "2024-10-26T14:40:30Z"
}
```

> **💡 Tip Postman:** Guarda el attachment ID en Tests:
> ```javascript
> pm.environment.set("attachmentId", pm.response.json().id);
> ```

---

#### Response 409 Conflict - Ya existe evidencia

**Body:**
```json
{
  "type": "about:blank",
  "title": "Conflicto",
  "status": 409,
  "detail": "La respuesta 5003 ya tiene una evidencia asociada (ID: f47ac10b-58cc-4372-a567-0e02b2c3d479). Solo se permite 1 evidencia por respuesta.",
  "instance": "/api/checklists/responses/5003/attachments",
  "timestamp": "2024-10-26T14:45:00Z",
  "existingAttachmentId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "existingFilename": "fuga_frenos.jpg"
}
```

> **Nota:** Para reemplazar, primero debe eliminarse la evidencia existente con DELETE.

---

#### Response 400 Bad Request - Archivo demasiado grande

**Body:**
```json
{
  "type": "about:blank",
  "title": "Solicitud inválida",
  "status": 400,
  "detail": "El archivo excede el tamaño máximo permitido de 5 MB. Tamaño recibido: 7.2 MB",
  "instance": "/api/checklists/responses/5003/attachments",
  "timestamp": "2024-10-26T14:45:00Z",
  "fileSize": 7549747,
  "maxAllowedSize": 5242880
}
```

---

#### Response 415 Unsupported Media Type - Tipo de archivo no permitido

**Body:**
```json
{
  "type": "about:blank",
  "title": "Tipo de medio no soportado",
  "status": 415,
  "detail": "El tipo de archivo 'application/msword' no está permitido. Solo se aceptan: image/jpeg, image/png, application/pdf",
  "instance": "/api/checklists/responses/5003/attachments",
  "timestamp": "2024-10-26T14:45:00Z",
  "detectedMimeType": "application/msword",
  "allowedTypes": ["image/jpeg", "image/png", "application/pdf"]
}
```

> **Nota:** El MIME type se detecta por **magic numbers** (primeros bytes del archivo), no por extensión.

---

### POST `/api/checklists/instances/{instanceId}/attachments`

Sube evidencias **generales** para la instancia (no asociadas a una respuesta específica). Sin límite de cantidad.

#### Request

**Método:** `POST`

**URL:** `{{baseUrl}}/api/checklists/instances/{{instanceId}}/attachments`

**Path Variables:**
- `instanceId`: `{{instanceId}}`

**Headers:**
```
Authorization: Bearer {{token}}
Content-Type: multipart/form-data
```

**Body (form-data en Postman):**
- Key: `file`
- Type: File
- Value: Seleccionar archivo JPG, PNG o PDF (máx 5MB)

---

#### Response 201 Created

**Body:**
```json
{
  "id": "a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d",
  "filename": "foto_general_vehiculo.jpg",
  "type": "image/jpeg",
  "size": 3145728,
  "url": "/api/attachments/a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d",
  "instanceId": 1001,
  "uploadedBy": "Juan Pérez",
  "uploadedAt": "2024-10-26T14:42:00Z"
}
```

---

### GET `/api/attachments/{id}`

Descarga el archivo de evidencia.

#### Request

**Método:** `GET`

**URL:** `{{baseUrl}}/api/attachments/{{attachmentId}}`

**Path Variables:**
- `id`: `{{attachmentId}}`

**Headers:**
```
Authorization: Bearer {{token}}
```

> **💡 Tip Postman:** En la pestaña "Send and Download", Postman descargará el archivo automáticamente.

---

#### Response 200 OK

**Headers:**
```
Content-Type: image/jpeg
Content-Disposition: attachment; filename="fuga_frenos.jpg"
Content-Length: 2048576
ETag: "2048576"
Cache-Control: private, max-age=86400
```

**Body:** Binary data (JPG/PNG/PDF)

---

#### Response 404 Not Found

**Body:**
```json
{
  "type": "about:blank",
  "title": "No encontrado",
  "status": 404,
  "detail": "No se encontró el attachment con ID f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "instance": "/api/attachments/f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "timestamp": "2024-10-26T14:50:00Z"
}
```

---

### DELETE `/api/attachments/{id}`

Elimina un archivo de evidencia.

#### Request

**Método:** `DELETE`

**URL:** `{{baseUrl}}/api/attachments/{{attachmentId}}`

**Path Variables:**
- `id`: `{{attachmentId}}`

**Headers:**
```
Authorization: Bearer {{token}}
```

---

#### Response 204 No Content

Sin body. Eliminación exitosa.

---

#### Response 404 Not Found

**Body:**
```json
{
  "type": "about:blank",
  "title": "No encontrado",
  "status": 404,
  "detail": "No se encontró el attachment con ID f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "instance": "/api/attachments/f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "timestamp": "2024-10-26T14:55:00Z"
}
```

> **Recomendación:** Bloquear eliminación en UI si la instancia ya está en estado `SUBMITTED`, `APPROVED` o `REJECTED`.

## 6) Submit (cierre de checklist)

### POST `/api/checklists/instances/{id}/submit`

Cierra y sella el checklist. Valida que todos los ítems críticos en NOOP tengan evidencia. Actualiza el odómetro del vehículo.

#### Request

**Método:** `POST`

**URL:** `{{baseUrl}}/api/checklists/instances/{{instanceId}}/submit`

**Path Variables:**
- `id`: `{{instanceId}}`

**Headers:**
```
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Body:**
```json
{
  "conditionGeneral": "APTO"
}
```

> **Valores permitidos para `conditionGeneral`:**
> - `APTO`: Vehículo en condiciones óptimas para operar
> - `APTO_RESTRICCIONES`: Vehículo puede operar con observaciones menores
> - `NO_APTO`: Vehículo no apto para operar (requiere reparación)

---

#### **Ejemplo 1: Submit exitoso - Vehículo APTO**

**Body:**
```json
{
  "conditionGeneral": "APTO"
}
```

**Response 200 OK:**
```json
{
  "message": "Checklist cerrado exitosamente",
  "instanceId": 1001,
  "status": "SUBMITTED",
  "completedAt": "2024-10-26T14:58:30Z",
  "conditionGeneral": "APTO",
  "vehicleUpdated": true,
  "vehicleId": 2001,
  "vehiclePlate": "ABC123",
  "updatedOdometer": 124500,
  "summary": {
    "totalItems": 30,
    "okCount": 28,
    "oobCount": 0,
    "noopCount": 0,
    "naCount": 2,
    "criticalNoopCount": 0,
    "overall": "APTO"
  }
}
```

---

#### **Ejemplo 2: Submit exitoso - Vehículo APTO_RESTRICCIONES**

**Body:**
```json
{
  "conditionGeneral": "APTO_RESTRICCIONES"
}
```

**Response 200 OK:**
```json
{
  "message": "Checklist cerrado exitosamente",
  "instanceId": 1003,
  "status": "SUBMITTED",
  "completedAt": "2024-10-26T15:12:45Z",
  "conditionGeneral": "APTO_RESTRICCIONES",
  "vehicleUpdated": true,
  "vehicleId": 2005,
  "vehiclePlate": "XYZ789",
  "updatedOdometer": 87300,
  "summary": {
    "totalItems": 30,
    "okCount": 27,
    "oobCount": 3,
    "noopCount": 0,
    "naCount": 0,
    "criticalNoopCount": 0,
    "overall": "APTO_RESTRICCIONES"
  },
  "observations": [
    {
      "itemCode": "ROD_LLANTAS",
      "itemLabel": "Llantas (estado general)",
      "severity": "HIGH",
      "comment": "Desgaste irregular en llanta delantera izquierda",
      "details": ["DEL_IZQ"]
    },
    {
      "itemCode": "FLU_LIQ_FRENOS",
      "itemLabel": "Líquido de frenos",
      "severity": "CRITICAL",
      "comment": "Nivel bajo, requiere revisión en taller",
      "details": []
    },
    {
      "itemCode": "SEG_ESPEJOS_CRISTALES",
      "itemLabel": "Espejos y cristales (visibilidad)",
      "severity": "MEDIUM",
      "comment": "Espejo retrovisor derecho con pequeña fisura",
      "details": []
    }
  ]
}
```

---

#### **Ejemplo 3: Submit exitoso - Vehículo NO_APTO**

**Body:**
```json
{
  "conditionGeneral": "NO_APTO"
}
```

**Response 200 OK:**
```json
{
  "message": "Checklist cerrado exitosamente",
  "instanceId": 1004,
  "status": "SUBMITTED",
  "completedAt": "2024-10-26T15:25:10Z",
  "conditionGeneral": "NO_APTO",
  "vehicleUpdated": true,
  "vehicleId": 2008,
  "vehiclePlate": "DEF456",
  "updatedOdometer": 156200,
  "summary": {
    "totalItems": 30,
    "okCount": 26,
    "oobCount": 2,
    "noopCount": 2,
    "naCount": 0,
    "criticalNoopCount": 2,
    "overall": "NO_APTO"
  },
  "criticalIssues": [
    {
      "itemCode": "FLU_LIQ_FRENOS",
      "itemLabel": "Líquido de frenos",
      "severity": "CRITICAL",
      "state": "NOOP",
      "comment": "Fuga masiva detectada en manguera principal",
      "hasEvidence": true,
      "attachments": [
        {
          "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
          "filename": "fuga_frenos.jpg"
        }
      ]
    },
    {
      "itemCode": "ROD_FRENOS_SISTEMA",
      "itemLabel": "Frenos (sistema)",
      "severity": "CRITICAL",
      "state": "NOOP",
      "comment": "Pedal de freno sin resistencia, sistema colapsado",
      "hasEvidence": true,
      "attachments": [
        {
          "id": "a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d",
          "filename": "frenos_sistema.jpg"
        }
      ]
    }
  ],
  "vehicleBlocked": true,
  "blockReason": "Vehículo marcado como NO_APTO debido a fallas críticas en sistema de frenos"
}
```

---

### **Errores Comunes**

#### Error 400 - Falta evidencia en NOOP crítico

**Body:**
```json
{
  "type": "about:blank",
  "title": "Solicitud inválida",
  "status": 400,
  "detail": "Se requiere evidencia fotográfica para todos los ítems CRITICAL en estado NOOP antes de cerrar el checklist",
  "instance": "/api/checklists/instances/1004/submit",
  "timestamp": "2024-10-26T15:20:00Z",
  "missingEvidenceItems": [
    {
      "itemCode": "FLU_LIQ_FRENOS",
      "itemLabel": "Líquido de frenos",
      "severity": "CRITICAL",
      "state": "NOOP",
      "responseId": 5045,
      "hasEvidence": false
    },
    {
      "itemCode": "ROD_FRENOS_SISTEMA",
      "itemLabel": "Frenos (sistema)",
      "severity": "CRITICAL",
      "state": "NOOP",
      "responseId": 5052,
      "hasEvidence": false
    }
  ]
}
```

> **Acción requerida:** Subir evidencias usando `POST /api/checklists/responses/{responseId}/attachments`

---

#### Error 400 - Ítems requeridos sin responder

**Body:**
```json
{
  "type": "about:blank",
  "title": "Solicitud inválida",
  "status": 400,
  "detail": "Hay 5 ítems obligatorios sin responder. Todos los ítems requeridos deben ser completados antes de cerrar.",
  "instance": "/api/checklists/instances/1001/submit",
  "timestamp": "2024-10-26T15:20:00Z",
  "pendingRequiredItems": [
    {
      "itemCode": "FLU_ACEITE_MOTOR",
      "itemLabel": "Aceite de motor",
      "section": "Fluidos",
      "severity": "HIGH"
    },
    {
      "itemCode": "TAB_PITO",
      "itemLabel": "Pito (bocina)",
      "section": "Tablero e instrumentos",
      "severity": "CRITICAL"
    },
    {
      "itemCode": "SEG_DIRECCION",
      "itemLabel": "Dirección",
      "section": "Seguridad activa/pasiva",
      "severity": "CRITICAL"
    }
  ]
}
```

---

#### Error 410 - Instancia expirada

**Body:**
```json
{
  "type": "about:blank",
  "title": "Instancia expirada",
  "status": 410,
  "detail": "La instancia 1001 expiró el 2024-10-26T15:30:00Z. No se puede cerrar un checklist expirado.",
  "instance": "/api/checklists/instances/1001/submit",
  "timestamp": "2024-10-26T15:45:00Z",
  "instanceId": 1001,
  "dueAt": "2024-10-26T15:30:00Z",
  "status": "EXPIRED"
}
```

---

#### Error 409 - Instancia ya cerrada

**Body:**
```json
{
  "type": "about:blank",
  "title": "Conflicto",
  "status": 409,
  "detail": "La instancia 1001 ya fue cerrada el 2024-10-26T14:58:30Z con estado SUBMITTED",
  "instance": "/api/checklists/instances/1001/submit",
  "timestamp": "2024-10-26T15:00:00Z",
  "currentStatus": "SUBMITTED",
  "completedAt": "2024-10-26T14:58:30Z"
}
```

---

### **Comportamiento Post-Submit**

Una vez que el checklist es cerrado con `SUBMITTED`:

1. ✅ **Instancia sellada**: `completedAt` registrado, no se permiten más cambios
2. ✅ **Vehículo actualizado**:
   - `currentOdometer` actualizado con el valor del checklist
   - `condition` actualizado según `conditionGeneral`:
     - `APTO` → condition = `GOOD` (Bueno)
     - `APTO_RESTRICCIONES` → condition = `FAIR` (Regular)
     - `NO_APTO` → condition = `BAD` (Malo)
3. ✅ **Evidencias preservadas**: Los attachments quedan inmutables
4. ✅ **Historial trazable**: Queda registro completo para auditoría

**Estados futuros (flujo de aprobación):**
- `SUBMITTED` → `APPROVED` (supervisor aprueba)
- `SUBMITTED` → `REJECTED` (supervisor rechaza, requiere correcciones)

## 7) Recetas de prueba (end-to-end)

### 1. **APTO - Vehículo en perfectas condiciones**

**Secuencia en Postman:**

1. `POST /api/checklists/instances` - Crear instancia
2. `POST /instances/{id}/responses` - Guardar todos los ítems en `OK` (o `NA` válido donde aplique)
3. `POST /instances/{id}/submit` con `conditionGeneral: "APTO"`

**Resultado esperado:** `overall = APTO`, vehículo actualizado con condition = GOOD

---

### 2. **APTO_RESTRICCIONES - Vehículo con observaciones menores**

**Secuencia en Postman:**

1. `POST /api/checklists/instances` - Crear instancia
2. `POST /instances/{id}/responses` - Guardar respuestas:
   - Mayoría en `OK`
   - Al menos uno en `OBS` (con comentario + detalles si aplica)
   - Ejemplo: `ROD_LLANTAS` en `OBS` con `details: ["DEL_IZQ"]`
3. `POST /responses/{responseId}/attachments` - Subir foto opcional para la observación
4. `POST /instances/{id}/submit` con `conditionGeneral: "APTO_RESTRICCIONES"`

**Resultado esperado:** `overall = APTO_RESTRICCIONES`, vehículo actualizado con condition = FAIR

---

### 3. **NO_APTO - Vehículo con fallas críticas (con validación de evidencia)**

**Secuencia en Postman:**

1. `POST /api/checklists/instances` - Crear instancia
2. `POST /instances/{id}/responses` - Guardar respuestas:
   - Mayoría en `OK`
   - `FLU_LIQ_FRENOS` en `NOOP` con comentario
3. `POST /instances/{id}/submit` - **DEBE FALLAR con error 400** (falta evidencia)
4. `POST /responses/{responseId}/attachments` - Subir foto del líquido de frenos
5. `POST /instances/{id}/submit` con `conditionGeneral: "NO_APTO"` - **AHORA SÍ FUNCIONA**

**Resultado esperado:** `overall = NO_APTO`, vehículo actualizado con condition = BAD

---

### 4. **Errores esperables - Validaciones**

#### Test A: NA inválido
```json
{
  "responses": [
    {
      "itemCode": "FLU_LIQ_FRENOS",
      "state": "NA",
      "comment": null
    }
  ]
}
```
**Resultado esperado:** Error 400 - "El ítem no admite N/A"

---

#### Test B: OBS sin detalles cuando se requieren
```json
{
  "responses": [
    {
      "itemCode": "ROD_RINES",
      "state": "OBS",
      "comment": "Daño detectado"
      // Falta: "details": ["DEL_IZQ"]
    }
  ]
}
```
**Resultado esperado:** Error 400 - "Se requiere al menos un detalle del catálogo"

---

#### Test C: Reasignar vehículo
```json
// Primera llamada
{
  "vehicleId": 2001,
  "odometer": 124500,
  "responses": [...]
}

// Segunda llamada (intento de cambiar vehículo)
{
  "vehicleId": 2005,  // Diferente vehículo
  "responses": [...]
}
```
**Resultado esperado:** Error 409 - "No se permite reasignación"

---

#### Test D: Instancia expirada
1. Crear instancia
2. Esperar más de 60 minutos (o ajustar TTL en config para testing)
3. Intentar guardar respuestas

**Resultado esperado:** Error 410 - "Instancia expirada"
