-- ============================================================================
--  MÓDULO: USUARIOS Y AUTENTICACIÓN (PostgreSQL) — ESQUEMA + SEED
--  Proyecto: Gestión de flotas (Java + Spring Boot + PostgreSQL)
--  Descripción:
--     Este script crea la estructura de base de datos para el sistema de
--     autenticación y autorización basado en JWT con roles y permisos.
--
--     ⚠️ Importante:
--       - La lógica de autenticación JWT se implementa en Spring Security
--       - Los roles y permisos se gestionan mediante RBAC (Role-Based Access Control)
--       - Las contraseñas se almacenan hasheadas con BCrypt
-- ============================================================================

-- ============================================================================
-- A) CREACIÓN DE TABLAS
-- ============================================================================

-- Tabla de Usuarios
CREATE TABLE IF NOT EXISTS users (
    id                  BIGSERIAL PRIMARY KEY,
    username            VARCHAR(50) NOT NULL UNIQUE,
    password            VARCHAR(255) NOT NULL,
    email               VARCHAR(100) NOT NULL UNIQUE,
    first_name          VARCHAR(100),
    last_name           VARCHAR(100),
    phone               VARCHAR(20),
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    is_locked           BOOLEAN NOT NULL DEFAULT FALSE,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    last_login          TIMESTAMPTZ,
    password_changed_at TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by_user_id  BIGINT,
    updated_by_user_id  BIGINT
);

COMMENT ON TABLE users IS 'Tabla de usuarios del sistema con información de autenticación y perfil.';
COMMENT ON COLUMN users.id IS 'PK autoincremental del usuario.';
COMMENT ON COLUMN users.username IS 'Nombre de usuario único para login.';
COMMENT ON COLUMN users.password IS 'Contraseña hasheada con BCrypt.';
COMMENT ON COLUMN users.email IS 'Correo electrónico único del usuario.';
COMMENT ON COLUMN users.first_name IS 'Nombre(s) del usuario.';
COMMENT ON COLUMN users.last_name IS 'Apellido(s) del usuario.';
COMMENT ON COLUMN users.phone IS 'Teléfono de contacto.';
COMMENT ON COLUMN users.is_active IS 'Indica si el usuario está activo en el sistema.';
COMMENT ON COLUMN users.is_locked IS 'Indica si la cuenta está bloqueada por seguridad.';
COMMENT ON COLUMN users.failed_login_attempts IS 'Contador de intentos fallidos de login.';
COMMENT ON COLUMN users.last_login IS 'Fecha/hora del último login exitoso.';
COMMENT ON COLUMN users.password_changed_at IS 'Fecha/hora del último cambio de contraseña.';
COMMENT ON COLUMN users.created_at IS 'Fecha/hora de creación del usuario.';
COMMENT ON COLUMN users.updated_at IS 'Fecha/hora de última actualización.';

-- Tabla de Roles
CREATE TABLE IF NOT EXISTS roles (
    id          BIGSERIAL PRIMARY KEY,
    role_name   VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

COMMENT ON TABLE roles IS 'Catálogo de roles del sistema (ADMIN, SUPERVISOR, etc.).';
COMMENT ON COLUMN roles.id IS 'PK autoincremental del rol.';
COMMENT ON COLUMN roles.role_name IS 'Nombre único del rol (ej: ADMIN, DRIVER).';
COMMENT ON COLUMN roles.description IS 'Descripción del rol y sus responsabilidades.';
COMMENT ON COLUMN roles.is_active IS 'Indica si el rol está activo.';
COMMENT ON COLUMN roles.created_at IS 'Fecha/hora de creación del rol.';

-- Tabla de Permisos
CREATE TABLE IF NOT EXISTS permissions (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

COMMENT ON TABLE permissions IS 'Catálogo de permisos del sistema (READ, CREATE, UPDATE, etc.).';
COMMENT ON COLUMN permissions.id IS 'PK autoincremental del permiso.';
COMMENT ON COLUMN permissions.name IS 'Nombre único del permiso (ej: MANAGE_VEHICLES).';
COMMENT ON COLUMN permissions.description IS 'Descripción del permiso.';
COMMENT ON COLUMN permissions.created_at IS 'Fecha/hora de creación del permiso.';

-- Tabla de relación Usuario-Rol (muchos a muchos)
CREATE TABLE IF NOT EXISTS user_roles (
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id     BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, role_id)
);

COMMENT ON TABLE user_roles IS 'Relación muchos a muchos entre usuarios y roles.';
COMMENT ON COLUMN user_roles.user_id IS 'FK al usuario.';
COMMENT ON COLUMN user_roles.role_id IS 'FK al rol.';
COMMENT ON COLUMN user_roles.assigned_at IS 'Fecha/hora en que se asignó el rol al usuario.';

-- Tabla de relación Rol-Permiso (muchos a muchos)
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id       BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    granted_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (role_id, permission_id)
);

