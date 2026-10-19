-- V12: 기술부채 관리대장 · 인수인계 질의(질문지) · 공용 첨부 파일
--   · 파일 본문은 디스크(app.storage.dir)에, 메타데이터만 DB 에 둔다(수급 산출물 V11 과 같은 방식, 소유자 유형으로 공용화).

-- 기술부채 관리대장
CREATE TABLE tech_debt (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    code            VARCHAR(50),
    sort_order      INT           NOT NULL DEFAULT 0,
    priority        VARCHAR(10)   NOT NULL,
    system_name     VARCHAR(20)   NOT NULL,
    area            VARCHAR(100),
    name            VARCHAR(255)  NOT NULL,
    summary         VARCHAR(2000),
    impact          VARCHAR(1000),
    basis           VARCHAR(500),
    remedy          VARCHAR(2000),
    confirm_with    VARCHAR(255),
    owner           VARCHAR(100),
    status          VARCHAR(20)   NOT NULL,
    verify_criteria VARCHAR(1000),
    target_date     DATE,
    completed_date  DATE,
    note            VARCHAR(1000),
    created_at      DATETIME      NOT NULL,
    updated_at      DATETIME      NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_tech_debt_code UNIQUE (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
CREATE INDEX idx_tech_debt_prio_order ON tech_debt (priority, sort_order);
CREATE INDEX idx_tech_debt_status ON tech_debt (status);

-- 인수인계 질의(질문지)
CREATE TABLE handover_question (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    code           VARCHAR(50),
    sort_order     INT           NOT NULL DEFAULT 0,
    priority       VARCHAR(10)   NOT NULL,
    kind           VARCHAR(100),
    related_node   VARCHAR(500),
    question       VARCHAR(1000) NOT NULL,
    basis          VARCHAR(1000),
    ask_to         VARCHAR(255),
    hypothesis     VARCHAR(1000),
    reflect_plan   VARCHAR(1000),
    status         VARCHAR(20)   NOT NULL,
    answer         VARCHAR(2000),
    confirmed_by   VARCHAR(100),
    confirmed_date DATE,
    note           VARCHAR(1000),
    created_at     DATETIME      NOT NULL,
    updated_at     DATETIME      NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_handover_question_code UNIQUE (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
CREATE INDEX idx_handover_question_prio_order ON handover_question (priority, sort_order);
CREATE INDEX idx_handover_question_status ON handover_question (status);

-- 공용 첨부 파일(소유자 유형 + 소유자 id). 소유자 삭제 시 서비스가 지운다.
CREATE TABLE attachment (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    owner_type    VARCHAR(30)  NOT NULL,
    owner_id      BIGINT       NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    stored_name   VARCHAR(100) NOT NULL,
    content_type  VARCHAR(150),
    size_bytes    BIGINT       NOT NULL,
    sha256        VARCHAR(64)  NOT NULL,
    created_at    DATETIME     NOT NULL,
    updated_at    DATETIME     NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_attachment_stored UNIQUE (stored_name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
CREATE INDEX idx_attachment_owner ON attachment (owner_type, owner_id);
