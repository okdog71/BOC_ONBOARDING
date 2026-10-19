-- V9: 수급 산출물(②)·접근권한(③)·인수인계(⑤) 고도화
--   · 불변 코드(code) + 그룹 내 순서(sort_order)  · 일정/자동 기록 일자  · 인수인계 ↔ 마일스톤/수급/접근 FK

-- ② 수급 산출물
ALTER TABLE supply_doc ADD COLUMN code VARCHAR(50);
ALTER TABLE supply_doc ADD COLUMN sort_order INT NOT NULL DEFAULT 0;
ALTER TABLE supply_doc ADD COLUMN target_date DATE;
ALTER TABLE supply_doc ADD COLUMN requested_date DATE;
ALTER TABLE supply_doc ADD COLUMN received_date DATE;
CREATE UNIQUE INDEX uk_supply_doc_code ON supply_doc (code);
CREATE INDEX idx_supply_doc_cat_order ON supply_doc (category, sort_order);

-- ③ 접근권한
ALTER TABLE access_item ADD COLUMN code VARCHAR(50);
ALTER TABLE access_item ADD COLUMN sort_order INT NOT NULL DEFAULT 0;
ALTER TABLE access_item ADD COLUMN target_date DATE;
CREATE UNIQUE INDEX uk_access_item_code ON access_item (code);
CREATE INDEX idx_access_item_cat_order ON access_item (category, sort_order);

-- ⑤ 인수인계(KT)
ALTER TABLE handover_session ADD COLUMN code VARCHAR(50);
ALTER TABLE handover_session ADD COLUMN sort_order INT NOT NULL DEFAULT 0;
ALTER TABLE handover_session ADD COLUMN completed_date DATE;
ALTER TABLE handover_session ADD COLUMN milestone_id BIGINT;
ALTER TABLE handover_session ADD CONSTRAINT fk_handover_milestone FOREIGN KEY (milestone_id) REFERENCES milestone (id);
CREATE UNIQUE INDEX uk_handover_code ON handover_session (code);
CREATE INDEX idx_handover_week_order ON handover_session (week, sort_order);

-- 인수인계 세션이 요구하는 수급 산출물 / 접근권한 (참조 중인 항목 삭제는 서비스에서 409 로 막는다)
CREATE TABLE handover_supply_doc (
    handover_id   BIGINT NOT NULL,
    supply_doc_id BIGINT NOT NULL,
    PRIMARY KEY (handover_id, supply_doc_id),
    CONSTRAINT fk_hsd_handover FOREIGN KEY (handover_id) REFERENCES handover_session (id) ON DELETE CASCADE,
    CONSTRAINT fk_hsd_supply FOREIGN KEY (supply_doc_id) REFERENCES supply_doc (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
CREATE INDEX idx_hsd_supply ON handover_supply_doc (supply_doc_id);

CREATE TABLE handover_access_item (
    handover_id    BIGINT NOT NULL,
    access_item_id BIGINT NOT NULL,
    PRIMARY KEY (handover_id, access_item_id),
    CONSTRAINT fk_hai_handover FOREIGN KEY (handover_id) REFERENCES handover_session (id) ON DELETE CASCADE,
    CONSTRAINT fk_hai_access FOREIGN KEY (access_item_id) REFERENCES access_item (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
CREATE INDEX idx_hai_access ON handover_access_item (access_item_id);
