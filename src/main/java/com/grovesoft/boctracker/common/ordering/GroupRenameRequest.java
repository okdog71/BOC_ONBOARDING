package com.grovesoft.boctracker.common.ordering;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "그룹명 일괄 변경 요청. 변경할 이름이 이미 있으면 두 그룹이 병합된다. 기존 코드는 바뀌지 않는다.")
public record GroupRenameRequest(
        @Schema(description = "현재 그룹명", example = "저장소") @NotBlank @Size(max = 100) String from,
        @Schema(description = "새 그룹명", example = "소스 저장소") @NotBlank @Size(max = 100) String to) {
}
