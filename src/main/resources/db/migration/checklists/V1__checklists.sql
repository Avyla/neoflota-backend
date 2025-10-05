-- ============================================================================
--  MÓDULO: CHECKLISTS (PostgreSQL) — ESQUEMA + SEED COMENTADOS
--  Proyecto: Gestión de flotas (Java + Spring Boot + PostgreSQL)
--  Autor: Equipo de Datos
--  Descripción:
--     Este script crea la estructura de base de datos para manejar Checklists
--     (plantillas, versiones, secciones e ítems) y su ejecución (instancias,
--     respuestas, opciones multiselección, evidencias y firmas).
--
--     ⚠️ Importante:
--       - Las REGLAS DE NEGOCIO (validaciones de "Submit", cálculo de APTO, etc.)
--         se implementan en la capa de aplicación (Spring Boot), no en la BD.
--       - Este script incluye comentarios persistentes (COMMENT ON) para que
--         cualquier compañero pueda entender el propósito de cada tabla/campo.
-- ============================================================================

-- Opcional: usar un esquema propio (descomentar si se desea)
-- CREATE SCHEMA flota;
-- SET search_path TO flota, public;

-- ============================================================================
-- A) CATÁLOGOS REUTILIZABLES
-- ============================================================================

CREATE TABLE option_group (
  option_group_id      BIGSERIAL PRIMARY KEY,
  code                 VARCHAR(50)  NOT NULL UNIQUE,
  name                 VARCHAR(100) NOT NULL,
  description          VARCHAR(300),
  is_active            BOOLEAN NOT NULL DEFAULT TRUE,
  created_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);
COMMENT ON TABLE option_group IS 'Agrupa catálogos reutilizables de opciones (p. ej., EstadoGeneral, Posiciones de llantas, etc.).';
COMMENT ON COLUMN option_group.option_group_id IS 'PK autoincremental del grupo de opciones.';
COMMENT ON COLUMN option_group.code IS 'Código único legible por máquina (ej: EstadoGeneral).';
COMMENT ON COLUMN option_group.name IS 'Nombre legible del grupo.';
COMMENT ON COLUMN option_group.description IS 'Descripción del uso del grupo (opcional).';
COMMENT ON COLUMN option_group.is_active IS 'Permite activar/desactivar el grupo sin borrarlo.';
COMMENT ON COLUMN option_group.created_at IS 'Fecha/hora de creación (UTC).';

CREATE TABLE option_item (
  option_id            BIGSERIAL PRIMARY KEY,
  option_group_id      BIGINT NOT NULL REFERENCES option_group(option_group_id),
  code                 VARCHAR(50)  NOT NULL,
  label                VARCHAR(120) NOT NULL,
  order_index          INT NOT NULL DEFAULT 0,
  is_active            BOOLEAN NOT NULL DEFAULT TRUE,
  UNIQUE (option_group_id, code)
);
COMMENT ON TABLE option_item IS 'Valores pertenecientes a un option_group (ej., OK/OBS/NOOP/NA en EstadoGeneral).';
COMMENT ON COLUMN option_item.option_id IS 'PK autoincremental de la opción.';
COMMENT ON COLUMN option_item.option_group_id IS 'FK al grupo de opciones dueño de este valor.';
COMMENT ON COLUMN option_item.code IS 'Código único dentro del grupo (ej: OK, OBS, NOOP).';
COMMENT ON COLUMN option_item.label IS 'Etiqueta visible en UI (ej: "Observación").';
COMMENT ON COLUMN option_item.order_index IS 'Orden sugerido de despliegue.';
COMMENT ON COLUMN option_item.is_active IS 'Permite ocultar opciones sin perder historial.';

-- ============================================================================
-- B) DISEÑO DE LA PLANTILLA (LO QUE SE PIDE)
-- ============================================================================

