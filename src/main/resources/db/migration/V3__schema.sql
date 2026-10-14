-- V3: menu_inventory, issue_queue, raci_assignment, review_log, analysis_card
-- (MySQL 예약어와 충돌하는 컬럼명은 *_desc / *_no 로 회피)

CREATE TABLE menu_inventory (
    id BIGINT NOT NULL AUTO_INCREMENT,
    gnb                VARCHAR(100),
    menu               VARCHAR(255) NOT NULL,
    submenu            VARCHAR(255),
    path               VARCHAR(500),
    domain             VARCHAR(100),
    backend_service    VARCHAR(500),
    data_store         VARCHAR(500),
    external_link      VARCHAR(500),
    permission         VARCHAR(500),
    generation         VARCHAR(20),
    priority           VARCHAR(10),
    known_issue        VARCHAR(500),
    dev_check          VARCHAR(20),
    owner              VARCHAR(100),
    analysis_status    VARCHAR(20) NOT NULL,
    note               VARCHAR(1000),
    created_at         DATETIME NOT NULL,
    updated_at         DATETIME NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_menu_inventory_analysis_status ON menu_inventory (analysis_status);
CREATE INDEX idx_menu_inventory_priority ON menu_inventory (priority);
CREATE INDEX idx_menu_inventory_domain ON menu_inventory (domain);
CREATE INDEX idx_menu_inventory_dev_check ON menu_inventory (dev_check);

CREATE TABLE issue_queue (
    id BIGINT NOT NULL AUTO_INCREMENT,
    type               VARCHAR(20),
    item               VARCHAR(255) NOT NULL,
    content            VARCHAR(1000),
    impact             VARCHAR(1000),
    basis              VARCHAR(1000),
    confirm_with       VARCHAR(255),
    owner              VARCHAR(100),
    status             VARCHAR(20) NOT NULL,
    target_date        DATE,
    action             VARCHAR(1000),
    note               VARCHAR(1000),
    created_at         DATETIME NOT NULL,
    updated_at         DATETIME NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_issue_queue_status ON issue_queue (status);
CREATE INDEX idx_issue_queue_type ON issue_queue (type);

CREATE TABLE raci_assignment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    domain_group       VARCHAR(255) NOT NULL,
    scope              VARCHAR(1000),
    complexity         VARCHAR(10),
    scale              VARCHAR(10),
    generation         VARCHAR(20),
    responsible        VARCHAR(255),
    backup             VARCHAR(255),
    reviewer           VARCHAR(255),
    menu_count         INT,
    status             VARCHAR(20) NOT NULL,
    target_date        DATE,
    note               VARCHAR(1000),
    created_at         DATETIME NOT NULL,
    updated_at         DATETIME NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_raci_assignment_status ON raci_assignment (status);

CREATE TABLE review_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    round_no           INT NOT NULL,
    review_date        DATE,
    target_domain      VARCHAR(255),
    presenter          VARCHAR(255),
    attendees          VARCHAR(1000),
    coverage_pct       INT,
    cross_check        VARCHAR(1000),
    gaps               VARCHAR(1000),
    dev_unconfirmed    VARCHAR(1000),
    action             VARCHAR(1000),
    action_owner       VARCHAR(100),
    target_date        DATE,
    status             VARCHAR(20) NOT NULL,
    created_at         DATETIME NOT NULL,
    updated_at         DATETIME NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_review_log_status ON review_log (status);

CREATE TABLE analysis_card (
    id BIGINT NOT NULL AUTO_INCREMENT,
    menu_name          VARCHAR(255) NOT NULL,
    path               VARCHAR(500),
    domain             VARCHAR(100),
    backend_service    VARCHAR(500),
    generation         VARCHAR(20),
    summary            VARCHAR(1000),
    scenario           VARCHAR(2000),
    input              VARCHAR(1000),
    output             VARCHAR(1000),
    backend_mapping    VARCHAR(1000),
    data_store         VARCHAR(500),
    external_link      VARCHAR(500),
    permission         VARCHAR(500),
    related_process    VARCHAR(500),
    known_issue        VARCHAR(500),
    dev_check          VARCHAR(20),
    risk               VARCHAR(1000),
    owner              VARCHAR(100),
    analyzed_date      DATE,
    status             VARCHAR(20) NOT NULL,
    created_at         DATETIME NOT NULL,
    updated_at         DATETIME NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_analysis_card_status ON analysis_card (status);
CREATE INDEX idx_analysis_card_domain ON analysis_card (domain);
