package com.grovesoft.boctracker.auditlog;

import com.grovesoft.boctracker.auditlog.dto.AuditLogResponse;
import com.grovesoft.boctracker.common.docs.ApiTags;
import com.grovesoft.boctracker.common.docs.ListResponses;
import com.grovesoft.boctracker.common.web.ApiResponse;
import com.grovesoft.boctracker.common.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = ApiTags.AUDIT_LOG, description = "모든 도메인 데이터의 등록·수정·삭제 이력(조회 전용)")
@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService service;

    @Operation(summary = "변경 이력 목록 조회",
            description = "도메인·사용자·작업(CREATE/UPDATE/DELETE)·대상 코드·IP·기간(from~to, 포함)·검색어(q: 대상 코드·이름·변경 내용) 필터 + 페이징. 기본 정렬 최신순")
    @ListResponses
    @GetMapping
    public ApiResponse<PageResponse<AuditLogResponse>> list(
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 50, sort = "id", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(PageResponse.of(service.search(domain, actor, action, code, ip, from, to, q, pageable), r -> r));
    }

    @Operation(summary = "도메인 목록", description = "도메인 필터에 쓸 이름 목록(화면 메뉴 순서)")
    @ListResponses
    @GetMapping("/domains")
    public ApiResponse<List<String>> domains() {
        return ApiResponse.ok(AuditDomains.all());
    }
}