CREATE TABLE checklist_template (
  template_id          BIGSERIAL PRIMARY KEY,
  code                 VARCHAR(50)  NOT NULL UNIQUE,   -- p.ej., CHK_PREOP_VEH_GEN
  name                 VARCHAR(200) NOT NULL,
  description          VARCHAR(500),
  entity_target        VARCHAR(24)  NOT NULL,          -- Vehicle|Service|Driver|MaintenanceOrder
  is_active            BOOLEAN NOT NULL DEFAULT TRUE,
  created_by_user_id   BIGINT,
  created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT chk_template_entity_target
    CHECK (entity_target IN ('Vehicle','Service','Driver','MaintenanceOrder'))
);
COMMENT ON TABLE checklist_template IS 'Plantilla base de checklist (familia de versiones). Define para qué entidad aplica.';
COMMENT ON COLUMN checklist_template.template_id IS 'PK autoincremental de la plantilla.';
COMMENT ON COLUMN checklist_template.code IS 'Código único de la plantilla (ej: CHK_PREOP_VEH_GEN).';
COMMENT ON COLUMN checklist_template.name IS 'Nombre visible (ej: "Checklist Pre-operacional – Vehículo (General)").';
COMMENT ON COLUMN checklist_template.description IS 'Descripción corta de la plantilla.';
COMMENT ON COLUMN checklist_template.entity_target IS 'A qué entidad apunta la plantilla: Vehicle, Service, Driver o MaintenanceOrder.';
COMMENT ON COLUMN checklist_template.is_active IS 'Permite activar/desactivar la plantilla.';
COMMENT ON COLUMN checklist_template.created_by_user_id IS 'Usuario que creó la plantilla (trazabilidad).';
COMMENT ON COLUMN checklist_template.created_at IS 'Fecha/hora de creación (UTC).';

CREATE TABLE checklist_version (
  version_id           BIGSERIAL PRIMARY KEY,
  template_id          BIGINT NOT NULL REFERENCES checklist_template(template_id),
  version_label        VARCHAR(20)  NOT NULL,          -- ej: 1.1
  status               VARCHAR(20)  NOT NULL,          -- Draft|Published|Archived
  created_by_user_id   BIGINT,
  created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
  published_at         TIMESTAMPTZ,
  UNIQUE (template_id, version_label),
  CONSTRAINT chk_version_status CHECK (status IN ('Draft','Published','Archived'))
);
COMMENT ON TABLE checklist_version IS 'Versiones inmutables de una plantilla. Lo ejecutado siempre referencia una versión.';
COMMENT ON COLUMN checklist_version.version_id IS 'PK autoincremental de la versión.';
COMMENT ON COLUMN checklist_version.template_id IS 'FK a la plantilla de la cual es versión.';
COMMENT ON COLUMN checklist_version.version_label IS 'Etiqueta de versión (ej: 1.1).';
COMMENT ON COLUMN checklist_version.status IS 'Estado: Draft (borrador), Published (publicada), Archived (archivada).';
COMMENT ON COLUMN checklist_version.created_by_user_id IS 'Usuario que creó la versión.';
COMMENT ON COLUMN checklist_version.created_at IS 'Fecha/hora de creación (UTC).';
COMMENT ON COLUMN checklist_version.published_at IS 'Fecha/hora de publicación (si aplica).';

CREATE TABLE checklist_section (
  section_id           BIGSERIAL PRIMARY KEY,
  version_id           BIGINT NOT NULL REFERENCES checklist_version(version_id),
  code                 VARCHAR(50)  NOT NULL,
  title                VARCHAR(200) NOT NULL,
  order_index          INT NOT NULL DEFAULT 0,
  UNIQUE (version_id, code)
);
COMMENT ON TABLE checklist_section IS 'Secciones lógicas para agrupar ítems (ej: Luces, Fluidos, etc.).';
COMMENT ON COLUMN checklist_section.section_id IS 'PK de la sección.';
COMMENT ON COLUMN checklist_section.version_id IS 'FK a la versión de la plantilla.';
COMMENT ON COLUMN checklist_section.code IS 'Código único por versión para identificar la sección.';
COMMENT ON COLUMN checklist_section.title IS 'Título visible de la sección.';
COMMENT ON COLUMN checklist_section.order_index IS 'Orden de despliegue dentro de la versión.';

