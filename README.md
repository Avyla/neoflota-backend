# NeoFlota · Guía de Integración Frontend – Módulo Checklists (v1.1)

Esta guía documenta **los endpoints reales** del backend (Spring Boot) para el **módulo de Checklists**, con ejemplos y reglas de negocio para que Frontend pueda simular **situaciones reales** en Postman/HTTP.

**Template publicado** soportado en este build:

* `CHK_PREOP_VEH_GEN` (Checklist Pre-operacional – Vehículo, versión `1.1`, estado `Published`).

**Estados y catálogos clave**:

* Estado de respuesta (`ResponseState`): `OK | OBS | NOOP | NA` (JSON en mayúsculas).
* Severidad (`SeverityOptions`): `LOW | MEDIUM | HIGH | CRITICAL`.
* Estado de instancia (`InstanceStatus`): `PENDING | IN_PROGRESS | SUBMITTED | APPROVED | REJECTED | EXPIRED`.

## 1) Base URL + Autenticación

* Base URL: `{{baseUrl}}` (ej.: `http://localhost:8080`).
* Autenticación: header `Authorization: Bearer {{token}}` (si está habilitado).

> En la colección Postman adjunta ya se incluye el header `Authorization` como variable.

## 2) Plantillas publicadas

### GET `/api/checklists/templates/{templateCode}/versions/published`

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

## 3) Instancias

### POST `/api/checklists/instances?templateCode={code}&driverId={id}`

Crea una **instancia** para el conductor con TTL/cooldown configurado.

**Respuesta**: `{ instanceId, status, startedAt, dueAt }`

### GET `/api/checklists/drivers/{driverId}/instances/pending/payload`

Devuelve la instancia abierta (si existe) + tiempo restante y progreso:

`{ instanceId, status, startedAt, dueAt, timeRemainingSec, responses[] }`

### GET `/api/checklists/instances/{id}/details`

Devuelve el **detalle** completo incluyendo resumen:

* `responses[]`: `{ id, itemCode, state, comment, details[], attachments[] }`
* `summary`: `{ total, okCount, oobCount, noopCount, criticalNoopCount, overall }`
  con `overall ∈ { "APTO", "APTO_RESTRICCIONES", "NO_APTO" }`

## 4) Guardar respuestas (y asignación tardía de vehículo/odómetro)

### POST `/api/checklists/instances/{id}/responses`

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

## 5) Evidencias (Adjuntos)

### POST `/api/checklists/responses/{responseId}/attachments` (multipart)

* `file`: `jpg|png|pdf` (**5MB** máx).
* **1 evidencia por respuesta** (si ya existe ⇒ `409`).
* Detección de MIME por **magic number** + fallback.

**Respuesta**: `{ id (UUID), filename, type, size, url }`

### POST `/api/checklists/instances/{instanceId}/attachments` (multipart)

Adjuntos **generales** para la instancia (sin límite de cantidad).

### GET `/api/attachments/{id}`

Descarga el archivo. Devuelve cabeceras `Content-Disposition` y `ETag` (por tamaño).

### DELETE `/api/attachments/{id}`

Elimina el adjunto. (Recomendado bloquear en UI tras `SUBMITTED`).

## 6) Submit (cierre de checklist)

### POST `/api/checklists/instances/{id}/submit`

Valida y sella la instancia:

* Si hay **ítems CRITICAL en NOOP** ⇒ **evidencia obligatoria** por respuesta.
* Marca `completedAt = now`, `status = SUBMITTED`.
* Si hay vehículo, actualiza `vehicle.currentOdometer`.

**Códigos de resultado**

* `200/201`: ok
* `400/422`: faltan evidencias para NOOP crítico, faltan respuestas requeridas
* `410`: instancia expirada (`dueAt` vencido)

## 7) Recetas de prueba (end-to-end)

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
