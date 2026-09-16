ALTER TABLE payment_transaction
    ADD COLUMN reason VARCHAR(500) NULL AFTER original_transaction_id;

CREATE TABLE payment_transaction_item (
    transaction_id BIGINT NOT NULL,
    charge_item_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (transaction_id, charge_item_id),
    KEY idx_payment_transaction_item_charge (charge_item_id, transaction_id),
    CONSTRAINT chk_payment_transaction_item_amount CHECK (amount > 0),
    CONSTRAINT fk_payment_transaction_item_transaction
        FOREIGN KEY (transaction_id) REFERENCES payment_transaction (id),
    CONSTRAINT fk_payment_transaction_item_charge
        FOREIGN KEY (charge_item_id) REFERENCES charge_item (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
