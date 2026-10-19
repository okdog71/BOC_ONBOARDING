package com.grovesoft.boctracker.handoverquestion.dto;

import com.grovesoft.boctracker.common.attachment.dto.AttachmentResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "인수인계 질의 응답")
public record HandoverQuestionResponse(
        @Schema(description = "ID", example = "1") Long id,
        @Schema(description = "코드(불변 식별자)", example = "Q-01") String code,
        @Schema(description = "우선순위 그룹 내 순서(No.)", example = "1") int no,
        @Schema(description = "우선순위", example = "높음") String priority,
        @Schema(description = "구분", example = "추정 I/F") String kind,
        @Schema(description = "관련 I/F·노드") String relatedNode,
        @Schema(description = "질문") String question,
        @Schema(description = "근거(문서·스웨거)") String basis,
        @Schema(description = "확인 대상") String askTo,
        @Schema(description = "가설(확인 전 추정, 참고용·사실 아님)") String hypothesis,
        @Schema(description = "구성도 반영 방법") String reflectPlan,
        @Schema(description = "상태", example = "미확인") String status,
        @Schema(description = "확인 답변") String answer,
        @Schema(description = "확인자") String confirmedBy,
        @Schema(description = "확인일") LocalDate confirmedDate,
        @Schema(description = "확인완료 여부", example = "false") boolean done,
        @Schema(description = "지연 여부(질의에는 기한이 없어 항상 false)", example = "false") boolean overdue,
        @Schema(description = "데이터 품질 경고(차단하지 않음)") List<String> warnings,
        @Schema(description = "첨부 파일(답변 증빙 등)") List<AttachmentResponse> files,
        @Schema(description = "비고") String note,
        @Schema(description = "생성 시각") LocalDateTime createdAt,
        @Schema(description = "수정 시각") LocalDateTime updatedAt) {
}
