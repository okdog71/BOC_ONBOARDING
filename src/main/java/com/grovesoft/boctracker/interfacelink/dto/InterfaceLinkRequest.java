package com.grovesoft.boctracker.interfacelink.dto;

import com.grovesoft.boctracker.common.validation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "연동 인터페이스 생성/수정 요청")
public record InterfaceLinkRequest(
        @Schema(description = "시스템명", example = "결제 게이트웨이") @NotBlank @Size(max = 255) String systemName,
        @Schema(description = "방향", example = "BOC→") @Size(max = 50) String direction,
        @Schema(description = "인터페이스명", example = "결제 승인 API") @Size(max = 255) String interfaceName,
        @Schema(description = "용도", example = "결제 승인 요청") @Size(max = 1000) String usage,
        @Schema(description = "인증 방식", example = "API Key") @Size(max = 255) String auth,
        @Schema(description = "주요 데이터", example = "주문번호, 금액") @Size(max = 1000) String data,
        @Schema(description = "dev 확인(확인/dev미노출/mock/미확인)", example = "확인") @EnumValue(allowed = {"확인", "dev미노출", "mock", "미확인"}) @Size(max = 20) String devCheck,
        @Schema(description = "비고") @Size(max = 1000) String note) {
}
