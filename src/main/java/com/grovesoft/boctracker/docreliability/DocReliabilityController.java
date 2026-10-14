package com.grovesoft.boctracker.docreliability;

import com.grovesoft.boctracker.common.docs.ApiTags;
import com.grovesoft.boctracker.common.docs.CreateResponses;
import com.grovesoft.boctracker.common.docs.DeleteResponses;
import com.grovesoft.boctracker.common.docs.GetResponses;
import com.grovesoft.boctracker.common.docs.ListResponses;
import com.grovesoft.boctracker.common.docs.UpdateResponses;
import com.grovesoft.boctracker.common.web.ApiResponse;
import com.grovesoft.boctracker.common.web.PageResponse;
import com.grovesoft.boctracker.docreliability.dto.DocReliabilityRequest;
import com.grovesoft.boctracker.docreliability.dto.DocReliabilityResponse;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = ApiTags.DOC_RELIABILITY, description = "문서 신뢰도 관리")
@RestController
@RequestMapping("/api/doc-reliabilities")
@RequiredArgsConstructor
public class DocReliabilityController {

    private final DocReliabilityService service;

    @Operation(summary = "문서 신뢰도 목록 조회", description = "grade, freshness 필터 + 페이징. 기본 정렬 id ASC")
    @ListResponses
    @GetMapping
    public ApiResponse<PageResponse<DocReliabilityResponse>> list(
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String freshness,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ApiResponse.ok(PageResponse.of(service.search(grade, freshness, pageable), r -> r));
    }

    @Operation(summary = "문서 신뢰도 단건 조회")
    @GetResponses
    @GetMapping("/{id}")
    public ApiResponse<DocReliabilityResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(service.get(id));
    }

    @Operation(summary = "문서 신뢰도 생성")
    @CreateResponses
    @PostMapping
    public ResponseEntity<ApiResponse<DocReliabilityResponse>> create(@Valid @RequestBody DocReliabilityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(service.create(request)));
    }

    @Operation(summary = "문서 신뢰도 전체 수정")
    @UpdateResponses
    @PutMapping("/{id}")
    public ApiResponse<DocReliabilityResponse> update(@PathVariable Long id,
            @Valid @RequestBody DocReliabilityRequest request) {
        return ApiResponse.ok(service.update(id, request));
    }

    @Operation(summary = "문서 신뢰도 삭제")
    @DeleteResponses
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
