package com.grovesoft.boctracker.milestone;

import com.grovesoft.boctracker.common.docs.ApiTags;
import com.grovesoft.boctracker.common.docs.CreateResponses;
import com.grovesoft.boctracker.common.docs.DeleteResponses;
import com.grovesoft.boctracker.common.docs.GetResponses;
import com.grovesoft.boctracker.common.docs.ListResponses;
import com.grovesoft.boctracker.common.docs.StatusResponses;
import com.grovesoft.boctracker.common.docs.UpdateResponses;
import com.grovesoft.boctracker.common.web.ApiResponse;
import com.grovesoft.boctracker.common.web.PageResponse;
import com.grovesoft.boctracker.milestone.dto.CheckItemToggleRequest;
import com.grovesoft.boctracker.milestone.dto.CheckItemsRequest;
import com.grovesoft.boctracker.milestone.dto.MilestoneOrderRequest;
import com.grovesoft.boctracker.milestone.dto.MilestoneRequest;
import com.grovesoft.boctracker.milestone.dto.PhaseRenameRequest;
import com.grovesoft.boctracker.milestone.dto.PhaseResponse;
import com.grovesoft.boctracker.milestone.dto.MilestoneResponse;
import com.grovesoft.boctracker.milestone.dto.MilestoneStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
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

@Tag(name = ApiTags.MILESTONE, description = "마일스톤 관리")
@RestController
@RequestMapping("/api/milestones")
@RequiredArgsConstructor
public class MilestoneController {

    private final MilestoneService service;

    @Operation(summary = "마일스톤 목록 조회",
            description = "status, phase, owner, q(코드·이름·목표 검색), overdue=true(지연만) 필터 + 페이징. 기본 정렬 id ASC")
    @ListResponses
    @GetMapping
    public ApiResponse<PageResponse<MilestoneResponse>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String phase,
            @RequestParam(required = false) String owner,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "false") boolean overdue,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ApiResponse.ok(PageResponse.of(service.search(status, phase, owner, q, overdue, pageable), r -> r));
    }

    @Operation(summary = "단계(Phase) 목록", description = "첫 등장 순서 기준 정렬, 단계별 전체/완료 건수 포함")
    @ListResponses
    @GetMapping("/phases")
    public ApiResponse<List<PhaseResponse>> phases() {
        return ApiResponse.ok(service.phases());
    }

    @Operation(summary = "단계명 일괄 변경", description = "대상 이름이 이미 있으면 병합된다. 존재하지 않는 단계면 404")
    @StatusResponses
    @PatchMapping("/phases")
    public ApiResponse<List<PhaseResponse>> renamePhase(@Valid @RequestBody PhaseRenameRequest request) {
        return ApiResponse.ok(service.renamePhase(request.from(), request.to()));
    }

    @Operation(summary = "단계 내 순서 변경(드래그앤드롭)",
            description = "phase 와 그 단계 전체 id 를 새 순서로 전달. 코드는 바뀌지 않고 No. 만 바뀐다. 목록이 맞지 않으면 400")
    @UpdateResponses
    @PutMapping("/order")
    public ApiResponse<List<MilestoneResponse>> reorder(@Valid @RequestBody MilestoneOrderRequest request) {
        return ApiResponse.ok(service.reorder(request.phase(), request.ids()));
    }

    @Operation(summary = "담당자 목록", description = "입력된 담당자 값 중복 제거(자동완성용)")
    @ListResponses
    @GetMapping("/owners")
    public ApiResponse<List<String>> owners() {
        return ApiResponse.ok(service.owners());
    }

    @Operation(summary = "마일스톤 단건 조회")
    @GetResponses
    @GetMapping("/{id}")
    public ApiResponse<MilestoneResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(service.get(id));
    }

    @Operation(summary = "마일스톤 생성")
    @CreateResponses
    @PostMapping
    public ResponseEntity<ApiResponse<MilestoneResponse>> create(@Valid @RequestBody MilestoneRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(service.create(request)));
    }

    @Operation(summary = "마일스톤 전체 수정")
    @UpdateResponses
    @PutMapping("/{id}")
    public ApiResponse<MilestoneResponse> update(@PathVariable Long id,
                                                 @Valid @RequestBody MilestoneRequest request) {
        return ApiResponse.ok(service.update(id, request));
    }

    @Operation(summary = "마일스톤 상태 변경")
    @StatusResponses
    @PatchMapping("/{id}/status")
    public ApiResponse<MilestoneResponse> changeStatus(@PathVariable Long id,
                                                       @Valid @RequestBody MilestoneStatusRequest request) {
        return ApiResponse.ok(service.changeStatus(id, request.status()));
    }

    @Operation(summary = "체크리스트 전체 교체", description = "목록 순서가 정렬 순서. 목록에 없는 기존 항목은 삭제")
    @UpdateResponses
    @PutMapping("/{id}/check-items")
    public ApiResponse<MilestoneResponse> replaceCheckItems(@PathVariable Long id,
                                                            @Valid @RequestBody CheckItemsRequest request) {
        return ApiResponse.ok(service.replaceCheckItems(id, request));
    }

    @Operation(summary = "체크리스트 항목 완료 토글")
    @StatusResponses
    @PatchMapping("/{id}/check-items/{itemId}")
    public ApiResponse<MilestoneResponse> toggleCheckItem(@PathVariable Long id, @PathVariable Long itemId,
                                                          @RequestBody CheckItemToggleRequest request) {
        return ApiResponse.ok(service.toggleCheckItem(id, itemId, request.done()));
    }

    @Operation(summary = "체크포인트로 체크리스트 생성",
            description = "체크포인트 문장을 · / 줄바꿈 / ; 로 나눠 항목 생성. 이미 항목이 있으면 409")
    @CreateResponses
    @PostMapping("/{id}/check-items/generate")
    public ResponseEntity<ApiResponse<MilestoneResponse>> generateCheckItems(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(service.generateCheckItems(id)));
    }

    @Operation(summary = "마일스톤 삭제", description = "다른 마일스톤이 선행으로 참조 중이면 409")
    @DeleteResponses
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