CREATE TABLE checklist_item (
  item_id                  BIGSERIAL PRIMARY KEY,
  version_id               BIGINT NOT NULL REFERENCES checklist_version(version_id),
  section_id               BIGINT REFERENCES checklist_section(section_id),
  code                     VARCHAR(50)  NOT NULL,      -- p.ej., ROD_LLANTAS
  label                    VARCHAR(200) NOT NULL,
  help_text                VARCHAR(500),
  severity                 VARCHAR(10)  NOT NULL,      -- Low|Medium|High|Critical
  required                 BOOLEAN NOT NULL DEFAULT FALSE,
  allow_na                 BOOLEAN NOT NULL DEFAULT FALSE,
  state_option_group_id    BIGINT NOT NULL REFERENCES option_group(option_group_id),
  detail_option_group_id   BIGINT REFERENCES option_group(option_group_id), -- habilita MultiSelect contextual
  order_index              INT NOT NULL DEFAULT 0,
  UNIQUE (version_id, code),
  CONSTRAINT chk_item_severity CHECK (severity IN ('Low','Medium','High','Critical'))
);
COMMENT ON TABLE checklist_item IS 'Ítems/preguntas de una versión de checklist. Cada ítem usa un Estado y opcionalmente un detalle MultiSelect.';
COMMENT ON COLUMN checklist_item.item_id IS 'PK del ítem (pregunta).';
COMMENT ON COLUMN checklist_item.version_id IS 'FK a la versión a la que pertenece el ítem.';
COMMENT ON COLUMN checklist_item.section_id IS 'FK a la sección donde se muestra el ítem (opcional).';
COMMENT ON COLUMN checklist_item.code IS 'Código único por versión para identificar el ítem (ej: ROD_LLANTAS).';
COMMENT ON COLUMN checklist_item.label IS 'Etiqueta visible de la pregunta en UI.';
COMMENT ON COLUMN checklist_item.help_text IS 'Texto de ayuda/instrucciones (opcional).';
COMMENT ON COLUMN checklist_item.severity IS 'Criticidad del ítem: Low, Medium, High o Critical.';
COMMENT ON COLUMN checklist_item.required IS 'Si el ítem es obligatorio en la captura.';
COMMENT ON COLUMN checklist_item.allow_na IS 'Si permite marcar N/A (no aplica).';
COMMENT ON COLUMN checklist_item.state_option_group_id IS 'Siempre apunta a EstadoGeneral (OK/OBS/NOOP/NA).';
COMMENT ON COLUMN checklist_item.detail_option_group_id IS 'Cuando no es NULL, habilita MultiSelect (p. ej., posiciones de llantas).';
COMMENT ON COLUMN checklist_item.order_index IS 'Orden de despliegue del ítem dentro de la sección.';

CREATE INDEX idx_item_version ON checklist_item(version_id, section_id, order_index);

-- ============================================================================
-- C) EJECUCIÓN DEL CHECKLIST (LO QUE SE CONTESTA EN CAMPO)
-- ============================================================================

