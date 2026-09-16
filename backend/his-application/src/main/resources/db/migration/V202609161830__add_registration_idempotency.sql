ALTER TABLE `register`
    ADD COLUMN request_no VARCHAR(64) NULL AFTER id;

UPDATE `register`
SET request_no = CONCAT('LEGACY-', id)
WHERE request_no IS NULL;

ALTER TABLE `register`
    MODIFY COLUMN request_no VARCHAR(64) NOT NULL,
    ADD CONSTRAINT uk_register_request_no UNIQUE (request_no);
