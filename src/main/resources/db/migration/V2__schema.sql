-- V2: catchup_action, supply_doc, access_item, mega_process, interface_link, doc_reliability
-- (MySQL 예약어 usage/procedure/data 는 *_desc 컬럼명으로 회피)

CREATE TABLE catchup_action (
    id BIGINT NOT NULL AUTO_INCREMENT,
    category           VARCHAR(100) NOT NULL,
    action             VARCHAR(1000) NOT NULL,
    purpose            VARCHAR(1000),
    timing             VARCHAR(20),
    owner              VARCHAR(100),
    kpi                VARCHAR(1000),
    status             VARCHAR(20) NOT NULL,
    note               VARCHAR(1000),
    created_at         DATETIME NOT NULL,
    updated_at         DATETIME NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_catchup_action_status ON catchup_action (status);
CREATE INDEX idx_catchup_action_timing ON catchup_action (timing);

CREATE TABLE supply_doc (
    id BIGINT NOT NULL AUTO_INCREMENT,
    category           VARCHAR(100) NOT NULL,
    name               VARCHAR(255) NOT NULL,
    expected_content   VARCHAR(1000),
    usage_purpose      VARCHAR(1000),
    existence          VARCHAR(20),
    location           VARCHAR(500),
    freshness          VARCHAR(20),
    owner              VARCHAR(100),
    supply_status      VARCHAR(20) NOT NULL,
    note               VARCHAR(1000),
    created_at         DATETIME NOT NULL,
    updated_at         DATETIME NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_supply_doc_supply_status ON supply_doc (supply_status);
CREATE INDEX idx_supply_doc_category ON supply_doc (category);

CREATE TABLE access_item (
    id BIGINT NOT NULL AUTO_INCREMENT,
    category           VARCHAR(100) NOT NULL,
    target             VARCHAR(255) NOT NULL,
    usage_desc         VARCHAR(1000),
    env_scope          VARCHAR(255),
    procedure_desc     VARCHAR(1000),
    status             VARCHAR(20) NOT NULL,
    requested_date     DATE,
    approved_date      DATE,
    owner              VARCHAR(100),
    note               VARCHAR(1000),
    created_at         DATETIME NOT NULL,
    updated_at         DATETIME NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_access_item_status ON access_item (status);
CREATE INDEX idx_access_item_category ON access_item (category);

CREATE TABLE mega_process (
    id BIGINT NOT NULL AUTO_INCREMENT,
    l0                 VARCHAR(255) NOT NULL,
    l1                 VARCHAR(255) NOT NULL,
    l2                 VARCHAR(255),
    l3                 VARCHAR(255),
    related_menu       VARCHAR(1000),
    related_service    VARCHAR(1000),
    external_link      VARCHAR(1000),
    output_data        VARCHAR(1000),
    note               VARCHAR(1000),
    created_at         DATETIME NOT NULL,
    updated_at         DATETIME NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_mega_process_l0 ON mega_process (l0);

CREATE TABLE interface_link (
    id BIGINT NOT NULL AUTO_INCREMENT,
    system_name        VARCHAR(255) NOT NULL,
    direction          VARCHAR(50),
    interface_name     VARCHAR(255),
    usage_desc         VARCHAR(1000),
    auth               VARCHAR(255),
    data_desc          VARCHAR(1000),
    dev_check          VARCHAR(20),
    note               VARCHAR(1000),
    created_at         DATETIME NOT NULL,
    updated_at         DATETIME NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_interface_link_dev_check ON interface_link (dev_check);

CREATE TABLE doc_reliability (
    id BIGINT NOT NULL AUTO_INCREMENT,
    doc_name           VARCHAR(255) NOT NULL,
    source             VARCHAR(500),
    last_updated       VARCHAR(100),
    freshness          VARCHAR(20),
    code_fit           VARCHAR(20),
    grade              VARCHAR(10),
    verify_method      VARCHAR(1000),
    gap                VARCHAR(1000),
    owner              VARCHAR(100),
    note               VARCHAR(1000),
    created_at         DATETIME NOT NULL,
    updated_at         DATETIME NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_doc_reliability_grade ON doc_reliability (grade);
CREATE INDEX idx_doc_reliability_freshness ON doc_reliability (freshness);
