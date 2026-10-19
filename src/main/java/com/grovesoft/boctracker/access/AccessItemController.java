package com.grovesoft.boctracker.access;

import com.grovesoft.boctracker.access.dto.AccessItemRequest;
import com.grovesoft.boctracker.access.dto.AccessItemResponse;
import com.grovesoft.boctracker.access.dto.AccessItemStatusRequest;
import com.grovesoft.boctracker.common.docs.ApiTags;
import com.grovesoft.boctracker.common.docs.CreateResponses;
import com.grovesoft.boctracker.common.docs.DeleteResponses;
import com.grovesoft.boctracker.common.docs.GetResponses;
import com.grovesoft.boctracker.common.docs.ListResponses;
import com.grovesoft.boctracker.common.docs.StatusResponses;
import com.grovesoft.boctracker.common.docs.UpdateResponses;
import com.grovesoft.boctracker.common.ordering.OrderRequest;
import java.util.List;
import com.grovesoft.boctracker.common.ordering.GroupRenameRequest;
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

@Tag(name = ApiTags.ACCESS, description = "접근권한 관리")
@RestController
@RequestMapping("/api/access-items")
@RequiredArgsConstructor
public class AccessItemController {

    private final AccessItemService service;

    @Operation(summary = "접근권한 목록 조회",
            description = "status, category, owner, q(코드·대상·용도 검색), overdue=true(지연만) 필터 + 페이징. 기본 정렬 id ASC")
    @ListResponses
    @GetMapping
    public ApiResponse<PageResponse<AccessItemResponse>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String owner,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "false") boolean overdue,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ApiResponse.ok(PageResponse.of(
                service.search(status, category, owner, q, overdue, pageable), r -> r));
    }

    @Operation(summary = "접근권한 그룹 내 순서 변경(드래그앤드롭)",
            description = "group(구분) 과 그 그룹 전체 id 를 새 순서로 전달. 코드는 바뀌지 않고 No. 만 바뀐다. 목록이 맞지 않으면 400")
    @UpdateResponses
    @PutMapping("/order")
    public ApiResponse<List<AccessItemResponse>> reorder(@Valid @RequestBody OrderRequest request) {
        return ApiResponse.ok(service.reorder(request.group(), request.ids()));
    }

    @Operation(summary = "접근권한 구분명 일괄 변경", description = "대상 이름이 이미 있으면 병합. 기존 코드는 바뀌지 않는다. 없는 구분이면 404")
    @StatusResponses
    @PatchMapping("/groups")
    public ApiResponse<Void> renameGroup(@Valid @RequestBody GroupRenameRequest request) {
        service.renameCategory(request.from(), request.to());
        return ApiResponse.ok(null);
    }


    @Operation(summary = "접근권한 단건 조회")
    @GetResponses
    @GetMapping("/{id}")
    public ApiResponse<AccessItemResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(service.get(id));
    }

    @Operation(summary = "접근권한 생성")
    @CreateResponses
    @PostMapping
    public ResponseEntity<ApiResponse<AccessItemResponse>> create(@Valid @RequestBody AccessItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(service.create(request)));
    }

    @Operation(summary = "접근권한 전체 수정")
    @UpdateResponses
    @PutMapping("/{id}")
    public ApiResponse<AccessItemResponse> update(@PathVariable Long id,
            @Valid @RequestBody AccessItemRequest request) {
        return ApiResponse.ok(service.update(id, request));
    }

    @Operation(summary = "접근권한 상태 변경")
    @StatusResponses
    @PatchMapping("/{id}/status")
    public ApiResponse<AccessItemResponse> changeStatus(@PathVariable Long id,
            @Valid @RequestBody AccessItemStatusRequest request) {
        return ApiResponse.ok(service.changeStatus(id, request.status()));
    }

    @Operation(summary = "접근권한 삭제", description = "인수인계 세션이 선행으로 요구 중이면 409")
    @DeleteResponses
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