CREATE TABLE checklist_instance (
  instance_id         BIGSERIAL PRIMARY KEY,
  version_id          BIGINT NOT NULL REFERENCES checklist_version(version_id),
  -- Contexto: al menos uno debería ser NO NULL (validado en la app).
  vehicle_id          BIGINT,
  service_id          BIGINT,
  driver_id           BIGINT,
  maintenance_order_id BIGINT,
  status              VARCHAR(20) NOT NULL DEFAULT 'Pending', -- Pending|InProgress|Submitted|Approved|Rejected|Expired
  due_at              TIMESTAMPTZ,
  started_at          TIMESTAMPTZ,
  completed_at        TIMESTAMPTZ,
  performed_by_user_id BIGINT,
  location_lat        NUMERIC(9,6),
  location_lon        NUMERIC(9,6),
  odometer            INTEGER,
  overall_pass        BOOLEAN,
  condition_general   VARCHAR(25),  -- APTO|APTO_RESTRICCIONES|NO_APTO
  notes               VARCHAR(1000),
  created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT chk_instance_status CHECK (status IN ('Pending','InProgress','Submitted','Approved','Rejected','Expired')),
  CONSTRAINT chk_instance_condition CHECK (condition_general IS NULL OR condition_general IN ('APTO','APTO_RESTRICCIONES','NO_APTO'))
);
COMMENT ON TABLE checklist_instance IS 'Instancia real de un checklist ejecutado sobre vehículo/servicio/driver/orden.';
COMMENT ON COLUMN checklist_instance.instance_id IS 'PK de la ejecución del checklist.';
COMMENT ON COLUMN checklist_instance.version_id IS 'FK a la versión usada para esta ejecución (trazabilidad).';
COMMENT ON COLUMN checklist_instance.vehicle_id IS 'Vehículo inspeccionado (opcional dependiendo del caso).';
COMMENT ON COLUMN checklist_instance.service_id IS 'Servicio/orden de trabajo asociado (opcional).';
COMMENT ON COLUMN checklist_instance.driver_id IS 'Conductor asociado (opcional).';
COMMENT ON COLUMN checklist_instance.maintenance_order_id IS 'Orden de mantenimiento asociada (opcional).';
COMMENT ON COLUMN checklist_instance.status IS 'Estado del flujo de la instancia (Pending, InProgress, Submitted, etc.).';
COMMENT ON COLUMN checklist_instance.due_at IS 'Fecha/hora objetivo para completar (si aplica).';
COMMENT ON COLUMN checklist_instance.started_at IS 'Fecha/hora de inicio real de la captura.';
COMMENT ON COLUMN checklist_instance.completed_at IS 'Fecha/hora de finalización (Submit).';
COMMENT ON COLUMN checklist_instance.performed_by_user_id IS 'Usuario que ejecutó el checklist (auditoría).';
COMMENT ON COLUMN checklist_instance.location_lat IS 'Latitud de captura (opcional).';
COMMENT ON COLUMN checklist_instance.location_lon IS 'Longitud de captura (opcional).';
COMMENT ON COLUMN checklist_instance.odometer IS 'Lectura de odómetro (recomendado cuando hay vehicle_id).';
COMMENT ON COLUMN checklist_instance.overall_pass IS 'true solo si la condición general resulta APTO.';
COMMENT ON COLUMN checklist_instance.condition_general IS 'Resultado consolidado: APTO / APTO_RESTRICCIONES / NO_APTO.';
COMMENT ON COLUMN checklist_instance.notes IS 'Notas generales de la inspección.';
COMMENT ON COLUMN checklist_instance.created_at IS 'Fecha/hora de creación del registro.';

CREATE INDEX idx_instance_version   ON checklist_instance(version_id);
CREATE INDEX idx_instance_vehicle   ON checklist_instance(vehicle_id);
CREATE INDEX idx_instance_status    ON checklist_instance(status);

CREATE TABLE checklist_response (
  response_id         BIGSERIAL PRIMARY KEY,
  instance_id         BIGINT NOT NULL REFERENCES checklist_instance(instance_id) ON DELETE CASCADE,
  item_id             BIGINT NOT NULL REFERENCES checklist_item(item_id),
  selected_option_id  BIGINT NOT NULL REFERENCES option_item(option_id), -- debe pertenecer al grupo EstadoGeneral
  comment             VARCHAR(1000),
  created_by_user_id  BIGINT,
  created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (instance_id, item_id)
);
COMMENT ON TABLE checklist_response IS 'Respuesta por ítem dentro de una instancia (Estado + comentario).';
COMMENT ON COLUMN checklist_response.response_id IS 'PK de la respuesta.';
COMMENT ON COLUMN checklist_response.instance_id IS 'FK a la instancia donde se está respondiendo.';
COMMENT ON COLUMN checklist_response.item_id IS 'FK al ítem (pregunta) que se responde.';
COMMENT ON COLUMN checklist_response.selected_option_id IS 'Opción seleccionada de EstadoGeneral (OK/OBS/NOOP/NA).';
COMMENT ON COLUMN checklist_response.comment IS 'Comentario obligatorio si Estado ≠ OK (validado en servicio).';
COMMENT ON COLUMN checklist_response.created_by_user_id IS 'Usuario que registró la respuesta.';
COMMENT ON COLUMN checklist_response.created_at IS 'Timestamp de la respuesta.';

