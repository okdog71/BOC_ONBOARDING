package com.grovesoft.boctracker.menu.dto;

import com.grovesoft.boctracker.common.validation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "메뉴 인벤토리 상태 변경 요청")
public record MenuInventoryStatusRequest(
        @Schema(description = "상태", example = "진행중") @EnumValue(allowed = {"미착수", "진행중", "완료", "보류"}, nullable = false) String status) {
}
