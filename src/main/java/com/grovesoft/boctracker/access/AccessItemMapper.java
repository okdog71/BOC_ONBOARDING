package com.grovesoft.boctracker.access;

import com.grovesoft.boctracker.access.dto.AccessItemRequest;
import com.grovesoft.boctracker.access.dto.AccessItemResponse;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AccessItemMapper {

    public AccessItem toEntity(AccessItemRequest r) {
        return AccessItem.builder()
                .code(blankToNull(r.code()))
                .category(r.category().trim())
                .target(r.target().trim())
                .usage(r.usage())
                .envScope(r.envScope())
                .procedure(r.procedure())
                .status(blankToNull(r.status()))
                .requestedDate(r.requestedDate())
                .approvedDate(r.approvedDate())
                .targetDate(r.targetDate())
                .owner(blankToNull(r.owner()))
                .note(r.note())
                .build();
    }

    public AccessItemResponse toResponse(AccessItem e, LocalDate today, boolean prerequisitesMissing,
                                         List<String> requiredBy) {
        return new AccessItemResponse(
                e.getId(), e.getCode(), e.getSortOrder(), e.getCategory(), e.getTarget(), e.getUsage(),
                e.getEnvScope(), e.getProcedure(), e.getStatus(), e.getRequestedDate(), e.getApprovedDate(),
                e.getTargetDate(), e.leadTimeDays(), e.isReady(), e.isOverdue(today), e.isPrerequisite(),
                e.warnings(prerequisitesMissing), requiredBy, e.getOwner(), e.getNote(),
                e.getCreatedAt(), e.getUpdatedAt());
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