COMMENT ON TABLE role_permissions IS 'Relación muchos a muchos entre roles y permisos.';
COMMENT ON COLUMN role_permissions.role_id IS 'FK al rol.';
COMMENT ON COLUMN role_permissions.permission_id IS 'FK al permiso.';
COMMENT ON COLUMN role_permissions.granted_at IS 'Fecha/hora en que se otorgó el permiso al rol.';

-- ============================================================================
-- B) ÍNDICES
-- ============================================================================

CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_is_active ON users(is_active);
CREATE INDEX IF NOT EXISTS idx_user_roles_user ON user_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role ON user_roles(role_id);
CREATE INDEX IF NOT EXISTS idx_role_permissions_role ON role_permissions(role_id);
CREATE INDEX IF NOT EXISTS idx_role_permissions_permission ON role_permissions(permission_id);

-- ============================================================================
-- C) TRIGGER PARA ACTUALIZAR updated_at
-- ============================================================================

-- Reutilizar la función fn_touch_updated_at si existe, si no, crearla
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_proc WHERE proname = 'fn_touch_updated_at') THEN
        CREATE OR REPLACE FUNCTION fn_touch_updated_at()
        RETURNS TRIGGER AS $func$
        BEGIN
           NEW.updated_at = now();
           RETURN NEW;
        END;
        $func$ LANGUAGE plpgsql;
    END IF;
END $$;

DROP TRIGGER IF EXISTS trg_users_touch_updated_at ON users;
CREATE TRIGGER trg_users_touch_updated_at
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE PROCEDURE fn_touch_updated_at();

COMMENT ON TRIGGER trg_users_touch_updated_at ON users IS 'Actualiza automáticamente updated_at en cada UPDATE.';

-- ============================================================================
-- D) INSERCIÓN DE DATOS INICIALES (IDEMPOTENTE)
-- ============================================================================

-- ============================================
-- Insertar Roles Iniciales (Idempotente)
-- ============================================
INSERT INTO roles (role_name, description)
VALUES
    ('ADMIN', 'Administrador del sistema con acceso total'),
    ('SUPERVISOR', 'Supervisor con permisos de gestión y reportes'),
    ('FLEET_MANAGER', 'Gestor de flotas con permisos sobre vehículos y conductores'),
    ('DRIVER', 'Conductor con permisos de solo lectura'),
    ('MECHANIC', 'Mecánico con permisos para gestionar mantenimiento de vehículos')
ON CONFLICT (role_name) DO NOTHING;

-- ============================================
-- Insertar Permisos Iniciales (Idempotente)
-- ============================================
INSERT INTO permissions (name, description)
VALUES
    ('READ', 'Permiso para leer/ver información'),
    ('CREATE', 'Permiso para crear nuevos registros'),
    ('UPDATE', 'Permiso para actualizar registros existentes'),
    ('DELETE', 'Permiso para eliminar registros'),
    ('MANAGE_USERS', 'Permiso para gestionar usuarios del sistema'),
    ('MANAGE_VEHICLES', 'Permiso para gestionar vehículos'),
    ('MANAGE_FLEET', 'Permiso para gestionar la flota completa'),
    ('VIEW_REPORTS', 'Permiso para ver reportes y dashboards')
ON CONFLICT (name) DO NOTHING;

-- ============================================
-- Asignar Permisos a Roles (Idempotente)
-- ============================================

-- ADMIN: Todos los permisos
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.role_name = 'ADMIN'
ON CONFLICT DO NOTHING;

-- SUPERVISOR: Ver, crear, actualizar y reportes
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN ('READ', 'CREATE', 'UPDATE', 'VIEW_REPORTS')
WHERE r.role_name = 'SUPERVISOR'
ON CONFLICT DO NOTHING;

-- FLEET_MANAGER: Gestión de flotas y vehículos
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN ('READ', 'CREATE', 'UPDATE', 'MANAGE_VEHICLES', 'MANAGE_FLEET', 'VIEW_REPORTS')
WHERE r.role_name = 'FLEET_MANAGER'
ON CONFLICT DO NOTHING;

-- DRIVER: Solo lectura
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name = 'READ'
WHERE r.role_name = 'DRIVER'
ON CONFLICT DO NOTHING;

-- MECHANIC: Gestión de vehículos
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN ('READ', 'UPDATE', 'MANAGE_VEHICLES')
WHERE r.role_name = 'MECHANIC'
ON CONFLICT DO NOTHING;

-- ============================================================================
-- Notas para el equipo (documentación)
-- ---------------------------------------------------------------------------
-- 1) Sistema de autenticación basado en JWT implementado en Spring Security
-- 2) RBAC (Role-Based Access Control): Usuarios -> Roles -> Permisos
-- 3) Las contraseñas NUNCA se almacenan en texto plano, siempre BCrypt
-- 4) El trigger actualiza automáticamente updated_at en cada UPDATE de users
-- 5) Todas las inserciones usan ON CONFLICT para ser idempotentes
-- 6) Los usuarios reales se crean desde la aplicación, no en migraciones
-- ============================================================================