package com.grovesoft.boctracker.review;

import com.grovesoft.boctracker.common.docs.ApiTags;
import com.grovesoft.boctracker.common.docs.CreateResponses;
import com.grovesoft.boctracker.common.docs.DeleteResponses;
import com.grovesoft.boctracker.common.docs.GetResponses;
import com.grovesoft.boctracker.common.docs.ListResponses;
import com.grovesoft.boctracker.common.docs.StatusResponses;
import com.grovesoft.boctracker.common.docs.UpdateResponses;
import com.grovesoft.boctracker.common.web.ApiResponse;
import com.grovesoft.boctracker.common.web.PageResponse;
import com.grovesoft.boctracker.review.dto.ReviewLogRequest;
import com.grovesoft.boctracker.review.dto.ReviewLogResponse;
import com.grovesoft.boctracker.review.dto.ReviewLogStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = ApiTags.REVIEW, description = "회고 리뷰 로그 관리")
@RestController
@RequestMapping("/api/review-logs")
@RequiredArgsConstructor
public class ReviewLogController {

    private final ReviewLogService service;

    @Operation(summary = "회고 리뷰 로그 목록 조회", description = "status 필터 + 페이징. 기본 정렬 id ASC")
    @ListResponses
    @GetMapping
    public ApiResponse<PageResponse<ReviewLogResponse>> list(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ApiResponse.ok(PageResponse.of(service.search(status, pageable), r -> r));
    }

    @Operation(summary = "회고 리뷰 로그 단건 조회")
    @GetResponses
    @GetMapping("/{id}")
    public ApiResponse<ReviewLogResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(service.get(id));
    }

    @Operation(summary = "회고 리뷰 로그 생성")
    @CreateResponses
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewLogResponse>> create(@Valid @RequestBody ReviewLogRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(service.create(request)));
    }

    @Operation(summary = "회고 리뷰 로그 전체 수정")
    @UpdateResponses
    @PutMapping("/{id}")
    public ApiResponse<ReviewLogResponse> update(@PathVariable Long id,
            @Valid @RequestBody ReviewLogRequest request) {
        return ApiResponse.ok(service.update(id, request));
    }

    @Operation(summary = "회고 리뷰 로그 상태 변경")
    @StatusResponses
    @PatchMapping("/{id}/status")
    public ApiResponse<ReviewLogResponse> changeStatus(@PathVariable Long id,
            @Valid @RequestBody ReviewLogStatusRequest request) {
        return ApiResponse.ok(service.changeStatus(id, request.status()));
    }

    @Operation(summary = "회고 리뷰 로그 삭제")
    @DeleteResponses
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
