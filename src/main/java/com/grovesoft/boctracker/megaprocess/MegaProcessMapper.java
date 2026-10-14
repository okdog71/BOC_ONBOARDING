package com.grovesoft.boctracker.megaprocess;

import com.grovesoft.boctracker.megaprocess.dto.MegaProcessRequest;
import com.grovesoft.boctracker.megaprocess.dto.MegaProcessResponse;
import org.springframework.stereotype.Component;

@Component
public class MegaProcessMapper {

    public MegaProcess toEntity(MegaProcessRequest r) {
        return MegaProcess.builder()
                .l0(r.l0())
                .l1(r.l1())
                .l2(r.l2())
                .l3(r.l3())
                .relatedMenu(r.relatedMenu())
                .relatedService(r.relatedService())
                .externalLink(r.externalLink())
                .outputData(r.outputData())
                .note(r.note())
                .build();
    }

    public MegaProcessResponse toResponse(MegaProcess e) {
        return new MegaProcessResponse(
                e.getId(),
                e.getL0(),
                e.getL1(),
                e.getL2(),
                e.getL3(),
                e.getRelatedMenu(),
                e.getRelatedService(),
                e.getExternalLink(),
                e.getOutputData(),
                e.getNote(),
                e.getCreatedAt(), e.getUpdatedAt());
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
