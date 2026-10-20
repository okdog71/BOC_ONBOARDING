package com.grovesoft.boctracker.issue;

import com.grovesoft.boctracker.issue.dto.IssueQueueRequest;
import com.grovesoft.boctracker.issue.dto.IssueQueueResponse;
import org.springframework.stereotype.Component;

@Component
public class IssueQueueMapper {

    public IssueQueue toEntity(IssueQueueRequest r) {
        return IssueQueue.builder()
                .type(blankToNull(r.type()))
                .item(r.item())
                .content(r.content())
                .impact(r.impact())
                .basis(r.basis())
                .confirmWith(r.confirmWith())
                .owner(r.owner())
                .status(blankToNull(r.status()))
                .targetDate(r.targetDate())
                .action(r.action())
                .note(r.note())
                .build();
    }

    public IssueQueueResponse toResponse(IssueQueue e) {
        return new IssueQueueResponse(
                e.getId(),
                e.getType(),
                e.getItem(),
                e.getContent(),
                e.getImpact(),
                e.getBasis(),
                e.getConfirmWith(),
                e.getOwner(),
                e.getStatus(),
                e.getTargetDate(),
                e.getAction(),
                e.getNote(),
                e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy());
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
