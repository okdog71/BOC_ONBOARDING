package com.grovesoft.boctracker.megaprocess.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "메가프로세스 생성/수정 요청")
public record MegaProcessRequest(
        @Schema(description = "L0", example = "리딤/주문") @NotBlank @Size(max = 255) String l0,
        @Schema(description = "L1", example = "쿠폰 리딤") @NotBlank @Size(max = 255) String l1,
        @Schema(description = "L2", example = "코드 검증") @Size(max = 255) String l2,
        @Schema(description = "L3") @Size(max = 255) String l3,
        @Schema(description = "관련 메뉴", example = "리딤 > 쿠폰 등록") @Size(max = 1000) String relatedMenu,
        @Schema(description = "관련 서비스") @Size(max = 1000) String relatedService,
        @Schema(description = "외부 연동") @Size(max = 1000) String externalLink,
        @Schema(description = "산출 데이터") @Size(max = 1000) String outputData,
        @Schema(description = "비고") @Size(max = 1000) String note) {
}
