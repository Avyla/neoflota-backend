-- ============================================================================
--  MÓDULO: VEHÍCULOS (PostgreSQL) — ESQUEMA + SEED COMENTADOS
--  Proyecto: Gestión de flotas (Java + Spring Boot + PostgreSQL)
--  Autor: Equipo de Datos (ajustado por Arquitectura/Backend)
--  Descripción:
--     Este script crea la estructura de base de datos para el módulo de
--     Vehículos. Define los catálogos necesarios (marcas, tipos, categorías,
--     combustibles, estados operativos y condición) y la tabla principal
--     vehicle. Está alineado en formato y convenciones con el módulo de
--     Checklists (PKs, naming snake_case, comentarios persistentes).
--
--     ⚠️ Importante:
--       - La lógica de negocio (validaciones específicas por país o flujo)
--         debe implementarse en la capa de servicio (Spring Boot).
--       - Los catálogos se pueblan con valores observados en UI y ejemplos
--         de uso común; pueden extenderse sin romper integridad.
-- ============================================================================

-- Opcional: usar un esquema propio (descomentar si aplica)
-- CREATE SCHEMA flota;
-- SET search_path TO flota, public;

-- ============================================================================
-- A) CATÁLOGOS REUTILIZABLES PARA VEHÍCULOS
-- ============================================================================

CREATE TABLE vehicle_make (
  make_id              BIGSERIAL PRIMARY KEY,
  name                 VARCHAR(100) NOT NULL UNIQUE,
  is_active            BOOLEAN NOT NULL DEFAULT TRUE,
  created_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);
COMMENT ON TABLE vehicle_make IS 'Catálogo de marcas de vehículos (ej: Toyota, Ford, Chevrolet).';
COMMENT ON COLUMN vehicle_make.make_id IS 'PK autoincremental de la marca.';
COMMENT ON COLUMN vehicle_make.name IS 'Nombre único de la marca.';
COMMENT ON COLUMN vehicle_make.is_active IS 'Permite activar/desactivar la marca sin borrarla.';
COMMENT ON COLUMN vehicle_make.created_at IS 'Fecha/hora de creación (UTC).';

CREATE TABLE vehicle_type (
  type_id              BIGSERIAL PRIMARY KEY,
  name                 VARCHAR(120) NOT NULL UNIQUE,
  is_active            BOOLEAN NOT NULL DEFAULT TRUE,
  created_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);
COMMENT ON TABLE vehicle_type IS 'Catálogo de tipos de vehículo (ej: Camión estacas, Volqueta, Automóvil).';
COMMENT ON COLUMN vehicle_type.type_id IS 'PK autoincremental del tipo.';
COMMENT ON COLUMN vehicle_type.name IS 'Nombre único del tipo de vehículo.';
COMMENT ON COLUMN vehicle_type.is_active IS 'Permite activar/desactivar el tipo sin borrarlo.';
COMMENT ON COLUMN vehicle_type.created_at IS 'Fecha/hora de creación (UTC).';

CREATE TABLE vehicle_category (
  category_id          BIGSERIAL PRIMARY KEY,
  name                 VARCHAR(120) NOT NULL UNIQUE,
  is_active            BOOLEAN NOT NULL DEFAULT TRUE,
  created_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);
COMMENT ON TABLE vehicle_category IS 'Catálogo de categorías de vehículo (ej: Carga seca, Maquinaria amarilla).';
COMMENT ON COLUMN vehicle_category.category_id IS 'PK autoincremental de la categoría.';
COMMENT ON COLUMN vehicle_category.name IS 'Nombre único de la categoría del vehículo.';
COMMENT ON COLUMN vehicle_category.is_active IS 'Permite activar/desactivar la categoría sin borrarla.';
COMMENT ON COLUMN vehicle_category.created_at IS 'Fecha/hora de creación (UTC).';

CREATE TABLE vehicle_fuel_type (
  fuel_type_id         BIGSERIAL PRIMARY KEY,
  name                 VARCHAR(50) NOT NULL UNIQUE,
  is_active            BOOLEAN NOT NULL DEFAULT TRUE,
  created_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);
COMMENT ON TABLE vehicle_fuel_type IS 'Catálogo de tipos de combustible (ej: Gasolina, Diesel, Eléctrico, Híbrido).';
COMMENT ON COLUMN vehicle_fuel_type.fuel_type_id IS 'PK autoincremental del tipo de combustible.';
COMMENT ON COLUMN vehicle_fuel_type.name IS 'Nombre único del tipo de combustible.';
COMMENT ON COLUMN vehicle_fuel_type.is_active IS 'Permite activar/desactivar el tipo sin borrarlo.';
COMMENT ON COLUMN vehicle_fuel_type.created_at IS 'Fecha/hora de creación (UTC).';

