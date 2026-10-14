package com.grovesoft.boctracker.handover;

import com.grovesoft.boctracker.handover.dto.HandoverSessionRequest;
import com.grovesoft.boctracker.handover.dto.HandoverSessionResponse;
import org.springframework.stereotype.Component;

@Component
public class HandoverSessionMapper {

    public HandoverSession toEntity(HandoverSessionRequest r) {
        return HandoverSession.builder()
                .week(r.week())
                .category(r.category())
                .domain(r.domain())
                .topic(r.topic())
                .ktStage(blankToNull(r.ktStage()))
                .handoverFrom(r.handoverFrom())
                .handoverTo(r.handoverTo())
                .method(r.method())
                .output(r.output())
                .precondition(r.precondition())
                .plannedDate(r.plannedDate())
                .status(blankToNull(r.status()))
                .note(r.note())
                .build();
    }

    public HandoverSessionResponse toResponse(HandoverSession e) {
        return new HandoverSessionResponse(
                e.getId(), e.getWeek(), e.getCategory(), e.getDomain(), e.getTopic(), e.getKtStage(),
                e.getHandoverFrom(), e.getHandoverTo(), e.getMethod(), e.getOutput(), e.getPrecondition(),
                e.getPlannedDate(), e.getStatus(), e.getNote(), e.getCreatedAt(), e.getUpdatedAt());
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
