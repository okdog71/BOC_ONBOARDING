package com.grovesoft.boctracker.analysiscard;

import com.grovesoft.boctracker.analysiscard.dto.AnalysisCardRequest;
import com.grovesoft.boctracker.analysiscard.dto.AnalysisCardResponse;
import org.springframework.stereotype.Component;

@Component
public class AnalysisCardMapper {

    public AnalysisCard toEntity(AnalysisCardRequest r) {
        return AnalysisCard.builder()
                .menuName(r.menuName())
                .path(r.path())
                .domain(r.domain())
                .backendService(r.backendService())
                .generation(blankToNull(r.generation()))
                .summary(r.summary())
                .scenario(r.scenario())
                .input(r.input())
                .output(r.output())
                .backendMapping(r.backendMapping())
                .dataStore(r.dataStore())
                .externalLink(r.externalLink())
                .permission(r.permission())
                .relatedProcess(r.relatedProcess())
                .knownIssue(r.knownIssue())
                .devCheck(blankToNull(r.devCheck()))
                .risk(r.risk())
                .owner(r.owner())
                .analyzedDate(r.analyzedDate())
                .status(blankToNull(r.status()))
                .build();
    }

    public AnalysisCardResponse toResponse(AnalysisCard e) {
        return new AnalysisCardResponse(
                e.getId(),
                e.getMenuName(),
                e.getPath(),
                e.getDomain(),
                e.getBackendService(),
                e.getGeneration(),
                e.getSummary(),
                e.getScenario(),
                e.getInput(),
                e.getOutput(),
                e.getBackendMapping(),
                e.getDataStore(),
                e.getExternalLink(),
                e.getPermission(),
                e.getRelatedProcess(),
                e.getKnownIssue(),
                e.getDevCheck(),
                e.getRisk(),
                e.getOwner(),
                e.getAnalyzedDate(),
                e.getStatus(),
                e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy());
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
