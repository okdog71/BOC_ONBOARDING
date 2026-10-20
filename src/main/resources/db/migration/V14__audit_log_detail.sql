-- V14: 변경 이력(감사 로그) 상세화 — 누가·언제·어디서·무엇을·어떻게(·누구에게)
--   기존 컬럼: entity_type, entity_id, action, actor, acted_at  (V13)
ALTER TABLE audit_log ADD COLUMN actor_name VARCHAR(50);
ALTER TABLE audit_log ADD COLUMN domain VARCHAR(40);
ALTER TABLE audit_log ADD COLUMN target_code VARCHAR(100);
ALTER TABLE audit_log ADD COLUMN target_label VARCHAR(255);
ALTER TABLE audit_log ADD COLUMN change_detail VARCHAR(4000);
ALTER TABLE audit_log ADD COLUMN client_ip VARCHAR(45);
ALTER TABLE audit_log ADD COLUMN request_desc VARCHAR(255);
UPDATE audit_log SET domain = entity_type WHERE domain IS NULL;
CREATE INDEX idx_audit_log_acted_at ON audit_log (acted_at);
CREATE INDEX idx_audit_log_actor ON audit_log (actor);
CREATE INDEX idx_audit_log_domain ON audit_log (domain);
