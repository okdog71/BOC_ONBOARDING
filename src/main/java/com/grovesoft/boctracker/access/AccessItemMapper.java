package com.grovesoft.boctracker.access;

import com.grovesoft.boctracker.access.dto.AccessItemRequest;
import com.grovesoft.boctracker.access.dto.AccessItemResponse;
import org.springframework.stereotype.Component;

@Component
public class AccessItemMapper {

    public AccessItem toEntity(AccessItemRequest r) {
        return AccessItem.builder()
                .category(r.category())
                .target(r.target())
                .usage(r.usage())
                .envScope(r.envScope())
                .procedure(r.procedure())
                .status(blankToNull(r.status()))
                .requestedDate(r.requestedDate())
                .approvedDate(r.approvedDate())
                .owner(r.owner())
                .note(r.note())
                .build();
    }

    public AccessItemResponse toResponse(AccessItem e) {
        return new AccessItemResponse(
                e.getId(),
                e.getCategory(),
                e.getTarget(),
                e.getUsage(),
                e.getEnvScope(),
                e.getProcedure(),
                e.getStatus(),
                e.getRequestedDate(),
                e.getApprovedDate(),
                e.getOwner(),
                e.getNote(),
                e.getCreatedAt(), e.getUpdatedAt());
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