CREATE TABLE checklist_response_option (
  response_id         BIGINT NOT NULL REFERENCES checklist_response(response_id) ON DELETE CASCADE,
  option_id           BIGINT NOT NULL REFERENCES option_item(option_id),
  PRIMARY KEY (response_id, option_id)
);
COMMENT ON TABLE checklist_response_option IS 'Puente para MultiSelect de detalle (ej., posiciones de llantas afectadas).';
COMMENT ON COLUMN checklist_response_option.response_id IS 'Respuesta a la que pertenecen los detalles MultiSelect.';
COMMENT ON COLUMN checklist_response_option.option_id IS 'Opción del grupo de detalle seleccionado.';

CREATE TABLE checklist_attachment (
  attachment_id       BIGSERIAL PRIMARY KEY,
  instance_id         BIGINT REFERENCES checklist_instance(instance_id) ON DELETE CASCADE,
  response_id         BIGINT REFERENCES checklist_response(response_id) ON DELETE CASCADE,
  file_url            VARCHAR(500) NOT NULL,
  mime_type           VARCHAR(100),
  caption             VARCHAR(200),
  created_by_user_id  BIGINT,
  created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT chk_attach_target CHECK (
    (CASE WHEN instance_id IS NULL THEN 0 ELSE 1 END) +
    (CASE WHEN response_id IS NULL THEN 0 ELSE 1 END) = 1
  )
);
COMMENT ON TABLE checklist_attachment IS 'Evidencias (fotos/archivos) asociadas a toda la instancia o a una respuesta en particular.';
COMMENT ON COLUMN checklist_attachment.attachment_id IS 'PK de la evidencia.';
COMMENT ON COLUMN checklist_attachment.instance_id IS 'FK si la evidencia aplica a toda la instancia.';
COMMENT ON COLUMN checklist_attachment.response_id IS 'FK si la evidencia aplica a una respuesta específica.';
COMMENT ON COLUMN checklist_attachment.file_url IS 'Ruta/URL del archivo almacenado.';
COMMENT ON COLUMN checklist_attachment.mime_type IS 'Tipo MIME (image/jpeg, application/pdf, etc.).';
COMMENT ON COLUMN checklist_attachment.caption IS 'Descripción corta de la evidencia.';
COMMENT ON COLUMN checklist_attachment.created_by_user_id IS 'Usuario que sube la evidencia.';
COMMENT ON COLUMN checklist_attachment.created_at IS 'Fecha/hora de subida.';

CREATE TABLE checklist_signature (
  signature_id        BIGSERIAL PRIMARY KEY,
  instance_id         BIGINT NOT NULL REFERENCES checklist_instance(instance_id) ON DELETE CASCADE,
  role                VARCHAR(20) NOT NULL,  -- Driver|Inspector|Supervisor
  signed_by_user_id   BIGINT NOT NULL,
  signed_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
  signature_image_url VARCHAR(500),
  CONSTRAINT chk_sign_role CHECK (role IN ('Driver','Inspector','Supervisor'))
);
COMMENT ON TABLE checklist_signature IS 'Firmas asociadas al cierre de una instancia (quién valida).';
COMMENT ON COLUMN checklist_signature.signature_id IS 'PK de la firma.';
COMMENT ON COLUMN checklist_signature.instance_id IS 'FK a la instancia firmada.';
COMMENT ON COLUMN checklist_signature.role IS 'Rol del firmante: Driver, Inspector o Supervisor.';
COMMENT ON COLUMN checklist_signature.signed_by_user_id IS 'Usuario que realizó la firma.';
COMMENT ON COLUMN checklist_signature.signed_at IS 'Fecha/hora de la firma.';
COMMENT ON COLUMN checklist_signature.signature_image_url IS 'Ruta/URL de la imagen de firma (si se captura gráfica).';

