package com.grovesoft.boctracker.auditlog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grovesoft.boctracker.auth.CurrentUser;
import com.grovesoft.boctracker.auth.SessionUser;
import com.grovesoft.boctracker.common.entity.BaseEntity;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import jakarta.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 모든 도메인 데이터의 등록·수정·삭제를 변경 이력(audit_log)에 남긴다.
 * 누가(로그인 ID·이름) · 언제 · 어디서(IP·요청) · 무엇을(도메인·대상 코드/이름) · 어떻게(작업·변경 필드 전/후 값).
 * HTTP 요청 밖(기동 시 시드 적재 등)의 변경은 사람이 한 일이 아니므로 남기지 않는다.
 * 같은 트랜잭션에서 기록하므로 변경이 롤백되면 이력도 함께 사라진다.
 */
@Slf4j
@Component
public class AuditEventListener implements PostInsertEventListener, PostUpdateEventListener, PostDeleteEventListener {

    private static final Set<String> SKIP_FIELDS = Set.of("createdAt", "createdBy", "updatedAt", "updatedBy");
    private static final Set<String> SECRET_FIELDS = Set.of("passwordHash");
    private static final List<String> LABEL_FIELDS = List.of("name", "topic", "target", "question", "docName",
            "menuName", "systemName", "title", "originalName", "fileName", "item", "action");
    private static final int MAX_VALUE = 120;
    private static final int MAX_JSON = 4000;

    private final EntityManagerFactory emf;
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;

    public AuditEventListener(EntityManagerFactory emf, JdbcTemplate jdbc, ObjectMapper mapper) {
        this.emf = emf;
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    @PostConstruct
    void register() {
        EventListenerRegistry registry = emf.unwrap(SessionFactoryImplementor.class).getServiceRegistry()
                .getService(EventListenerRegistry.class);
        registry.appendListeners(EventType.POST_INSERT, this);
        registry.appendListeners(EventType.POST_UPDATE, this);
        registry.appendListeners(EventType.POST_DELETE, this);
    }

    @Override
    public void onPostInsert(PostInsertEvent e) {
        record("CREATE", e.getEntity(), e.getId(), e.getPersister(), null, e.getState());
    }

    @Override
    public void onPostUpdate(PostUpdateEvent e) {
        record("UPDATE", e.getEntity(), e.getId(), e.getPersister(), e.getOldState(), e.getState());
    }

    @Override
    public void onPostDelete(PostDeleteEvent e) {
        record("DELETE", e.getEntity(), e.getId(), e.getPersister(), e.getDeletedState(), null);
    }

    @Override
    public boolean requiresPostCommitHandling(EntityPersister persister) {
        return false;
    }

    private void record(String action, Object entity, Object id, EntityPersister persister, Object[] before, Object[] after) {
        try {
            if (!(entity instanceof BaseEntity) || !(id instanceof Long entityId)) {
                return;
            }
            RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return; // HTTP 요청이 아닌 작업(시드 적재 등)
            }
            String[] names = persister.getPropertyNames();
            Object[] current = after != null ? after : before;
            List<Map<String, String>> changes = new ArrayList<>();
            if ("UPDATE".equals(action)) {
                for (int i = 0; i < names.length; i++) {
                    if (SKIP_FIELDS.contains(names[i]) || !simple(after[i]) && !simple(before == null ? null : before[i])) {
                        continue;
                    }
                    Object b = before == null ? null : before[i];
                    if (!Objects.equals(b, after[i])) {
                        changes.add(change(names[i], b, after[i], true));
                    }
                }
                if (changes.isEmpty()) {
                    return; // 감사 컬럼만 바뀐 경우
                }
            } else {
                for (int i = 0; i < names.length; i++) {
                    if (SKIP_FIELDS.contains(names[i]) || current[i] == null || !simple(current[i])) {
                        continue;
                    }
                    changes.add("CREATE".equals(action) ? change(names[i], null, current[i], false)
                            : change(names[i], current[i], null, false));
                }
            }
            String type = entity.getClass().getSimpleName();
            SessionUser su = CurrentUser.get().orElse(null);
            HttpServletRequest req = attrs instanceof ServletRequestAttributes sra ? sra.getRequest() : null;
            jdbc.update("""
                    INSERT INTO audit_log (entity_type, entity_id, action, actor, actor_name, acted_at, domain,
                        target_code, target_label, change_detail, client_ip, request_desc)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
                    type, entityId, action, CurrentUser.actor(), su == null ? "시스템" : su.name(),
                    Timestamp.valueOf(LocalDateTime.now(ZoneId.of("Asia/Seoul"))), AuditDomains.label(type),
                    cut(valueOf(names, current, "code", "loginId"), 100), cut(label(names, current), 255),
                    toJson(changes), req == null ? null : cut(clientIp(req), 45),
                    req == null ? null : cut(req.getMethod() + " " + req.getRequestURI(), 255));
        } catch (Exception ex) {
            // 이력 기록 실패가 업무 처리를 막지 않게 한다
            log.warn("변경 이력 기록 실패: {}", ex.toString());
        }
    }

    private Map<String, String> change(String field, Object from, Object to, boolean both) {
        if (SECRET_FIELDS.contains(field)) {
            return Map.of("field", "password", "from", "", "to", "(변경됨)");
        }
        java.util.LinkedHashMap<String, String> m = new java.util.LinkedHashMap<>();
        m.put("field", field);
        if (both || from != null) {
            m.put("from", cut(str(from), MAX_VALUE));
        }
        if (both || to != null) {
            m.put("to", cut(str(to), MAX_VALUE));
        }
        return m;
    }

    /** 문자열·숫자·불리언·날짜만 이력에 담는다(연관 엔티티·컬렉션 제외). */
    private static boolean simple(Object v) {
        return v == null || v instanceof CharSequence || v instanceof Number || v instanceof Boolean
                || v instanceof java.time.temporal.Temporal || v instanceof Enum<?>;
    }

    private static String str(Object v) {
        return v == null ? "" : v.toString();
    }

    private static String cut(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    private static String valueOf(String[] names, Object[] state, String... keys) {
        for (String k : keys) {
            for (int i = 0; i < names.length; i++) {
                if (names[i].equals(k) && state[i] != null) {
                    return state[i].toString();
                }
            }
        }
        return null;
    }

    private static String label(String[] names, Object[] state) {
        return valueOf(names, state, LABEL_FIELDS.toArray(new String[0]));
    }

    private static String clientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        String ip = xff != null && !xff.isBlank() ? xff.split(",")[0].trim() : req.getRemoteAddr();
        return "0:0:0:0:0:0:0:1".equals(ip) ? "::1" : ip;
    }

    private String toJson(List<Map<String, String>> changes) throws JsonProcessingException {
        List<Map<String, String>> kept = new ArrayList<>(changes);
        String json = mapper.writeValueAsString(kept);
        while (json.length() > MAX_JSON && kept.size() > 1) {
            kept.remove(kept.size() - 1);
            json = mapper.writeValueAsString(kept);
        }
        return json.length() > MAX_JSON ? "[]" : json;
    }

    @SuppressWarnings("unused")
    private static boolean isCollection(Object v) {
        return v instanceof Collection<?>;
    }
}
