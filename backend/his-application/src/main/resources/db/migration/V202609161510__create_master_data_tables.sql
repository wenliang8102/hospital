CREATE TABLE department (
    id BIGINT NOT NULL AUTO_INCREMENT,
    dept_code VARCHAR(64) NOT NULL,
    dept_name VARCHAR(64) NOT NULL,
    dept_type VARCHAR(32) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    UNIQUE KEY uk_department_code (dept_code),
    KEY idx_department_type_active (dept_type, active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE regist_level (
    id BIGINT NOT NULL AUTO_INCREMENT,
    regist_code VARCHAR(64) NOT NULL,
    regist_name VARCHAR(64) NOT NULL,
    regist_fee DECIMAL(10, 2) NOT NULL DEFAULT 0,
    regist_quota INT NOT NULL DEFAULT 0,
    sequence_no INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    UNIQUE KEY uk_regist_level_code (regist_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE settle_category (
    id BIGINT NOT NULL AUTO_INCREMENT,
    settle_code VARCHAR(64) NOT NULL,
    settle_name VARCHAR(64) NOT NULL,
    sequence_no INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    UNIQUE KEY uk_settle_category_code (settle_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE scheduling (
    id BIGINT NOT NULL AUTO_INCREMENT,
    rule_name VARCHAR(64) NOT NULL,
    week_rule CHAR(14) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT chk_scheduling_week_rule CHECK (week_rule REGEXP '^[01]{14}$')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE disease (
    id BIGINT NOT NULL AUTO_INCREMENT,
    disease_code VARCHAR(50) NOT NULL,
    disease_name VARCHAR(255) NOT NULL,
    disease_icd VARCHAR(50) NULL,
    disease_category VARCHAR(50) NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    UNIQUE KEY uk_disease_code (disease_code),
    KEY idx_disease_name (disease_name),
    KEY idx_disease_icd (disease_icd)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE drug_info (
    id BIGINT NOT NULL AUTO_INCREMENT,
    drug_code VARCHAR(64) NOT NULL,
    drug_name VARCHAR(255) NOT NULL,
    drug_format VARCHAR(255) NOT NULL,
    drug_unit VARCHAR(16) NOT NULL,
    manufacturer VARCHAR(255) NULL,
    drug_dosage VARCHAR(64) NULL,
    drug_type VARCHAR(64) NULL,
    drug_price DECIMAL(10, 2) NOT NULL DEFAULT 0,
    mnemonic_code VARCHAR(64) NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_drug_info_code (drug_code),
    KEY idx_drug_info_name (drug_name),
    KEY idx_drug_info_mnemonic (mnemonic_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE medical_technology (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tech_code VARCHAR(64) NOT NULL,
    tech_name VARCHAR(128) NOT NULL,
    tech_format VARCHAR(64) NULL,
    tech_price DECIMAL(10, 2) NOT NULL DEFAULT 0,
    tech_type VARCHAR(32) NOT NULL,
    price_type VARCHAR(64) NULL,
    department_id BIGINT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    UNIQUE KEY uk_medical_technology_code (tech_code),
    KEY idx_medical_technology_type (tech_type, active),
    CONSTRAINT fk_medical_technology_department FOREIGN KEY (department_id) REFERENCES department (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE employee (
    id BIGINT NOT NULL AUTO_INCREMENT,
    real_name VARCHAR(64) NOT NULL,
    department_id BIGINT NOT NULL,
    regist_level_id BIGINT NULL,
    scheduling_id BIGINT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_employee_department_active (department_id, active),
    CONSTRAINT fk_employee_department FOREIGN KEY (department_id) REFERENCES department (id),
    CONSTRAINT fk_employee_regist_level FOREIGN KEY (regist_level_id) REFERENCES regist_level (id),
    CONSTRAINT fk_employee_scheduling FOREIGN KEY (scheduling_id) REFERENCES scheduling (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE sys_user
    ADD CONSTRAINT uk_sys_user_employee UNIQUE (employee_id),
    ADD CONSTRAINT fk_sys_user_employee FOREIGN KEY (employee_id) REFERENCES employee (id);

INSERT INTO regist_level (regist_code, regist_name, regist_fee, regist_quota, sequence_no) VALUES
    ('GENERAL', '普通号', 8.00, 100, 1),
    ('EXPERT', '专家号', 20.00, 40, 2);

INSERT INTO settle_category (settle_code, settle_name, sequence_no) VALUES
    ('SELF_PAY', '自费', 1),
    ('MEDICAL_INSURANCE', '医保', 2);

INSERT INTO scheduling (rule_name, week_rule) VALUES
    ('全周排班', '11111111111111'),
    ('工作日排班', '00111111111000');