-- ============================================================================
-- D) SEED DE CATÁLOGOS + PLANTILLA v1.1
-- ============================================================================

INSERT INTO option_group(code, name) VALUES
('EstadoGeneral','Estado general de ítem'),
('WheelPositions','Posiciones de llantas'),
('SuspensionAreas','Áreas de suspensión'),
('SeatbeltPositions','Posiciones de cinturones'),
('InstrumentFaults','Fallas de instrumentos'),
('ExternalLights','Luces externas'),
('RegulatoryItems','Equipo reglamentario'),
('FirstAidItems','Botiquín vehicular'),
('OtherFluids','Otros fluidos')
ON CONFLICT (code) DO NOTHING;

INSERT INTO option_item(option_group_id, code, label, order_index)
SELECT og.option_group_id, v.code, v.label, v.ord
FROM option_group og
JOIN (VALUES
  ('EstadoGeneral','OK','OK',1),
  ('EstadoGeneral','OBS','Observación',2),
  ('EstadoGeneral','NOOP','No operativo',3),
  ('EstadoGeneral','NA','N/A',4),

  ('WheelPositions','DEL_IZQ','Delantera izquierda',1),
  ('WheelPositions','DEL_DER','Delantera derecha',2),
  ('WheelPositions','TRAS_IZQ','Trasera izquierda',3),
  ('WheelPositions','TRAS_DER','Trasera derecha',4),
  ('WheelPositions','EJE2_IZQ','Eje 2 izquierda',5),
  ('WheelPositions','EJE2_DER','Eje 2 derecha',6),

  ('SuspensionAreas','DEL','Delantera',1),
  ('SuspensionAreas','TRAS','Trasera',2),

  ('SeatbeltPositions','PILOTO','Piloto',1),
  ('SeatbeltPositions','COPILOTO','Copiloto',2),
  ('SeatbeltPositions','TRASEROS','Traseros',3),

  ('InstrumentFaults','VELOCIMETRO','Velocímetro',1),
  ('InstrumentFaults','TACOMETRO','Tacómetro',2),
  ('InstrumentFaults','ACEITE','Indicador de aceite',3),
  ('InstrumentFaults','TEMPERATURA','Indicador de temperatura',4),
  ('InstrumentFaults','COMBUSTIBLE','Nivel de combustible',5),

  ('ExternalLights','BAJAS','Bajas',1),
  ('ExternalLights','MEDIAS_ALTAS','Medias/Altas',2),
  ('ExternalLights','DIR_DEL','Direccionales delanteras',3),
  ('ExternalLights','DIR_TRAS','Direccionales traseras',4),
  ('ExternalLights','PARQUEO','Parqueo',5),
  ('ExternalLights','EXPLORADORAS','Exploradoras/Antiniebla',6),
  ('ExternalLights','REVERSA','Reversa',7),

  ('RegulatoryItems','CRUCETA','Cruceta/Copa',1),
  ('RegulatoryItems','TACOS','2 tacos de bloqueo',2),
  ('RegulatoryItems','SENALES','2 señales de carretera',3),
  ('RegulatoryItems','LINTERNA','Linterna',4),
  ('RegulatoryItems','HERRAMIENTAS','Caja de herramientas',5),
  ('RegulatoryItems','CHALECO','Chaleco reflectivo',6),
  ('RegulatoryItems','GUANTES','Guantes de vaqueta',7),

  ('FirstAidItems','ALCOHOL','Alcohol antiséptico',1),
  ('FirstAidItems','BAJALENGUAS','Depresores linguales',2),
  ('FirstAidItems','ESPARADRAPO','Esparadrapo',3),
  ('FirstAidItems','GASAS','Gasas estériles',4),
  ('FirstAidItems','VENDAJES','Vendajes',5),
  ('FirstAidItems','CURAS','Curas',6),
  ('FirstAidItems','AGUA','Agua potable',7),

  ('OtherFluids','HIDRAULICO','Aceite hidráulico',1),
  ('OtherFluids','AGUA_PLUMILLAS','Agua para plumillas',2),
  ('OtherFluids','BATERIA','Agua de batería',3)
) AS v(group_code, code, label, ord)
  ON og.code = v.group_code
