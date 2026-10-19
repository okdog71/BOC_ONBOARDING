-- V11: 수급 산출물(②) 실제 파일 첨부. 파일 본문은 디스크(app.storage.dir)에, 메타데이터만 DB 에 둔다.
CREATE TABLE supply_doc_file (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    supply_doc_id BIGINT       NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    stored_name   VARCHAR(100) NOT NULL,
    content_type  VARCHAR(150),
    size_bytes    BIGINT       NOT NULL,
    sha256        VARCHAR(64)  NOT NULL,
    created_at    DATETIME     NOT NULL,
    updated_at    DATETIME     NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_supply_doc_file_stored UNIQUE (stored_name),
    CONSTRAINT fk_supply_doc_file_doc FOREIGN KEY (supply_doc_id) REFERENCES supply_doc (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
CREATE INDEX idx_supply_doc_file_doc ON supply_doc_file (supply_doc_id);
