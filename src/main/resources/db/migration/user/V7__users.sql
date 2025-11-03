-- ============================================
-- Insertar Roles Iniciales
-- ============================================
INSERT INTO roles (role_name) VALUES ('ADMIN');
INSERT INTO roles (role_name) VALUES ('SUPERVISOR');
INSERT INTO roles (role_name) VALUES ('FLEET_MANAGER');
INSERT INTO roles (role_name) VALUES ('DRIVER');
INSERT INTO roles (role_name) VALUES ('MECHANIC');

-- ============================================
-- Insertar Permisos Iniciales
-- ============================================
INSERT INTO permissions (name) VALUES ('READ');
INSERT INTO permissions (name) VALUES ('CREATE');
INSERT INTO permissions (name) VALUES ('UPDATE');
INSERT INTO permissions (name) VALUES ('DELETE');
INSERT INTO permissions (name) VALUES ('MANAGE_USERS');
INSERT INTO permissions (name) VALUES ('MANAGE_VEHICLES');
INSERT INTO permissions (name) VALUES ('MANAGE_FLEET');
INSERT INTO permissions (name) VALUES ('VIEW_REPORTS');

-- ============================================
-- Asignar Permisos a Roles (Opcional)
-- ============================================

-- ADMIN: Todos los permisos
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         CROSS JOIN permissions p
WHERE r.role_name = 'ADMIN';

-- SUPERVISOR: Ver, crear y actualizar
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         JOIN permissions p ON p.name IN ('READ', 'CREATE', 'UPDATE', 'VIEW_REPORTS')
WHERE r.role_name = 'SUPERVISOR';

-- FLEET_MANAGER: Gestión de flotas y vehículos
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         JOIN permissions p ON p.name IN ('READ', 'CREATE', 'UPDATE', 'MANAGE_VEHICLES', 'MANAGE_FLEET', 'VIEW_REPORTS')
WHERE r.role_name = 'FLEET_MANAGER';

-- DRIVER: Solo lectura
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         JOIN permissions p ON p.name = 'READ'
WHERE r.role_name = 'DRIVER';

-- MECHANIC: Gestión de vehículos
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         JOIN permissions p ON p.name IN ('READ', 'UPDATE', 'MANAGE_VEHICLES')
WHERE r.role_name = 'MECHANIC';