ON CONFLICT (option_group_id, code) DO NOTHING;

INSERT INTO checklist_template(code, name, description, entity_target)
VALUES ('CHK_PREOP_VEH_GEN','Checklist Pre-operacional – Vehículo (General)','Plantilla LITE 1.1','Vehicle')
ON CONFLICT (code) DO NOTHING;

INSERT INTO checklist_version(template_id, version_label, status, published_at)
SELECT t.template_id, '1.1', 'Published', now()
FROM checklist_template t
WHERE t.code = 'CHK_PREOP_VEH_GEN'
ON CONFLICT (template_id, version_label) DO NOTHING;

WITH ver AS (
  SELECT v.version_id
  FROM checklist_version v
  JOIN checklist_template t ON t.template_id = v.template_id
  WHERE t.code = 'CHK_PREOP_VEH_GEN' AND v.version_label = '1.1'
)
INSERT INTO checklist_section(version_id, code, title, order_index)
SELECT ver.version_id, s.code, s.title, s.ord
FROM ver
CROSS JOIN (VALUES
  ('SEC_ROD_FRE','Rodadura y frenos',1),
  ('SEC_SEG',    'Seguridad activa/pasiva',2),
  ('SEC_FLU',    'Fluidos',3),
  ('SEC_TAB',    'Tablero e instrumentos',4),
  ('SEC_LUZ',    'Luces',5),
  ('SEC_CONF',   'Presentación y confort',6),
  ('SEC_REG',    'Equipo reglamentario y botiquín',7),
  ('SEC_OTR',    'Otros componentes',8)
) AS s(code, title, ord)
WHERE NOT EXISTS (
  SELECT 1
  FROM checklist_section cs
  WHERE cs.version_id = ver.version_id
    AND cs.code = s.code
);


WITH
ver AS (
  SELECT v.version_id
  FROM checklist_version v
  JOIN checklist_template t ON t.template_id = v.template_id
  WHERE t.code = 'CHK_PREOP_VEH_GEN' AND v.version_label = '1.1'
),
sec AS (
  SELECT s.code, s.section_id
  FROM checklist_section s
  JOIN ver ON ver.version_id = s.version_id
),
og AS (
  SELECT code, option_group_id FROM option_group
),
state AS (
  SELECT option_group_id AS state_group_id
  FROM option_group WHERE code = 'EstadoGeneral'
)
INSERT INTO checklist_item(
  version_id, section_id, code, label, severity, required, allow_na,
  state_option_group_id, detail_option_group_id, order_index
)
SELECT
  ver.version_id,
  (SELECT section_id FROM sec WHERE code = i.sec_code),
  i.code,
  i.label,
  i.severity,
  i.required,
  i.allow_na,
  (SELECT state_group_id FROM state),
  (SELECT option_group_id FROM og WHERE og.code = i.detail_group_code),
  i.ord
