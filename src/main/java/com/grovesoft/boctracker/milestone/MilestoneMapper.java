package com.grovesoft.boctracker.milestone;

import com.grovesoft.boctracker.milestone.dto.MilestoneRequest;
import com.grovesoft.boctracker.milestone.dto.MilestoneResponse;
import com.grovesoft.boctracker.milestone.dto.MilestoneResponse.CheckItemResponse;
import com.grovesoft.boctracker.milestone.dto.MilestoneResponse.Progress;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MilestoneMapper {

    public Milestone toEntity(MilestoneRequest r) {
        return Milestone.builder()
                .phase(r.phase().trim())
                .code(blankToNull(r.code()))
                .name(r.name().trim())
                .goal(r.goal())
                .checkpoint(r.checkpoint())
                .output(r.output())
                .precondition(r.precondition())
                .owner(blankToNull(r.owner()))
                .durationWeeks(r.durationWeeks())
                .status(blankToNull(r.status()))
                .doneCriteria(r.doneCriteria())
                .note(r.note())
                .targetDate(r.targetDate())
                .build();
    }

    /** @param today 지연(overdue) 판단 기준일 */
    public MilestoneResponse toResponse(Milestone e, LocalDate today) {
        List<CheckItemResponse> items = e.getCheckItems().stream()
                .map(i -> new CheckItemResponse(i.getId(), i.getTitle(), i.isDone())).toList();
        int done = (int) items.stream().filter(CheckItemResponse::done).count();
        Progress progress = new Progress(done, items.size(),
                items.isEmpty() ? null : Math.round(done * 100f / items.size()));
        return new MilestoneResponse(
                e.getId(), e.getPhase(), e.getCode(), e.getSortOrder(), e.getName(), e.getGoal(), e.getCheckpoint(),
                e.getOutput(), e.getPrecondition(), e.getOwner(), e.getDurationWeeks(), e.getStatus(),
                e.getDoneCriteria(), e.getNote(), e.getTargetDate(), e.getCompletedDate(), e.isOverdue(today),
                e.getPredecessors().stream().map(Milestone::getCode).sorted().toList(),
                e.incompletePredecessors().stream().map(Milestone::getCode).sorted().toList(),
                progress, items, e.getCreatedAt(), e.getUpdatedAt());
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
