package com.grovesoft.boctracker.supplydoc;

import com.grovesoft.boctracker.supplydoc.dto.SupplyDocRequest;
import com.grovesoft.boctracker.supplydoc.dto.SupplyDocResponse;
import org.springframework.stereotype.Component;

@Component
public class SupplyDocMapper {

    public SupplyDoc toEntity(SupplyDocRequest r) {
        return SupplyDoc.builder()
                .category(r.category())
                .name(r.name())
                .expectedContent(r.expectedContent())
                .usagePurpose(r.usagePurpose())
                .existence(blankToNull(r.existence()))
                .location(r.location())
                .freshness(blankToNull(r.freshness()))
                .owner(r.owner())
                .supplyStatus(blankToNull(r.supplyStatus()))
                .note(r.note())
                .build();
    }

    public SupplyDocResponse toResponse(SupplyDoc e) {
        return new SupplyDocResponse(
                e.getId(),
                e.getCategory(),
                e.getName(),
                e.getExpectedContent(),
                e.getUsagePurpose(),
                e.getExistence(),
                e.getLocation(),
                e.getFreshness(),
                e.getOwner(),
                e.getSupplyStatus(),
                e.getNote(),
                e.getCreatedAt(), e.getUpdatedAt());
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