CREATE TABLE vehicle_status (
  status_id            BIGSERIAL PRIMARY KEY,
  code                 VARCHAR(50) NOT NULL UNIQUE,
  name                 VARCHAR(100) NOT NULL,
  description          VARCHAR(300),
  is_active            BOOLEAN NOT NULL DEFAULT TRUE,
  created_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);
COMMENT ON TABLE vehicle_status IS 'Catálogo de estados operativos de un vehículo (ej: Activo, En Mantenimiento, Inactivo).';
COMMENT ON COLUMN vehicle_status.status_id IS 'PK autoincremental del estado.';
COMMENT ON COLUMN vehicle_status.code IS 'Código único para uso en la aplicación (ej: ACTIVE, IN_REPAIR).';
COMMENT ON COLUMN vehicle_status.name IS 'Nombre visible para el usuario (ej: "Activo").';
COMMENT ON COLUMN vehicle_status.description IS 'Descripción opcional del estado.';
COMMENT ON COLUMN vehicle_status.is_active IS 'Permite activar/desactivar el estado sin borrarlo.';
COMMENT ON COLUMN vehicle_status.created_at IS 'Fecha/hora de creación (UTC).';

CREATE TABLE vehicle_condition (
  condition_id         BIGSERIAL PRIMARY KEY,
  code                 VARCHAR(20)  NOT NULL UNIQUE,   -- GOOD|FAIR|BAD|NA
  name                 VARCHAR(50)  NOT NULL,          -- Bueno|Regular|Malo|No aplica
  order_index          INT NOT NULL DEFAULT 0,
  is_active            BOOLEAN NOT NULL DEFAULT TRUE,
  created_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);
COMMENT ON TABLE vehicle_condition IS 'Catálogo de condición/estado físico del vehículo (Bueno, Regular, Malo, No aplica).';
COMMENT ON COLUMN vehicle_condition.condition_id IS 'PK autoincremental de la condición.';
COMMENT ON COLUMN vehicle_condition.code IS 'Código legible por máquina (GOOD, FAIR, BAD, NA).';
COMMENT ON COLUMN vehicle_condition.name IS 'Etiqueta visible (Bueno/Regular/Malo/No aplica).';
COMMENT ON COLUMN vehicle_condition.order_index IS 'Orden sugerido de despliegue de la condición.';
COMMENT ON COLUMN vehicle_condition.is_active IS 'Permite desactivar sin borrar.';
COMMENT ON COLUMN vehicle_condition.created_at IS 'Fecha/hora de creación (UTC).';

-- ============================================================================
-- B) TABLA PRINCIPAL DE VEHÍCULOS
-- ============================================================================

