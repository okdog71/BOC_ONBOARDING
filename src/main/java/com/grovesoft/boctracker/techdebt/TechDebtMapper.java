package com.grovesoft.boctracker.techdebt;

import com.grovesoft.boctracker.common.attachment.dto.AttachmentResponse;
import com.grovesoft.boctracker.techdebt.dto.TechDebtRequest;
import com.grovesoft.boctracker.techdebt.dto.TechDebtResponse;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TechDebtMapper {

    public TechDebt toEntity(TechDebtRequest r) {
        return TechDebt.builder()
                .code(blankToNull(r.code()))
                .priority(r.priority().trim())
                .system(r.system().trim())
                .area(blankToNull(r.area()))
                .name(r.name().trim())
                .summary(r.summary())
                .impact(r.impact())
                .basis(r.basis())
                .remedy(r.remedy())
                .confirmWith(blankToNull(r.confirmWith()))
                .owner(blankToNull(r.owner()))
                .status(blankToNull(r.status()))
                .verifyCriteria(r.verifyCriteria())
                .targetDate(r.targetDate())
                .note(r.note())
                .build();
    }

    public TechDebtResponse toResponse(TechDebt e, LocalDate today, List<AttachmentResponse> files) {
        return new TechDebtResponse(
                e.getId(), e.getCode(), e.getSortOrder(), e.getPriority(), e.getSystem(), e.getArea(), e.getName(),
                e.getSummary(), e.getImpact(), e.getBasis(), e.getRemedy(), e.getConfirmWith(), e.getOwner(),
                e.getStatus(), e.getVerifyCriteria(), e.getTargetDate(), e.getCompletedDate(), e.isDone(),
                e.isOverdue(today), e.warnings(), files, e.getNote(), e.getCreatedAt(), e.getUpdatedAt());
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
