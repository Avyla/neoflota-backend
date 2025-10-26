-- ============================================================================
--  V2: Migración a BLOB + UUID en checklist_attachment  (PostgreSQL)
--  - Requiere extensión pgcrypto (o uuid-ossp como alternativa)
-- ============================================================================

-- Habilitar generador de UUID (elige UNA de las dos líneas siguientes):
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1) Agregar columna UUID (temporalmente nullable)
ALTER TABLE checklist_attachment ADD COLUMN id UUID;

-- 2) Poblar con UUID
UPDATE checklist_attachment
SET id = COALESCE(id,
                  gen_random_uuid()  -- si usas pgcrypto
                  -- uuid_generate_v4()  -- si usas uuid-ossp
                 );

-- 3) Asegurar NOT NULL
ALTER TABLE checklist_attachment ALTER COLUMN id SET NOT NULL;

-- 4) Quitar PK antigua y poner nueva en 'id'
DO $$
DECLARE
  pk_name text;
BEGIN
  SELECT conname INTO pk_name
  FROM pg_constraint
  WHERE conrelid = 'checklist_attachment'::regclass
    AND contype = 'p';
  IF pk_name IS NOT NULL THEN
    EXECUTE format('ALTER TABLE checklist_attachment DROP CONSTRAINT %I', pk_name);
  END IF;
END $$;

ALTER TABLE checklist_attachment ADD CONSTRAINT pk_checklist_attachment PRIMARY KEY (id);

-- 5) Remover columnas antiguas de storage por URL
ALTER TABLE checklist_attachment
  DROP COLUMN IF EXISTS file_url,
  DROP COLUMN IF EXISTS mime_type,
  DROP COLUMN IF EXISTS caption;

-- 6) Agregar columnas BLOB + metadatos
ALTER TABLE checklist_attachment
  ADD COLUMN IF NOT EXISTS filename VARCHAR(255) NOT NULL DEFAULT 'file',
  ADD COLUMN IF NOT EXISTS type     VARCHAR(100) NOT NULL DEFAULT 'application/octet-stream',
  ADD COLUMN IF NOT EXISTS size     BIGINT       NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS data     BYTEA        NOT NULL DEFAULT ''::bytea;

-- 7) Asegurar XOR entre response_id e instance_id (ya existía; recrear si hace falta)
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint
     WHERE conname = 'chk_attach_target'
       AND conrelid = 'checklist_attachment'::regclass
  ) THEN
    ALTER TABLE checklist_attachment
      ADD CONSTRAINT chk_attach_target CHECK (
        (CASE WHEN instance_id IS NULL THEN 0 ELSE 1 END) +
        (CASE WHEN response_id IS NULL THEN 0 ELSE 1 END) = 1
      );
  END IF;
END $$;

-- 8) Única evidencia por respuesta
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint
     WHERE conname = 'uk_attachment_response'
       AND conrelid = 'checklist_attachment'::regclass
  ) THEN
    ALTER TABLE checklist_attachment
      ADD CONSTRAINT uk_attachment_response UNIQUE (response_id);
  END IF;
END $$;

-- 9) Índices útiles
CREATE INDEX IF NOT EXISTS idx_attachment_response   ON checklist_attachment (response_id);
CREATE INDEX IF NOT EXISTS idx_attachment_instance   ON checklist_attachment (instance_id);
CREATE INDEX IF NOT EXISTS idx_attachment_created_at ON checklist_attachment (created_at);

-- 10) (Opcional) Comentarios
COMMENT ON COLUMN checklist_attachment.id      IS 'UUID PK de la evidencia';
COMMENT ON COLUMN checklist_attachment.data    IS 'Contenido binario (BLOB)';
COMMENT ON COLUMN checklist_attachment.type    IS 'MIME real';
COMMENT ON COLUMN checklist_attachment.size    IS 'Tamaño en bytes';
COMMENT ON CONSTRAINT uk_attachment_response ON checklist_attachment IS 'Máximo 1 evidencia por respuesta';
