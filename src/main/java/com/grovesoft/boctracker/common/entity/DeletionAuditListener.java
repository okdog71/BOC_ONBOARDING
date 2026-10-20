package com.grovesoft.boctracker.common.entity;

import com.grovesoft.boctracker.auth.CurrentUser;
import jakarta.persistence.PostRemove;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** 삭제는 행이 사라지므로, 누가 언제 지웠는지를 audit_log 에 남긴다(같은 트랜잭션). */
@Component
public class DeletionAuditListener {

    private final JdbcTemplate jdbc;

    public DeletionAuditListener(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostRemove
    public void onRemove(Object entity) {
        if (entity instanceof BaseEntity e && e.getId() != null) {
            jdbc.update("INSERT INTO audit_log (entity_type, entity_id, action, actor, acted_at) VALUES (?, ?, 'DELETE', ?, ?)",
                    entity.getClass().getSimpleName(), e.getId(), CurrentUser.actor(),
                    Timestamp.valueOf(LocalDateTime.now(ZoneId.of("Asia/Seoul"))));
        }
    }
}
