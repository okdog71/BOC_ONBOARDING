package com.grovesoft.boctracker.auditlog;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grovesoft.boctracker.auditlog.dto.AuditLogResponse;
import com.grovesoft.boctracker.common.web.BusinessRuleException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private static final List<String> ACTIONS = List.of("CREATE", "UPDATE", "DELETE");

    private final AuditLogRepository repository;
    private final ObjectMapper mapper;

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> search(String domain, String actor, String action, String code, String ip,
            LocalDate from, LocalDate to, String q, Pageable pageable) {
        if (action != null && !action.isBlank() && !ACTIONS.contains(action)) {
            throw new BusinessRuleException("action 은 CREATE, UPDATE, DELETE 중 하나여야 합니다.");
        }
        if (from != null && to != null && to.isBefore(from)) {
            throw new BusinessRuleException("종료일은 시작일보다 빠를 수 없습니다.");
        }
        LocalDateTime start = from == null ? LocalDate.of(2000, 1, 1).atStartOfDay() : from.atStartOfDay();
        LocalDateTime end = to == null ? LocalDate.of(2999, 1, 1).atStartOfDay() : to.plusDays(1).atStartOfDay();
        String like = q == null || q.isBlank() ? null : "%" + q.trim().toLowerCase() + "%";
        return repository.search(blank(domain), blank(actor), blank(action), blank(code), blank(ip), start, end, like,
                pageable).map(this::toResponse);
    }

    private AuditLogResponse toResponse(AuditLogEntry e) {
        return new AuditLogResponse(e.getId(), e.getActedAt(), e.getActor(), e.getActorName(), e.getAction(),
                e.getDomain(), e.getEntityId(), e.getTargetCode(), e.getTargetLabel(), changes(e.getChangeDetail()),
                e.getClientIp(), e.getRequestDesc());
    }

    private List<AuditLogResponse.Change> changes(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            List<Map<String, String>> raw = mapper.readValue(json, new TypeReference<>() { });
            return raw.stream().map(m -> new AuditLogResponse.Change(m.get("field"), m.get("from"), m.get("to"))).toList();
        } catch (Exception ex) {
            return List.of();
        }
    }

    private static String blank(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
