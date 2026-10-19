package com.grovesoft.boctracker.handoverquestion.dto;

import com.grovesoft.boctracker.common.validation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "인수인계 질의 생성/수정 요청")
public record HandoverQuestionRequest(
        @Schema(description = "코드. 생성 시 생략하면 Q-41 처럼 자동 부여. 부여 후 변경 불가", example = "Q-01")
        @Size(max = 50) @Pattern(regexp = "^[A-Za-z]+-\\d+$", message = "코드는 Q-01 형식이어야 합니다") String code,
        @Schema(description = "우선순위(높음/중/낮음)", example = "높음")
        @EnumValue(allowed = {"높음", "중", "낮음"}, nullable = false) String priority,
        @Schema(description = "구분", example = "추정 I/F") @Size(max = 100) String kind,
        @Schema(description = "관련 I/F·노드", example = "IF-29 ec-admin → kecdata") @Size(max = 500) String relatedNode,
        @Schema(description = "질문", example = "후지급 추첨 배치는 ec-admin 내부 스케줄러인가?") @NotBlank @Size(max = 1000) String question,
        @Schema(description = "근거(문서·스웨거)") @Size(max = 1000) String basis,
        @Schema(description = "확인 대상(역할·조직)", example = "KEC 개발 리드") @Size(max = 255) String askTo,
        @Schema(description = "가설(확인 전 추정, 참고용·사실 아님)") @Size(max = 1000) String hypothesis,
        @Schema(description = "구성도 반영 방법") @Size(max = 1000) String reflectPlan,
        @Schema(description = "상태(미확인/진행중/확인완료/보류). 생략하면 미확인. 확인완료는 확인 답변·확인자가 필요", example = "미확인")
        @EnumValue(allowed = {"미확인", "진행중", "확인완료", "보류"}) String status,
        @Schema(description = "확인 답변(실제로 확인한 내용). 계정·토큰 등 민감정보는 적지 않는다") @Size(max = 2000) String answer,
        @Schema(description = "확인자") @Size(max = 100) String confirmedBy,
        @Schema(description = "확인일. 확인완료인데 비우면 서버가 오늘로 기록", example = "2026-10-20") LocalDate confirmedDate,
        @Schema(description = "비고") @Size(max = 1000) String note) {
}