CREATE TABLE vehicle (
  vehicle_id             BIGSERIAL PRIMARY KEY,
  plate                  VARCHAR(10)  NOT NULL,
  make_id                BIGINT       NOT NULL REFERENCES vehicle_make(make_id),
  model_name             VARCHAR(100) NOT NULL,
  model_year             INTEGER,
  type_id                BIGINT       NOT NULL REFERENCES vehicle_type(type_id),
  category_id            BIGINT       NOT NULL REFERENCES vehicle_category(category_id),
  fuel_type_id           BIGINT       NOT NULL REFERENCES vehicle_fuel_type(fuel_type_id),
  status_id              BIGINT       NOT NULL REFERENCES vehicle_status(status_id),
  condition_id           BIGINT            REFERENCES vehicle_condition(condition_id),
  vin                    VARCHAR(50) UNIQUE,           -- Vehicle Identification Number
  color                  VARCHAR(50),
  current_odometer       INTEGER DEFAULT 0,
  soat_expiration_date   DATE,
  is_active              BOOLEAN NOT NULL DEFAULT TRUE,
  created_by_user_id     BIGINT,
  created_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_by_user_id     BIGINT,
  updated_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT chk_vehicle_model_year CHECK (model_year IS NULL OR (model_year BETWEEN 1950 AND 2099)),
  CONSTRAINT chk_vehicle_odometer CHECK (current_odometer >= 0),
  -- Solo formatos Colombia SIN guion: ABC123 (carro/camión) o ABC12D (moto)
  CONSTRAINT chk_vehicle_plate_colombia_format CHECK (
    plate ~ '^(?:[A-Z]{3}[0-9]{3}|[A-Z]{3}[0-9]{2}[A-Z])$'
  ),
  -- Unicidad directa (se almacena en MAYÚSCULAS por trigger)
  CONSTRAINT uq_vehicle_plate UNIQUE (plate)
);
COMMENT ON TABLE vehicle IS 'Tabla central que almacena la información de cada vehículo de la flota.';
COMMENT ON COLUMN vehicle.vehicle_id IS 'PK autoincremental del vehículo.';
COMMENT ON COLUMN vehicle.plate IS 'Placa en formato Colombia SIN guion: ABC123 (carro/camión) o ABC12D (moto). Se guarda siempre en MAYÚSCULAS.';
COMMENT ON COLUMN vehicle.make_id IS 'FK a la marca del vehículo (ej: Toyota).';
COMMENT ON COLUMN vehicle.model_name IS 'Nombre del modelo (ej: Hilux, Corolla, F-150).';
COMMENT ON COLUMN vehicle.model_year IS 'Año del modelo del vehículo (ej: 2023).';
COMMENT ON COLUMN vehicle.type_id IS 'FK al tipo de vehículo (ej: Camioneta, Camión estacas).';
COMMENT ON COLUMN vehicle.category_id IS 'FK a la categoría del vehículo (ej: Carga seca).';
COMMENT ON COLUMN vehicle.fuel_type_id IS 'FK al tipo de combustible (ej: Diesel).';
COMMENT ON COLUMN vehicle.status_id IS 'FK al estado operativo (ej: Activo).';
COMMENT ON COLUMN vehicle.condition_id IS 'FK a la condición física (Bueno/Regular/Malo/No aplica).';
COMMENT ON COLUMN vehicle.vin IS 'Número de Identificación Vehicular (VIN). Único y opcional.';
COMMENT ON COLUMN vehicle.color IS 'Color principal del vehículo.';
COMMENT ON COLUMN vehicle.current_odometer IS 'Última lectura conocida del odómetro/kilometraje.';
COMMENT ON COLUMN vehicle.soat_expiration_date IS 'Fecha de vencimiento del seguro obligatorio (SOAT).';
COMMENT ON COLUMN vehicle.is_active IS 'Indica si el registro está activo (soft delete lógico).';
COMMENT ON COLUMN vehicle.created_by_user_id IS 'Usuario que creó el registro (trazabilidad).';
COMMENT ON COLUMN vehicle.created_at IS 'Fecha/hora de creación del registro (UTC).';
COMMENT ON COLUMN vehicle.updated_by_user_id IS 'Último usuario que modificó el registro.';
COMMENT ON COLUMN vehicle.updated_at IS 'Fecha/hora de la última modificación (UTC).';

-- Índices recomendados (FKs y columnas de búsqueda frecuentes)
CREATE INDEX idx_vehicle_make_id       ON vehicle(make_id);
CREATE INDEX idx_vehicle_type_id       ON vehicle(type_id);
CREATE INDEX idx_vehicle_category_id   ON vehicle(category_id);
CREATE INDEX idx_vehicle_fuel_type_id  ON vehicle(fuel_type_id);
CREATE INDEX idx_vehicle_status_id     ON vehicle(status_id);
CREATE INDEX idx_vehicle_condition_id  ON vehicle(condition_id);
CREATE INDEX idx_vehicle_is_active     ON vehicle(is_active);
CREATE INDEX idx_vehicle_soat_exp_date ON vehicle(soat_expiration_date);

-- (Opcional) Búsquedas por subcadena de placa (habilitar extensión pg_trgm si se requiere)
-- CREATE EXTENSION IF NOT EXISTS pg_trgm;
-- CREATE INDEX idx_vehicle_plate_trgm ON vehicle USING GIN (plate gin_trgm_ops);

-- ============================================================================
-- C) FUNCIONES Y TRIGGERS (AUDITORÍA Y NORMALIZACIÓN)
-- ============================================================================

-- Actualiza automáticamente `updated_at` en cada UPDATE
CREATE OR REPLACE FUNCTION fn_touch_updated_at()
RETURNS TRIGGER AS $$
BEGIN
   NEW.updated_at = now();
   RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_vehicle_touch_updated_at ON vehicle;
CREATE TRIGGER trg_vehicle_touch_updated_at
BEFORE UPDATE ON vehicle
FOR EACH ROW
EXECUTE PROCEDURE fn_touch_updated_at();

