package com.grovesoft.boctracker.docreliability;

import com.grovesoft.boctracker.docreliability.dto.DocReliabilityRequest;
import com.grovesoft.boctracker.docreliability.dto.DocReliabilityResponse;
import org.springframework.stereotype.Component;

@Component
public class DocReliabilityMapper {

    public DocReliability toEntity(DocReliabilityRequest r) {
        return DocReliability.builder()
                .docName(r.docName())
                .source(r.source())
                .lastUpdated(r.lastUpdated())
                .freshness(blankToNull(r.freshness()))
                .codeFit(blankToNull(r.codeFit()))
                .grade(blankToNull(r.grade()))
                .verifyMethod(r.verifyMethod())
                .gap(r.gap())
                .owner(r.owner())
                .note(r.note())
                .build();
    }

    public DocReliabilityResponse toResponse(DocReliability e) {
        return new DocReliabilityResponse(
                e.getId(),
                e.getDocName(),
                e.getSource(),
                e.getLastUpdated(),
                e.getFreshness(),
                e.getCodeFit(),
                e.getGrade(),
                e.getVerifyMethod(),
                e.getGap(),
                e.getOwner(),
                e.getNote(),
                e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy());
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
