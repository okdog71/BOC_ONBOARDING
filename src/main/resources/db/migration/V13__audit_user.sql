-- V13: 공통 감사(Audit) 컬럼(등록자·수정자) + 사용자(app_user) + 삭제 이력(audit_log)
--   · 등록자/수정자는 로그인 ID 를 저장한다. 기존 행은 'system' 으로 채운다.

ALTER TABLE access_item ADD COLUMN created_by VARCHAR(50);
ALTER TABLE access_item ADD COLUMN updated_by VARCHAR(50);
UPDATE access_item SET created_by = 'system', updated_by = 'system';
ALTER TABLE analysis_card ADD COLUMN created_by VARCHAR(50);
ALTER TABLE analysis_card ADD COLUMN updated_by VARCHAR(50);
UPDATE analysis_card SET created_by = 'system', updated_by = 'system';
ALTER TABLE catchup_action ADD COLUMN created_by VARCHAR(50);
ALTER TABLE catchup_action ADD COLUMN updated_by VARCHAR(50);
UPDATE catchup_action SET created_by = 'system', updated_by = 'system';
ALTER TABLE attachment ADD COLUMN created_by VARCHAR(50);
ALTER TABLE attachment ADD COLUMN updated_by VARCHAR(50);
UPDATE attachment SET created_by = 'system', updated_by = 'system';
ALTER TABLE doc_reliability ADD COLUMN created_by VARCHAR(50);
ALTER TABLE doc_reliability ADD COLUMN updated_by VARCHAR(50);
UPDATE doc_reliability SET created_by = 'system', updated_by = 'system';
ALTER TABLE handover_session ADD COLUMN created_by VARCHAR(50);
ALTER TABLE handover_session ADD COLUMN updated_by VARCHAR(50);
UPDATE handover_session SET created_by = 'system', updated_by = 'system';
ALTER TABLE handover_question ADD COLUMN created_by VARCHAR(50);
ALTER TABLE handover_question ADD COLUMN updated_by VARCHAR(50);
UPDATE handover_question SET created_by = 'system', updated_by = 'system';
ALTER TABLE interface_link ADD COLUMN created_by VARCHAR(50);
ALTER TABLE interface_link ADD COLUMN updated_by VARCHAR(50);
UPDATE interface_link SET created_by = 'system', updated_by = 'system';
ALTER TABLE issue_queue ADD COLUMN created_by VARCHAR(50);
ALTER TABLE issue_queue ADD COLUMN updated_by VARCHAR(50);
UPDATE issue_queue SET created_by = 'system', updated_by = 'system';
ALTER TABLE mega_process ADD COLUMN created_by VARCHAR(50);
ALTER TABLE mega_process ADD COLUMN updated_by VARCHAR(50);
UPDATE mega_process SET created_by = 'system', updated_by = 'system';
ALTER TABLE menu_inventory ADD COLUMN created_by VARCHAR(50);
ALTER TABLE menu_inventory ADD COLUMN updated_by VARCHAR(50);
UPDATE menu_inventory SET created_by = 'system', updated_by = 'system';
ALTER TABLE milestone ADD COLUMN created_by VARCHAR(50);
ALTER TABLE milestone ADD COLUMN updated_by VARCHAR(50);
UPDATE milestone SET created_by = 'system', updated_by = 'system';
ALTER TABLE milestone_check_item ADD COLUMN created_by VARCHAR(50);
ALTER TABLE milestone_check_item ADD COLUMN updated_by VARCHAR(50);
UPDATE milestone_check_item SET created_by = 'system', updated_by = 'system';
ALTER TABLE raci_assignment ADD COLUMN created_by VARCHAR(50);
ALTER TABLE raci_assignment ADD COLUMN updated_by VARCHAR(50);
UPDATE raci_assignment SET created_by = 'system', updated_by = 'system';
ALTER TABLE review_log ADD COLUMN created_by VARCHAR(50);
ALTER TABLE review_log ADD COLUMN updated_by VARCHAR(50);
UPDATE review_log SET created_by = 'system', updated_by = 'system';
ALTER TABLE supply_doc ADD COLUMN created_by VARCHAR(50);
ALTER TABLE supply_doc ADD COLUMN updated_by VARCHAR(50);
UPDATE supply_doc SET created_by = 'system', updated_by = 'system';
ALTER TABLE supply_doc_file ADD COLUMN created_by VARCHAR(50);
ALTER TABLE supply_doc_file ADD COLUMN updated_by VARCHAR(50);
UPDATE supply_doc_file SET created_by = 'system', updated_by = 'system';
ALTER TABLE tech_debt ADD COLUMN created_by VARCHAR(50);
ALTER TABLE tech_debt ADD COLUMN updated_by VARCHAR(50);
UPDATE tech_debt SET created_by = 'system', updated_by = 'system';

-- 사용자(내부 5명). 비밀번호는 BCrypt 해시만 저장한다.
CREATE TABLE app_user (
    id                   BIGINT       NOT NULL AUTO_INCREMENT,
    login_id             VARCHAR(30)  NOT NULL,
    password_hash        VARCHAR(100) NOT NULL,
    must_change_password BOOLEAN      NOT NULL DEFAULT TRUE,
    affiliation          VARCHAR(50)  NOT NULL,
    name                 VARCHAR(50)  NOT NULL,
    join_date            DATE,
    leave_date           DATE,
    krafton_account      VARCHAR(100),
    phone                VARCHAR(30),
    grove_mail           VARCHAR(100),
    ip                   VARCHAR(100),
    gmail                VARCHAR(100),
    vpn                  VARCHAR(100),
    boc_account          VARCHAR(100),
    kec_account          VARCHAR(100),
    aws                  VARCHAR(100),
    created_at           DATETIME     NOT NULL,
    updated_at           DATETIME     NOT NULL,
    created_by           VARCHAR(50),
    updated_by           VARCHAR(50),
    PRIMARY KEY (id),
    CONSTRAINT uk_app_user_login UNIQUE (login_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 삭제 이력(행이 사라지므로 누가 언제 지웠는지만 남긴다)
CREATE TABLE audit_log (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    entity_type VARCHAR(60) NOT NULL,
    entity_id   BIGINT      NOT NULL,
    action      VARCHAR(10) NOT NULL,
    actor       VARCHAR(50) NOT NULL,
    acted_at    DATETIME    NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
CREATE INDEX idx_audit_log_entity ON audit_log (entity_type, entity_id);
