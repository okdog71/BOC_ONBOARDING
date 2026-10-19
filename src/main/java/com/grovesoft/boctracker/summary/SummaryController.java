package com.grovesoft.boctracker.summary;

import com.grovesoft.boctracker.common.docs.ApiTags;
import com.grovesoft.boctracker.common.web.ApiResponse;
import com.grovesoft.boctracker.summary.dto.SummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.grovesoft.boctracker.link.LinkIssue;
import com.grovesoft.boctracker.link.LinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = ApiTags.SUMMARY, description = "전체 대시보드 집계")
@RestController
@RequestMapping("/api/summary")
@RequiredArgsConstructor
public class SummaryController {

    private final SummaryService service;
    private final LinkService linkService;

    @Operation(summary = "전체 요약 집계",
            description = "각 트래커의 total + status별 count. menu 는 priority(상/중/하)별 count 추가")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ApiResponse<SummaryResponse> summary() {
        return ApiResponse.ok(service.summary());
    }

    @Operation(summary = "메뉴·문서 링크 점검",
            description = "인수인계→수급/접근/마일스톤 연결, 마일스톤 선행 코드, 인수인계 마일스톤→세션 연결을 점검해 끊어진 링크를 알려 준다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/links")
    public ApiResponse<LinkIssue.Report> links() {
        return ApiResponse.ok(linkService.audit());
    }
}
