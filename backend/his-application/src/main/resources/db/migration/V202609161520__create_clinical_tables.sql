CREATE TABLE `register` (
    id BIGINT NOT NULL AUTO_INCREMENT,
    case_number VARCHAR(64) NOT NULL,
    real_name VARCHAR(64) NOT NULL,
    gender VARCHAR(16) NOT NULL,
    card_number VARCHAR(32) NULL,
    birthday DATE NULL,
    age INT NULL,
    age_type VARCHAR(16) NULL,
    home_address VARCHAR(255) NULL,
    visit_date DATETIME NOT NULL,
    noon VARCHAR(16) NOT NULL,
    department_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    regist_level_id BIGINT NOT NULL,
    settle_category_id BIGINT NOT NULL,
    is_book BOOLEAN NOT NULL DEFAULT FALSE,
    regist_method VARCHAR(32) NOT NULL,
    visit_state VARCHAR(32) NOT NULL DEFAULT 'REGISTERED',
    regist_money DECIMAL(10, 2) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_register_case_number (case_number),
    KEY idx_register_doctor_date_state (employee_id, visit_date, visit_state),
    KEY idx_register_department_date (department_id, visit_date),
    CONSTRAINT fk_register_department FOREIGN KEY (department_id) REFERENCES department (id),
    CONSTRAINT fk_register_employee FOREIGN KEY (employee_id) REFERENCES employee (id),
    CONSTRAINT fk_register_regist_level FOREIGN KEY (regist_level_id) REFERENCES regist_level (id),
    CONSTRAINT fk_register_settle_category FOREIGN KEY (settle_category_id) REFERENCES settle_category (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE medical_record (
    id BIGINT NOT NULL AUTO_INCREMENT,
    register_id BIGINT NOT NULL,
    chief_complaint VARCHAR(500) NULL,
    present_illness TEXT NULL,
    present_treatment TEXT NULL,
    past_history TEXT NULL,
    allergy_history TEXT NULL,
    physical_examination TEXT NULL,
    examination_proposal TEXT NULL,
    precaution TEXT NULL,
    diagnosis TEXT NULL,
    treatment_plan TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_medical_record_register (register_id),
    CONSTRAINT fk_medical_record_register FOREIGN KEY (register_id) REFERENCES `register` (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE medical_record_disease (
    medical_record_id BIGINT NOT NULL,
    disease_id BIGINT NOT NULL,
    diagnosis_type VARCHAR(32) NOT NULL DEFAULT 'FINAL',
    PRIMARY KEY (medical_record_id, disease_id),
    CONSTRAINT fk_medical_record_disease_record FOREIGN KEY (medical_record_id) REFERENCES medical_record (id),
    CONSTRAINT fk_medical_record_disease_disease FOREIGN KEY (disease_id) REFERENCES disease (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE check_request (
    id BIGINT NOT NULL AUTO_INCREMENT,
    register_id BIGINT NOT NULL,
    medical_technology_id BIGINT NOT NULL,
    request_info VARCHAR(1000) NULL,
    body_position VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    executor_employee_id BIGINT NULL,
    result_employee_id BIGINT NULL,
    executed_at DATETIME NULL,
    result TEXT NULL,
    state VARCHAR(32) NOT NULL DEFAULT 'CREATED',
    remark VARCHAR(1000) NULL,
    PRIMARY KEY (id),
    KEY idx_check_request_register (register_id),
    KEY idx_check_request_state_created (state, created_at),
    CONSTRAINT fk_check_request_register FOREIGN KEY (register_id) REFERENCES `register` (id),
    CONSTRAINT fk_check_request_technology FOREIGN KEY (medical_technology_id) REFERENCES medical_technology (id),
    CONSTRAINT fk_check_request_executor FOREIGN KEY (executor_employee_id) REFERENCES employee (id),
    CONSTRAINT fk_check_request_result_employee FOREIGN KEY (result_employee_id) REFERENCES employee (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE inspection_request (
    id BIGINT NOT NULL AUTO_INCREMENT,
    register_id BIGINT NOT NULL,
    medical_technology_id BIGINT NOT NULL,
    request_info VARCHAR(1000) NULL,
    body_position VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    executor_employee_id BIGINT NULL,
    result_employee_id BIGINT NULL,
    executed_at DATETIME NULL,
    result TEXT NULL,
    state VARCHAR(32) NOT NULL DEFAULT 'CREATED',
    remark VARCHAR(1000) NULL,
    PRIMARY KEY (id),
    KEY idx_inspection_request_register (register_id),
    KEY idx_inspection_request_state_created (state, created_at),
    CONSTRAINT fk_inspection_request_register FOREIGN KEY (register_id) REFERENCES `register` (id),
    CONSTRAINT fk_inspection_request_technology FOREIGN KEY (medical_technology_id) REFERENCES medical_technology (id),
    CONSTRAINT fk_inspection_request_executor FOREIGN KEY (executor_employee_id) REFERENCES employee (id),
    CONSTRAINT fk_inspection_request_result_employee FOREIGN KEY (result_employee_id) REFERENCES employee (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE disposal_request (
    id BIGINT NOT NULL AUTO_INCREMENT,
    register_id BIGINT NOT NULL,
    medical_technology_id BIGINT NOT NULL,
    request_info VARCHAR(1000) NULL,
    body_position VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    executor_employee_id BIGINT NULL,
    result_employee_id BIGINT NULL,
    executed_at DATETIME NULL,
    result TEXT NULL,
    state VARCHAR(32) NOT NULL DEFAULT 'CREATED',
    remark VARCHAR(1000) NULL,
    PRIMARY KEY (id),
    KEY idx_disposal_request_register (register_id),
    KEY idx_disposal_request_state_created (state, created_at),
    CONSTRAINT fk_disposal_request_register FOREIGN KEY (register_id) REFERENCES `register` (id),
    CONSTRAINT fk_disposal_request_technology FOREIGN KEY (medical_technology_id) REFERENCES medical_technology (id),
    CONSTRAINT fk_disposal_request_executor FOREIGN KEY (executor_employee_id) REFERENCES employee (id),
    CONSTRAINT fk_disposal_request_result_employee FOREIGN KEY (result_employee_id) REFERENCES employee (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE prescription (
    id BIGINT NOT NULL AUTO_INCREMENT,
    register_id BIGINT NOT NULL,
    drug_id BIGINT NOT NULL,
    drug_usage VARCHAR(500) NOT NULL,
    drug_number INT NOT NULL,
    state VARCHAR(32) NOT NULL DEFAULT 'CREATED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    dispensed_at DATETIME NULL,
    dispensed_by BIGINT NULL,
    PRIMARY KEY (id),
    KEY idx_prescription_register (register_id),
    KEY idx_prescription_state_created (state, created_at),
    CONSTRAINT chk_prescription_drug_number CHECK (drug_number > 0),
    CONSTRAINT fk_prescription_register FOREIGN KEY (register_id) REFERENCES `register` (id),
    CONSTRAINT fk_prescription_drug FOREIGN KEY (drug_id) REFERENCES drug_info (id),
    CONSTRAINT fk_prescription_dispenser FOREIGN KEY (dispensed_by) REFERENCES employee (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE charge_item (
    id BIGINT NOT NULL AUTO_INCREMENT,
    register_id BIGINT NOT NULL,
    item_type VARCHAR(32) NOT NULL,
    source_id BIGINT NOT NULL,
    item_name VARCHAR(255) NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    total_amount DECIMAL(10, 2) NOT NULL,
    state VARCHAR(32) NOT NULL DEFAULT 'UNPAID',
    paid_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_charge_item_source (item_type, source_id),
    KEY idx_charge_item_register_state (register_id, state),
    CONSTRAINT chk_charge_item_quantity CHECK (quantity > 0),
    CONSTRAINT fk_charge_item_register FOREIGN KEY (register_id) REFERENCES `register` (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE payment_transaction (
    id BIGINT NOT NULL AUTO_INCREMENT,
    transaction_no VARCHAR(64) NOT NULL,
    register_id BIGINT NOT NULL,
    transaction_type VARCHAR(16) NOT NULL,
    payment_method VARCHAR(32) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'SUCCESS',
    operator_user_id BIGINT NOT NULL,
    original_transaction_id BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_payment_transaction_no (transaction_no),
    KEY idx_payment_transaction_register (register_id, created_at),
    CONSTRAINT chk_payment_transaction_amount CHECK (amount > 0),
    CONSTRAINT fk_payment_transaction_register FOREIGN KEY (register_id) REFERENCES `register` (id),
    CONSTRAINT fk_payment_transaction_operator FOREIGN KEY (operator_user_id) REFERENCES sys_user (id),
    CONSTRAINT fk_payment_transaction_original FOREIGN KEY (original_transaction_id) REFERENCES payment_transaction (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE drug_stock (
    drug_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (drug_id),
    CONSTRAINT chk_drug_stock_quantity CHECK (quantity >= 0),
    CONSTRAINT fk_drug_stock_drug FOREIGN KEY (drug_id) REFERENCES drug_info (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE drug_stock_transaction (
    id BIGINT NOT NULL AUTO_INCREMENT,
    drug_id BIGINT NOT NULL,
    prescription_id BIGINT NULL,
    transaction_type VARCHAR(32) NOT NULL,
    quantity INT NOT NULL,
    quantity_before INT NOT NULL,
    quantity_after INT NOT NULL,
    operator_user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_drug_stock_transaction_drug (drug_id, created_at),
    CONSTRAINT chk_drug_stock_transaction_quantity CHECK (quantity > 0),
    CONSTRAINT fk_drug_stock_transaction_drug FOREIGN KEY (drug_id) REFERENCES drug_info (id),
    CONSTRAINT fk_drug_stock_transaction_prescription FOREIGN KEY (prescription_id) REFERENCES prescription (id),
    CONSTRAINT fk_drug_stock_transaction_operator FOREIGN KEY (operator_user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
