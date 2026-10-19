-- V4: 마일스톤 선행관계 (Milestone.precondition 문자열의 정규화 FK 병행). 기존 precondition 컬럼은 유지.
CREATE TABLE milestone_dependency (
    milestone_id   BIGINT NOT NULL,
    predecessor_id BIGINT NOT NULL,
    PRIMARY KEY (milestone_id, predecessor_id),
    CONSTRAINT fk_milestone_dep_milestone FOREIGN KEY (milestone_id) REFERENCES milestone (id) ON DELETE CASCADE,
    CONSTRAINT fk_milestone_dep_predecessor FOREIGN KEY (predecessor_id) REFERENCES milestone (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_milestone_dep_predecessor ON milestone_dependency (predecessor_id);
