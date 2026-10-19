-- V6: 마일스톤 고도화 — 목표일/완료일, 체크리스트 항목
ALTER TABLE milestone ADD COLUMN target_date DATE;
ALTER TABLE milestone ADD COLUMN completed_date DATE;

-- 이미 완료 상태인 행은 마지막 수정일을 완료일로 간주
UPDATE milestone SET completed_date = CAST(updated_at AS DATE) WHERE status = '완료';

CREATE TABLE milestone_check_item (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    milestone_id BIGINT       NOT NULL,
    title        VARCHAR(500) NOT NULL,
    done         BOOLEAN      NOT NULL DEFAULT FALSE,
    sort_order   INT          NOT NULL,
    created_at   DATETIME     NOT NULL,
    updated_at   DATETIME     NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_check_item_milestone FOREIGN KEY (milestone_id) REFERENCES milestone (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_check_item_milestone ON milestone_check_item (milestone_id, sort_order);
CREATE INDEX idx_milestone_target_date ON milestone (target_date);
CREATE INDEX idx_milestone_owner ON milestone (owner);
