package com.grovesoft.boctracker.handoverquestion;

import com.grovesoft.boctracker.common.attachment.dto.AttachmentResponse;
import com.grovesoft.boctracker.handoverquestion.dto.HandoverQuestionRequest;
import com.grovesoft.boctracker.handoverquestion.dto.HandoverQuestionResponse;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class HandoverQuestionMapper {

    public HandoverQuestion toEntity(HandoverQuestionRequest r) {
        return HandoverQuestion.builder()
                .code(blankToNull(r.code()))
                .priority(r.priority().trim())
                .kind(blankToNull(r.kind()))
                .relatedNode(blankToNull(r.relatedNode()))
                .question(r.question().trim())
                .basis(r.basis())
                .askTo(blankToNull(r.askTo()))
                .hypothesis(r.hypothesis())
                .reflectPlan(r.reflectPlan())
                .status(blankToNull(r.status()))
                .answer(r.answer())
                .confirmedBy(blankToNull(r.confirmedBy()))
                .confirmedDate(r.confirmedDate())
                .note(r.note())
                .build();
    }

    public HandoverQuestionResponse toResponse(HandoverQuestion e, List<AttachmentResponse> files) {
        return new HandoverQuestionResponse(
                e.getId(), e.getCode(), e.getSortOrder(), e.getPriority(), e.getKind(), e.getRelatedNode(),
                e.getQuestion(), e.getBasis(), e.getAskTo(), e.getHypothesis(), e.getReflectPlan(), e.getStatus(),
                e.getAnswer(), e.getConfirmedBy(), e.getConfirmedDate(), e.isConfirmed(), false, e.warnings(), files,
                e.getNote(), e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy());
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
