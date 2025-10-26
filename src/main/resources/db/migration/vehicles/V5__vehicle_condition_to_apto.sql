-- ============================================================================
-- V5__vehicle_condition_to_apto.sql
-- Alinear vehicle.condition_id con estado operativo de checklist (instancia)
-- APTO | APTO_RESTRICCIONES | NO_APTO + RTM + documentos del vehículo
-- ============================================================================

-- 1) Asegurar RTM en vehicle (idempotente)
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
     WHERE table_name = 'vehicle' AND column_name = 'rtm_expiration_date'
  ) THEN
    ALTER TABLE vehicle ADD COLUMN rtm_expiration_date DATE;
  END IF;
END $$;

-- -- 2) Sembrar catálogo objetivo (APTO / APTO_RESTRICCIONES / NO_APTO) si faltan
-- INSERT INTO vehicle_condition(code, name, order_index, is_active)
-- SELECT v.code, v.name, v.order_index, TRUE
-- FROM (VALUES
--   ('APTO','Apto',0),
--   ('APTO_RESTRICCIONES','Apto con restricciones',1),
--   ('NO_APTO','No apto',2)
-- ) AS v(code, name, order_index)
-- WHERE NOT EXISTS (SELECT 1 FROM vehicle_condition vc WHERE vc.code = v.code);
--
-- -- 3) Migrar referencias de vehículos desde códigos antiguos (GOOD/FAIR/BAD/NA)
-- --    Map:
-- --      GOOD -> APTO
-- --      FAIR -> APTO_RESTRICCIONES
-- --      BAD  -> NO_APTO
-- --      NA   -> APTO_RESTRICCIONES   -- (si había NA, lo llevamos a “con restricciones”)
-- WITH oldc AS (
--   SELECT condition_id, code FROM vehicle_condition
--    WHERE code IN ('GOOD','FAIR','BAD','NA')
-- ), new_map AS (
--   SELECT 'GOOD' AS old_code, 'APTO' AS new_code UNION ALL
--   SELECT 'FAIR','APTO_RESTRICCIONES' UNION ALL
--   SELECT 'BAD','NO_APTO' UNION ALL
--   SELECT 'NA','APTO_RESTRICCIONES'
-- ), newc AS (
--   SELECT vc.condition_id, vc.code
--     FROM vehicle_condition vc
--    WHERE vc.code IN ('APTO','APTO_RESTRICCIONES','NO_APTO')
-- )
-- UPDATE vehicle v
--    SET condition_id = nc.condition_id
--   FROM oldc oc
--   JOIN new_map m ON oc.code = m.old_code
--   JOIN newc nc  ON nc.code = m.new_code
--  WHERE v.condition_id = oc.condition_id;

-- -- 4) Limpiar filas antiguas si quedaron (evitar colisiones de UNIQUE(code))
-- DELETE FROM vehicle_condition
--  WHERE code IN ('GOOD','FAIR','BAD','NA')
--    AND code NOT IN ('APTO','APTO_RESTRICCIONES','NO_APTO');

-- 5) Documentos del vehículo (SOAT/RTM) — histórico e ID por UUID
CREATE TABLE IF NOT EXISTS vehicle_document (
  document_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  vehicle_id         BIGINT NOT NULL REFERENCES vehicle(vehicle_id) ON DELETE CASCADE,
  doc_type           VARCHAR(10) NOT NULL CHECK (doc_type IN ('SOAT','RTM')),
  number             VARCHAR(80),
  issuer             VARCHAR(120),
  issued_at          DATE,
  expiration_date    DATE,
  filename           VARCHAR(255),
  mime_type          VARCHAR(100),
  size               BIGINT,
  data               BYTEA,
  created_by_user_id BIGINT,
  created_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_vehicle_document_vehicle ON vehicle_document(vehicle_id);
CREATE INDEX IF NOT EXISTS idx_vehicle_document_type    ON vehicle_document(doc_type);

COMMENT ON TABLE vehicle_document IS 'Documentos asociados al vehículo (SOAT, RTM), con histórico';
COMMENT ON COLUMN vehicle_document.document_id IS 'Identificador único del documento (UUID)';
COMMENT ON COLUMN vehicle_document.vehicle_id IS 'Referencia al vehículo asociado';
COMMENT ON COLUMN vehicle_document.doc_type IS 'Tipo de documento: SOAT o RTM';
COMMENT ON COLUMN vehicle_document.data IS 'Contenido binario del documento (PDF/imagen)';
