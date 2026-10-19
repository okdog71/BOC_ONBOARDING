package com.grovesoft.boctracker.techdebt.dto;

import com.grovesoft.boctracker.common.attachment.dto.AttachmentResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "기술부채 응답")
public record TechDebtResponse(
        @Schema(description = "ID", example = "1") Long id,
        @Schema(description = "코드(불변 식별자)", example = "KEC-01") String code,
        @Schema(description = "우선순위 그룹 내 순서(No.)", example = "1") int no,
        @Schema(description = "우선순위", example = "P1") String priority,
        @Schema(description = "시스템", example = "KEC") String system,
        @Schema(description = "영역", example = "연동/장애") String area,
        @Schema(description = "항목명", example = "enroll provider 매칭 회귀") String name,
        @Schema(description = "내용 요약") String summary,
        @Schema(description = "리스크·영향") String impact,
        @Schema(description = "근거(문서·조항)") String basis,
        @Schema(description = "대응(처리) 방안") String remedy,
        @Schema(description = "확인 필요처") String confirmWith,
        @Schema(description = "담당") String owner,
        @Schema(description = "상태", example = "미착수") String status,
        @Schema(description = "검증 기준") String verifyCriteria,
        @Schema(description = "목표 시점") LocalDate targetDate,
        @Schema(description = "완료일(서버 기록)") LocalDate completedDate,
        @Schema(description = "완료 여부", example = "false") boolean done,
        @Schema(description = "지연 여부(목표 시점 경과 & 완료·보류 아님)", example = "false") boolean overdue,
        @Schema(description = "데이터 품질 경고(차단하지 않음)") List<String> warnings,
        @Schema(description = "첨부 파일(근거 자료 등)") List<AttachmentResponse> files,
        @Schema(description = "비고") String note,
        @Schema(description = "생성 시각") LocalDateTime createdAt,
        @Schema(description = "최종 업데이트 시각") LocalDateTime updatedAt) {
}
