-- ============================================================================
-- V8__user_soft_delete.sql
-- Agregar soft delete y sincronizar esquema BD con UserEntity
-- ============================================================================

-- 1) Agregar deleted_at para soft delete
ALTER TABLE users ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

-- 2) Agregar campos que faltan en la BD pero están en UserEntity
ALTER TABLE users ADD COLUMN IF NOT EXISTS document_identity_type VARCHAR(20);
ALTER TABLE users ADD COLUMN IF NOT EXISTS document_number VARCHAR(50);

-- 3) Renombrar y ajustar columnas para match con UserEntity
DO $$
BEGIN
    -- Renombrar 'id' a 'user_id' (si es necesario)
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'users' AND column_name = 'id'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'users' AND column_name = 'user_id'
    ) THEN
        ALTER TABLE users RENAME COLUMN id TO user_id;
    END IF;

    -- Renombrar 'phone' a 'phone_number' (si es necesario)
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'users' AND column_name = 'phone'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'users' AND column_name = 'phone_number'
    ) THEN
        ALTER TABLE users RENAME COLUMN phone TO phone_number;
    END IF;

    -- Renombrar 'is_active' a 'is_enable' (si es necesario)
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'users' AND column_name = 'is_active'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'users' AND column_name = 'is_enable'
    ) THEN
        ALTER TABLE users RENAME COLUMN is_active TO is_enable;
    END IF;

    -- Transformar 'is_locked' a 'is_account_no_locked' con lógica invertida
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'users' AND column_name = 'is_locked'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'users' AND column_name = 'is_account_no_locked'
    ) THEN
        -- Agregar nueva columna
        ALTER TABLE users ADD COLUMN is_account_no_locked BOOLEAN NOT NULL DEFAULT TRUE;
        -- Copiar datos invertidos: is_locked=TRUE -> is_account_no_locked=FALSE
        UPDATE users SET is_account_no_locked = NOT is_locked;
        -- Eliminar columna antigua
        ALTER TABLE users DROP COLUMN is_locked;
    END IF;

    -- Agregar is_credentials_non_expired (si no existe)
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'users' AND column_name = 'is_credentials_non_expired'
    ) THEN
        ALTER TABLE users ADD COLUMN is_credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE;
    END IF;

    -- Agregar is_account_non_expired (si no existe)
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'users' AND column_name = 'is_account_non_expired'
    ) THEN
        ALTER TABLE users ADD COLUMN is_account_non_expired BOOLEAN NOT NULL DEFAULT TRUE;
    END IF;
END $$;

-- 4) Índices para búsquedas de usuarios no eliminados
CREATE INDEX IF NOT EXISTS idx_users_deleted_at ON users(deleted_at);
CREATE INDEX IF NOT EXISTS idx_users_active_not_deleted ON users(is_enable, deleted_at)
    WHERE deleted_at IS NULL;

-- 5) Índice compuesto para búsquedas
CREATE INDEX IF NOT EXISTS idx_users_search ON users(username, email)
    WHERE deleted_at IS NULL;

-- 6) Comentarios
COMMENT ON COLUMN users.deleted_at IS 'Fecha/hora de eliminación (soft delete). NULL = activo';
COMMENT ON COLUMN users.document_identity_type IS 'Tipo de documento (CC, CE, TI, etc.)';
COMMENT ON COLUMN users.document_number IS 'Número de documento de identidad';

-- ============================================================================
-- Notas:
-- - deleted_at NULL = usuario activo
-- - deleted_at NOT NULL = usuario eliminado (soft delete)
-- - Al eliminar, también se debe actualizar is_enable = FALSE
-- ============================================================================