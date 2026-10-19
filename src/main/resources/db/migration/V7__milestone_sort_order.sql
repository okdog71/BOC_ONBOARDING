-- V7: 단계 내 표시 순서(No.). 코드와 분리된 값 — 순서를 바꿔도 코드는 그대로.
ALTER TABLE milestone ADD COLUMN sort_order INT NOT NULL DEFAULT 0;
CREATE INDEX idx_milestone_phase_order ON milestone (phase, sort_order);
