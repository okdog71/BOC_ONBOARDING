package com.grovesoft.boctracker.catchup;

import com.grovesoft.boctracker.catchup.dto.CatchupActionRequest;
import com.grovesoft.boctracker.catchup.dto.CatchupActionResponse;
import org.springframework.stereotype.Component;

@Component
public class CatchupActionMapper {

    public CatchupAction toEntity(CatchupActionRequest r) {
        return CatchupAction.builder()
                .category(r.category())
                .action(r.action())
                .purpose(r.purpose())
                .timing(blankToNull(r.timing()))
                .owner(r.owner())
                .kpi(r.kpi())
                .status(blankToNull(r.status()))
                .note(r.note())
                .build();
    }

    public CatchupActionResponse toResponse(CatchupAction e) {
        return new CatchupActionResponse(
                e.getId(),
                e.getCategory(),
                e.getAction(),
                e.getPurpose(),
                e.getTiming(),
                e.getOwner(),
                e.getKpi(),
                e.getStatus(),
                e.getNote(),
                e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy());
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
