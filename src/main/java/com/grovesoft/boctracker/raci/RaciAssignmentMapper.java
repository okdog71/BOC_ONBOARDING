package com.grovesoft.boctracker.raci;

import com.grovesoft.boctracker.raci.dto.RaciAssignmentRequest;
import com.grovesoft.boctracker.raci.dto.RaciAssignmentResponse;
import org.springframework.stereotype.Component;

@Component
public class RaciAssignmentMapper {

    public RaciAssignment toEntity(RaciAssignmentRequest r) {
        return RaciAssignment.builder()
                .domainGroup(r.domainGroup())
                .scope(r.scope())
                .complexity(blankToNull(r.complexity()))
                .scale(blankToNull(r.scale()))
                .generation(blankToNull(r.generation()))
                .responsible(r.responsible())
                .backup(r.backup())
                .reviewer(r.reviewer())
                .menuCount(r.menuCount())
                .status(blankToNull(r.status()))
                .targetDate(r.targetDate())
                .note(r.note())
                .build();
    }

    public RaciAssignmentResponse toResponse(RaciAssignment e) {
        return new RaciAssignmentResponse(
                e.getId(),
                e.getDomainGroup(),
                e.getScope(),
                e.getComplexity(),
                e.getScale(),
                e.getGeneration(),
                e.getResponsible(),
                e.getBackup(),
                e.getReviewer(),
                e.getMenuCount(),
                e.getStatus(),
                e.getTargetDate(),
                e.getNote(),
                e.getCreatedAt(), e.getUpdatedAt());
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
