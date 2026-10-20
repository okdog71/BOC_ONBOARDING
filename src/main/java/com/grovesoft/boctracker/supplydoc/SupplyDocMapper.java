package com.grovesoft.boctracker.supplydoc;

import com.grovesoft.boctracker.supplydoc.dto.SupplyDocFileResponse;
import com.grovesoft.boctracker.supplydoc.dto.SupplyDocRequest;
import com.grovesoft.boctracker.supplydoc.dto.SupplyDocResponse;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SupplyDocMapper {

    public SupplyDoc toEntity(SupplyDocRequest r) {
        return SupplyDoc.builder()
                .code(blankToNull(r.code()))
                .category(r.category().trim())
                .name(r.name().trim())
                .expectedContent(r.expectedContent())
                .usagePurpose(r.usagePurpose())
                .existence(blankToNull(r.existence()))
                .location(r.location())
                .freshness(blankToNull(r.freshness()))
                .owner(blankToNull(r.owner()))
                .supplyStatus(blankToNull(r.supplyStatus()))
                .targetDate(r.targetDate())
                .note(r.note())
                .build();
    }

    /** @param requiredBy 이 산출물을 요구하는 인수인계 세션 코드 */
    public SupplyDocResponse toResponse(SupplyDoc e, LocalDate today, List<String> requiredBy,
                                        List<SupplyDocFileResponse> files) {
        return new SupplyDocResponse(
                e.getId(), e.getCode(), e.getSortOrder(), e.getCategory(), e.getName(), e.getExpectedContent(),
                e.getUsagePurpose(), e.getExistence(), e.getLocation(), e.getFreshness(), e.getOwner(),
                e.getSupplyStatus(), e.getTargetDate(), e.getRequestedDate(), e.getReceivedDate(),
                e.isReady(), e.isOverdue(today), e.warnings(files.size()), requiredBy, files, e.getNote(),
                e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy());
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
