package com.grovesoft.boctracker.catchup;

import com.grovesoft.boctracker.catchup.dto.CatchupActionRequest;
import com.grovesoft.boctracker.catchup.dto.CatchupActionResponse;
import com.grovesoft.boctracker.catchup.dto.CatchupActionStatusRequest;
import com.grovesoft.boctracker.common.docs.ApiTags;
import com.grovesoft.boctracker.common.docs.CreateResponses;
import com.grovesoft.boctracker.common.docs.DeleteResponses;
import com.grovesoft.boctracker.common.docs.GetResponses;
import com.grovesoft.boctracker.common.docs.ListResponses;
import com.grovesoft.boctracker.common.docs.StatusResponses;
import com.grovesoft.boctracker.common.docs.UpdateResponses;
import com.grovesoft.boctracker.common.web.ApiResponse;
import com.grovesoft.boctracker.common.web.PageResponse;
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

@Tag(name = ApiTags.CATCHUP, description = "캐치업 액션 관리")
@RestController
@RequestMapping("/api/catchup-actions")
@RequiredArgsConstructor
public class CatchupActionController {

    private final CatchupActionService service;

    @Operation(summary = "캐치업 액션 목록 조회", description = "status, timing 필터 + 페이징. 기본 정렬 id ASC")
    @ListResponses
    @GetMapping
    public ApiResponse<PageResponse<CatchupActionResponse>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String timing,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ApiResponse.ok(PageResponse.of(service.search(status, timing, pageable), r -> r));
    }

    @Operation(summary = "캐치업 액션 단건 조회")
    @GetResponses
    @GetMapping("/{id}")
    public ApiResponse<CatchupActionResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(service.get(id));
    }

    @Operation(summary = "캐치업 액션 생성")
    @CreateResponses
    @PostMapping
    public ResponseEntity<ApiResponse<CatchupActionResponse>> create(@Valid @RequestBody CatchupActionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(service.create(request)));
    }

    @Operation(summary = "캐치업 액션 전체 수정")
    @UpdateResponses
    @PutMapping("/{id}")
    public ApiResponse<CatchupActionResponse> update(@PathVariable Long id,
            @Valid @RequestBody CatchupActionRequest request) {
        return ApiResponse.ok(service.update(id, request));
    }

    @Operation(summary = "캐치업 액션 상태 변경")
    @StatusResponses
    @PatchMapping("/{id}/status")
    public ApiResponse<CatchupActionResponse> changeStatus(@PathVariable Long id,
            @Valid @RequestBody CatchupActionStatusRequest request) {
        return ApiResponse.ok(service.changeStatus(id, request.status()));
    }

    @Operation(summary = "캐치업 액션 삭제")
    @DeleteResponses
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
