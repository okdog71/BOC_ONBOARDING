-- V1: milestone, handover_session (2단계 범위). 나머지 테이블은 V2 이후 증분.

CREATE TABLE milestone (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    phase          VARCHAR(100)  NOT NULL,
    code           VARCHAR(50)   NOT NULL,
    name           VARCHAR(255)  NOT NULL,
    goal           VARCHAR(1000),
    checkpoint     VARCHAR(1000),
    output         VARCHAR(1000),
    precondition VARCHAR(1000),
    owner          VARCHAR(100),
    duration_weeks VARCHAR(50),
    status         VARCHAR(20)   NOT NULL,
    done_criteria  VARCHAR(1000),
    note           VARCHAR(1000),
    created_at     DATETIME      NOT NULL,
    updated_at     DATETIME      NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_milestone_code UNIQUE (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_milestone_status ON milestone (status);
CREATE INDEX idx_milestone_phase ON milestone (phase);

CREATE TABLE handover_session (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    week           VARCHAR(10)   NOT NULL,
    category       VARCHAR(20)   NOT NULL,
    domain         VARCHAR(255),
    topic          VARCHAR(1000) NOT NULL,
    kt_stage       VARCHAR(50),
    handover_from  VARCHAR(255),
    handover_to    VARCHAR(255),
    method         VARCHAR(255),
    output         VARCHAR(1000),
    precondition VARCHAR(1000),
    planned_date   DATE,
    status         VARCHAR(20)   NOT NULL,
    note           VARCHAR(1000),
    created_at     DATETIME      NOT NULL,
    updated_at     DATETIME      NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_handover_status ON handover_session (status);
CREATE INDEX idx_handover_week ON handover_session (week);
CREATE INDEX idx_handover_category ON handover_session (category);