FROM ver
CROSS JOIN (VALUES
  -- SEC_ROD_FRE
  ('SEC_ROD_FRE','ROD_LLANTAS','Llantas (estado general)','High',true,false,'WheelPositions',1),
  ('SEC_ROD_FRE','ROD_RINES','Rines (deformación/daño)','Medium',true,false,'WheelPositions',2),
  ('SEC_ROD_FRE','ROD_FRENOS_SISTEMA','Frenos (sistema)','Critical',true,false,NULL,3),
  ('SEC_ROD_FRE','ROD_FRENO_MANO','Freno de mano','Critical',true,false,NULL,4),
  -- SEC_SEG
  ('SEC_SEG','SEG_DIRECCION','Dirección','Critical',true,false,NULL,1),
  ('SEC_SEG','SEG_SUSPENSION','Suspensión','High',true,false,'SuspensionAreas',2),
  ('SEC_SEG','SEG_ESPEJOS_CRISTALES','Espejos y cristales (visibilidad)','Medium',true,false,NULL,3),
  ('SEC_SEG','SEG_LIMPIA','Limpiaparabrisas (plumillas + lava)','Medium',true,false,NULL,4),
  ('SEC_SEG','SEG_CINTURONES','Cinturones de seguridad','Critical',true,false,'SeatbeltPositions',5),
  ('SEC_SEG','SEG_AIRBAGS','Airbags (si aplica)','High',false,true,NULL,6),
  -- SEC_FLU
  ('SEC_FLU','FLU_ACEITE_MOTOR','Aceite de motor','High',true,false,NULL,1),
  ('SEC_FLU','FLU_LIQ_FRENOS','Líquido de frenos','Critical',true,false,NULL,2),
  ('SEC_FLU','FLU_REFRIGERANTE','Refrigerante (radiador)','High',true,false,NULL,3),
  ('SEC_FLU','FLU_OTROS','Otros fluidos','Medium',false,false,'OtherFluids',4),
  -- SEC_TAB
  ('SEC_TAB','TAB_INSTRUMENTOS','Instrumentos / indicadores','High',true,false,'InstrumentFaults',1),
  ('SEC_TAB','TAB_PITO','Pito (bocina)','Critical',true,false,NULL,2),
  -- SEC_LUZ
  ('SEC_LUZ','LUZ_EXTERNAS','Luces externas (conjunto)','High',true,false,'ExternalLights',1),
  ('SEC_LUZ','LUZ_FRENO','Luz de freno','Critical',true,false,NULL,2),
  -- SEC_CONF
  ('SEC_CONF','CONF_ASEO','Aseo y presentación (int/ext)','Low',false,false,NULL,1),
  ('SEC_CONF','CONF_CLIMA','Climatización (A/C y ventilación)','Low',false,false,NULL,2),
  ('SEC_CONF','CONF_CABINA','Cabina (sillas y luces internas)','Low',false,false,NULL,3),
  -- SEC_REG
  ('SEC_REG','REG_EXTINTOR','Extintor (presencia/vigencia)','Critical',true,false,NULL,1),
  ('SEC_REG','REG_EQUIPO','Equipo reglamentario','High',true,false,'RegulatoryItems',2),
  ('SEC_REG','REG_BOTIQUIN','Botiquín vehicular','Medium',false,false,'FirstAidItems',3),
  -- SEC_OTR
  ('SEC_OTR','OTR_ELECTRICO','Sistema eléctrico (general)','High',true,false,NULL,1),
  ('SEC_OTR','OTR_TREN_MOTRIZ','Tren motriz (transmisión/embrague/encendido)','High',true,false,NULL,2),
  ('SEC_OTR','OTR_EXOSTO','Escape (exosto)','Medium',false,false,NULL,3),
  ('SEC_OTR','OTR_ALARMA_REVERSA','Alarma de reversa (si aplica)','Medium',false,true,NULL,4),
  ('SEC_OTR','OTR_PLACAS','Placas (legibilidad/presencia)','High',true,false,NULL,5)
) AS i(sec_code, code, label, severity, required, allow_na, detail_group_code, ord)
WHERE NOT EXISTS (
  SELECT 1 FROM checklist_item ci
  WHERE ci.version_id = ver.version_id
    AND ci.code = i.code
);

-- ============================================================================
-- Notas para el equipo (no ejecutables, solo documentación)
-- ---------------------------------------------------------------------------
-- 1) La lógica de cierre/validación del checklist (Submit) vive en Spring Boot:
--    - Odómetro obligatorio si instance.vehicle_id no es NULL y odometer >= 0
--    - Si Estado != OK => comentario requerido (mín 5–10 chars)
--    - Si el ítem tiene detail_option_group_id y Estado != OK => al menos un detalle en checklist_response_option
--    - Si ítem "Critical" = NOOP => evidencias (foto) requeridas
--    - Cálculo de condition_general: NO_APTO / APTO_RESTRICCIONES / APTO
-- 2) Los OptionGroup/OptionItem permiten reutilizar catálogos en múltiples ítems.
-- 3) La versión (checklist_version) garantiza trazabilidad: una instancia referencia
--    exactamente qué diseño aplicaba en ese momento.
-- ============================================================================
