package com.grovesoft.boctracker.raci;

import com.grovesoft.boctracker.common.docs.ApiTags;
import com.grovesoft.boctracker.common.docs.CreateResponses;
import com.grovesoft.boctracker.common.docs.DeleteResponses;
import com.grovesoft.boctracker.common.docs.GetResponses;
import com.grovesoft.boctracker.common.docs.ListResponses;
import com.grovesoft.boctracker.common.docs.StatusResponses;
import com.grovesoft.boctracker.common.docs.UpdateResponses;
import com.grovesoft.boctracker.common.web.ApiResponse;
import com.grovesoft.boctracker.common.web.PageResponse;
import com.grovesoft.boctracker.raci.dto.RaciAssignmentRequest;
import com.grovesoft.boctracker.raci.dto.RaciAssignmentResponse;
import com.grovesoft.boctracker.raci.dto.RaciAssignmentStatusRequest;
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

@Tag(name = ApiTags.RACI, description = "RACI 배정 관리")
@RestController
@RequestMapping("/api/raci-assignments")
@RequiredArgsConstructor
public class RaciAssignmentController {

    private final RaciAssignmentService service;

    @Operation(summary = "RACI 배정 목록 조회", description = "status 필터 + 페이징. 기본 정렬 id ASC")
    @ListResponses
    @GetMapping
    public ApiResponse<PageResponse<RaciAssignmentResponse>> list(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ApiResponse.ok(PageResponse.of(service.search(status, pageable), r -> r));
    }

    @Operation(summary = "RACI 배정 단건 조회")
    @GetResponses
    @GetMapping("/{id}")
    public ApiResponse<RaciAssignmentResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(service.get(id));
    }

    @Operation(summary = "RACI 배정 생성")
    @CreateResponses
    @PostMapping
    public ResponseEntity<ApiResponse<RaciAssignmentResponse>> create(@Valid @RequestBody RaciAssignmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(service.create(request)));
    }

    @Operation(summary = "RACI 배정 전체 수정")
    @UpdateResponses
    @PutMapping("/{id}")
    public ApiResponse<RaciAssignmentResponse> update(@PathVariable Long id,
            @Valid @RequestBody RaciAssignmentRequest request) {
        return ApiResponse.ok(service.update(id, request));
    }

    @Operation(summary = "RACI 배정 상태 변경")
    @StatusResponses
    @PatchMapping("/{id}/status")
    public ApiResponse<RaciAssignmentResponse> changeStatus(@PathVariable Long id,
            @Valid @RequestBody RaciAssignmentStatusRequest request) {
        return ApiResponse.ok(service.changeStatus(id, request.status()));
    }

    @Operation(summary = "RACI 배정 삭제")
    @DeleteResponses
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
