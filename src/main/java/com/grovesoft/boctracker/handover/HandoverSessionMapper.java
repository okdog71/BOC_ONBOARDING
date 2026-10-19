package com.grovesoft.boctracker.handover;

import com.grovesoft.boctracker.handover.dto.HandoverSessionRequest;
import com.grovesoft.boctracker.handover.dto.HandoverSessionResponse;
import com.grovesoft.boctracker.handover.dto.HandoverSessionResponse.AccessRef;
import com.grovesoft.boctracker.handover.dto.HandoverSessionResponse.SupplyRef;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class HandoverSessionMapper {

    public HandoverSession toEntity(HandoverSessionRequest r) {
        return HandoverSession.builder()
                .code(blankToNull(r.code()))
                .week(r.week())
                .category(r.category())
                .domain(r.domain())
                .topic(r.topic().trim())
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

    /** @param allSessions 마무리 세션("전 세션 완료")의 미완료 판단용 전체 세션 */
    public HandoverSessionResponse toResponse(HandoverSession e, LocalDate today, List<HandoverSession> allSessions) {
        List<SupplyRef> supply = e.getSupplyDocs().stream()
                .sorted(Comparator.comparing(d -> d.getCode()))
                .map(d -> new SupplyRef(d.getCode(), d.getName(), d.getSupplyStatus(), d.isReady())).toList();
        List<AccessRef> access = e.getAccessItems().stream()
                .sorted(Comparator.comparing(a -> a.getCode()))
                .map(a -> new AccessRef(a.getCode(), a.getTarget(), a.getStatus(), a.isReady())).toList();
        return new HandoverSessionResponse(
                e.getId(), e.getCode(), e.getSortOrder(), e.getWeek(), e.getCategory(), e.getDomain(), e.getTopic(),
                e.getKtStage(), e.getHandoverFrom(), e.getHandoverTo(), e.getMethod(), e.getOutput(),
                e.getPrecondition(), e.getPlannedDate(), e.getCompletedDate(), e.getStatus(), e.isOverdue(today),
                e.getMilestone() == null ? null : e.getMilestone().getCode(),
                e.getMilestone() == null ? null : e.getMilestone().getName(),
                supply, access, e.blockers(allSessions), e.warnings(), e.getNote(),
                e.getCreatedAt(), e.getUpdatedAt());
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
