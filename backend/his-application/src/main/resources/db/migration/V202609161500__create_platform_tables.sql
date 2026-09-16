CREATE TABLE sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(64) NOT NULL,
    employee_id BIGINT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE sys_role (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(64) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE sys_permission (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(128) NOT NULL,
    name VARCHAR(64) NOT NULL,
    module_code VARCHAR(64) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_permission_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_sys_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
    CONSTRAINT fk_sys_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE sys_role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_sys_role_permission_role FOREIGN KEY (role_id) REFERENCES sys_role (id),
    CONSTRAINT fk_sys_role_permission_permission FOREIGN KEY (permission_id) REFERENCES sys_permission (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE operation_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    operator_id BIGINT NULL,
    module_code VARCHAR(64) NOT NULL,
    action VARCHAR(128) NOT NULL,
    target_type VARCHAR(64) NULL,
    target_id VARCHAR(64) NULL,
    detail VARCHAR(1000) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_operation_log_operator (operator_id),
    KEY idx_operation_log_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO sys_role (code, name) VALUES
    ('ROOT', '系统管理员'),
    ('REGISTRATION_CASHIER', '挂号收费员'),
    ('OUTPATIENT_DOCTOR', '门诊医生'),
    ('CHECK_DOCTOR', '检查医生'),
    ('INSPECTION_DOCTOR', '检验医生'),
    ('DISPOSAL_DOCTOR', '处置医生'),
    ('PHARMACY_ADMIN', '药房管理员');

INSERT INTO sys_permission (code, name, module_code) VALUES
    ('platform:manage', '平台管理', 'platform'),
    ('master-data:read', '基础数据查询', 'master-data'),
    ('master-data:write', '基础数据维护', 'master-data'),
    ('registration:write', '挂号收费操作', 'registration'),
    ('outpatient:write', '门诊诊疗操作', 'outpatient'),
    ('medical-tech:check', '检查执行', 'medical-tech'),
    ('medical-tech:inspection', '检验执行', 'medical-tech'),
    ('medical-tech:disposal', '处置执行', 'medical-tech'),
    ('pharmacy:write', '药房操作', 'pharmacy');

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT role.id, permission.id
FROM sys_role role
JOIN sys_permission permission ON
    role.code = 'ROOT'
    OR (role.code = 'REGISTRATION_CASHIER' AND permission.code IN ('master-data:read', 'registration:write'))
    OR (role.code = 'OUTPATIENT_DOCTOR' AND permission.code IN ('master-data:read', 'outpatient:write'))
    OR (role.code = 'CHECK_DOCTOR' AND permission.code IN ('master-data:read', 'medical-tech:check'))
    OR (role.code = 'INSPECTION_DOCTOR' AND permission.code IN ('master-data:read', 'medical-tech:inspection'))
    OR (role.code = 'DISPOSAL_DOCTOR' AND permission.code IN ('master-data:read', 'medical-tech:disposal'))
    OR (role.code = 'PHARMACY_ADMIN' AND permission.code IN ('master-data:read', 'pharmacy:write'));