COMMENT ON FUNCTION fn_touch_updated_at IS 'Actualiza automáticamente la columna updated_at con now() en operaciones UPDATE.';
COMMENT ON TRIGGER trg_vehicle_touch_updated_at ON vehicle IS 'Dispara fn_touch_updated_at antes de cada UPDATE en vehicle.';

-- Normaliza `plate` a MAYÚSCULAS en INSERT/UPDATE
CREATE OR REPLACE FUNCTION fn_vehicle_normalize_plate()
RETURNS TRIGGER AS $$
BEGIN
  NEW.plate := UPPER(NEW.plate);
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_vehicle_normalize_plate ON vehicle;
CREATE TRIGGER trg_vehicle_normalize_plate
BEFORE INSERT OR UPDATE ON vehicle
FOR EACH ROW
EXECUTE PROCEDURE fn_vehicle_normalize_plate();

COMMENT ON FUNCTION fn_vehicle_normalize_plate IS 'Normaliza la columna plate a MAYÚSCULAS en INSERT/UPDATE.';
COMMENT ON TRIGGER trg_vehicle_normalize_plate ON vehicle IS 'Normaliza plate a MAYÚSCULAS antes de insertar o actualizar.';

-- ============================================================================
-- D) SEED DE CATÁLOGOS
-- ============================================================================

-- Marcas (de ejemplo + ampliables)
INSERT INTO vehicle_make(name) VALUES
('Toyota'), ('Chevrolet'), ('Ford'), ('Nissan'), ('Mazda'),
('Kia'), ('Hyundai'), ('Renault'), ('Volkswagen'), ('Mercedes-Benz'),
('Kenworth'), ('International')
ON CONFLICT (name) DO NOTHING;

-- Tipos (extraídos de la UI proporcionada)
INSERT INTO vehicle_type(name) VALUES
('Camión estacas'),
('Camión silo granelero o tanque'),
('Camión palet o de reparto'),
('Camión furgón'),
('Doble troque'),
('Volqueta platón'),
('Camioneta'),
('Campero'),
('Automóvil'),
('Tractocamión')
ON CONFLICT (name) DO NOTHING;

-- Categorías (extraídas de la UI proporcionada)
INSERT INTO vehicle_category(name) VALUES
('Carga seca'),
('Carga refrigerante'),
('Automóvil, campero, camioneta'),
('Maquinaria amarilla')
ON CONFLICT (name) DO NOTHING;

-- Tipos de combustible (extraídos de la UI)
INSERT INTO vehicle_fuel_type(name) VALUES
('Gasolina'), ('Diesel'), ('Eléctrico'), ('Híbrido'), ('Gas Natural Vehicular (GNV)')
ON CONFLICT (name) DO NOTHING;

-- Estados operativos
INSERT INTO vehicle_status(code, name, description) VALUES
('ACTIVE',   'Activo',          'El vehículo está operativo y disponible.'),
('IN_REPAIR','En mantenimiento','El vehículo se encuentra en taller o reparación.'),
('INACTIVE', 'Inactivo',        'El vehículo está fuera de servicio.'),
('SOLD',     'Vendido',         'El vehículo ya no pertenece a la flota.')
ON CONFLICT (code) DO NOTHING;

-- Condición operativa (Apto/Apto con restricciones/No apto)
-- Estos códigos coinciden con los resultados del checklist
INSERT INTO vehicle_condition(code, name, order_index) VALUES
('APTO','Apto',0),
('APTO_RESTRICCIONES','Apto con restricciones',1),
('NO_APTO','No apto',2)
ON CONFLICT (code) DO NOTHING;
-- ============================================================================
-- Notas para el equipo (documentación)
-- ---------------------------------------------------------------------------
-- 1) Placa Colombia SIN guion: solo se aceptan ABC123 (carro/camión) y ABC12D (moto).
--    El trigger `fn_vehicle_normalize_plate` guarda SIEMPRE en MAYÚSCULAS.
--    La unicidad es directa sobre `plate` (constraint uq_vehicle_plate).
-- 2) Se separan `status` (operativo) y `condition` (estado físico) porque son
--    selecciones de negocio distintas.
-- 3) Columnas de auditoría + trigger `fn_touch_updated_at` para consistencia temporal.
-- 4) Índices sobre FKs y filtros frecuentes (activo, SOAT). Activar `pg_trgm`
--    si se esperan búsquedas por subcadena de placa.
-- 5) Seeds con `ON CONFLICT DO NOTHING` para idempotencia.
-- ============================================================================
