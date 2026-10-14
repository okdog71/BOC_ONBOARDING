package com.grovesoft.boctracker.milestone;

import com.grovesoft.boctracker.milestone.dto.MilestoneRequest;
import com.grovesoft.boctracker.milestone.dto.MilestoneResponse;
import org.springframework.stereotype.Component;

@Component
public class MilestoneMapper {

    public Milestone toEntity(MilestoneRequest r) {
        return Milestone.builder()
                .phase(r.phase())
                .code(r.code())
                .name(r.name())
                .goal(r.goal())
                .checkpoint(r.checkpoint())
                .output(r.output())
                .precondition(r.precondition())
                .owner(r.owner())
                .durationWeeks(r.durationWeeks())
                .status(blankToNull(r.status()))
                .doneCriteria(r.doneCriteria())
                .note(r.note())
                .build();
    }

    public MilestoneResponse toResponse(Milestone e) {
        return new MilestoneResponse(
                e.getId(), e.getPhase(), e.getCode(), e.getName(), e.getGoal(), e.getCheckpoint(),
                e.getOutput(), e.getPrecondition(), e.getOwner(), e.getDurationWeeks(), e.getStatus(),
                e.getDoneCriteria(), e.getNote(), e.getCreatedAt(), e.getUpdatedAt());
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
