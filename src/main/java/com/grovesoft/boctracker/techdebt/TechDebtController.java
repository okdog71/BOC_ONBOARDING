package com.grovesoft.boctracker.techdebt;

import com.grovesoft.boctracker.common.attachment.AttachmentHttp;
import com.grovesoft.boctracker.common.attachment.AttachmentOwner;
import com.grovesoft.boctracker.common.attachment.AttachmentService;
import com.grovesoft.boctracker.common.docs.ApiTags;
import com.grovesoft.boctracker.common.docs.CreateResponses;
import com.grovesoft.boctracker.common.docs.DeleteResponses;
import com.grovesoft.boctracker.common.docs.GetResponses;
import com.grovesoft.boctracker.common.docs.ListResponses;
import com.grovesoft.boctracker.common.docs.StatusResponses;
import com.grovesoft.boctracker.common.docs.UpdateResponses;
import com.grovesoft.boctracker.common.ordering.GroupRenameRequest;
import com.grovesoft.boctracker.common.ordering.OrderRequest;
import com.grovesoft.boctracker.common.web.ApiResponse;
import com.grovesoft.boctracker.common.web.PageResponse;
import com.grovesoft.boctracker.techdebt.dto.TechDebtRequest;
import com.grovesoft.boctracker.techdebt.dto.TechDebtResponse;
import com.grovesoft.boctracker.techdebt.dto.TechDebtStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

@Tag(name = ApiTags.TECH_DEBT, description = "기술부채 관리대장")
@RestController
@RequestMapping("/api/tech-debts")
@RequiredArgsConstructor
public class TechDebtController {

    private static final AttachmentOwner OWNER = AttachmentOwner.TECH_DEBT;
    private static final MediaType XLSX =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final TechDebtService service;
    private final AttachmentService attachments;
    private final Clock clock;

    @Operation(summary = "기술부채 목록 조회",
            description = "status, priority, system, owner, q(코드·항목명·요약·영역 검색), overdue=true(지연만) 필터 + 페이징. 기본 정렬 id ASC")
    @ListResponses
    @GetMapping
    public ApiResponse<PageResponse<TechDebtResponse>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String system,
            @RequestParam(required = false) String owner,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "false") boolean overdue,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ApiResponse.ok(PageResponse.of(service.search(status, priority, system, owner, q, overdue, pageable), r -> r));
    }

    @Operation(summary = "기술부채 엑셀 내보내기",
            description = "목록과 같은 필터(status, priority, system, owner, q, overdue)를 적용해 관리대장 양식(.xlsx)으로 내려준다. 우선순위·No. 순")
    @ListResponses
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String system,
            @RequestParam(required = false) String owner,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "false") boolean overdue) {
        byte[] body = service.exportXlsx(status, priority, system, owner, q, overdue);
        String name = "기술부채_관리대장_" + LocalDate.now(clock).toString().replace("-", "") + ".xlsx";
        return ResponseEntity.ok().contentType(XLSX)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(name, StandardCharsets.UTF_8).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(body);
    }

    @Operation(summary = "기술부채 우선순위 내 순서 변경(드래그앤드롭)",
            description = "group(우선순위) 과 그 그룹 전체 id 를 새 순서로 전달. 코드는 바뀌지 않고 No. 만 바뀐다. 목록이 맞지 않으면 400")
    @UpdateResponses
    @PutMapping("/order")
    public ApiResponse<List<TechDebtResponse>> reorder(@Valid @RequestBody OrderRequest request) {
        return ApiResponse.ok(service.reorder(request.group(), request.ids()));
    }

    @Operation(summary = "기술부채 우선순위(그룹)명 일괄 변경", description = "대상 이름이 이미 있으면 병합. 없는 그룹이면 404")
    @StatusResponses
    @PatchMapping("/groups")
    public ApiResponse<Void> renameGroup(@Valid @RequestBody GroupRenameRequest request) {
        service.renameGroup(request.from(), request.to());
        return ApiResponse.ok(null);
    }

    @Operation(summary = "기술부채 단건 조회")
    @GetResponses
    @GetMapping("/{id}")
    public ApiResponse<TechDebtResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(service.get(id));
    }

    @Operation(summary = "기술부채 생성", description = "코드를 생략하면 시스템 접두로 자동 부여(KEC-19 등)")
    @CreateResponses
    @PostMapping
    public ResponseEntity<ApiResponse<TechDebtResponse>> create(@Valid @RequestBody TechDebtRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(service.create(request)));
    }

    @Operation(summary = "기술부채 전체 수정", description = "코드는 변경 불가. 완료 상태에는 담당이 필요(400)")
    @UpdateResponses
    @PutMapping("/{id}")
    public ApiResponse<TechDebtResponse> update(@PathVariable Long id, @Valid @RequestBody TechDebtRequest request) {
        return ApiResponse.ok(service.update(id, request));
    }

    @Operation(summary = "기술부채 상태 변경", description = "완료로 바꾸려면 담당이 지정되어 있어야 한다(400). 완료일은 서버가 기록")
    @StatusResponses
    @PatchMapping("/{id}/status")
    public ApiResponse<TechDebtResponse> changeStatus(@PathVariable Long id,
            @Valid @RequestBody TechDebtStatusRequest request) {
        return ApiResponse.ok(service.changeStatus(id, request.status()));
    }

    @Operation(summary = "기술부채 삭제", description = "첨부 파일도 함께 삭제")
    @DeleteResponses
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "기술부채 첨부 파일 업로드",
            description = "multipart/form-data 의 file 파트. 갱신된 항목을 돌려준다. 실행 파일·빈 파일·20개 초과는 400, 같은 내용이 있으면 409, 50MB 초과는 413. 계정·토큰 등 민감정보가 담긴 파일은 올리지 않는다.")
    @CreateResponses
    @PostMapping(value = "/{id}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<TechDebtResponse>> upload(@PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        service.requireExists(id);
        attachments.upload(OWNER, id, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(service.get(id)));
    }

    @Operation(summary = "기술부채 첨부 파일 다운로드", description = "attachment 로 내려준다. 항목 id 와 파일 id 가 맞지 않으면 404")
    @GetResponses
    @GetMapping("/{id}/files/{fileId}")
    public ResponseEntity<Resource> download(@PathVariable Long id, @PathVariable Long fileId) {
        return AttachmentHttp.download(attachments.download(OWNER, id, fileId));
    }

    @Operation(summary = "기술부채 첨부 파일 삭제")
    @DeleteResponses
    @DeleteMapping("/{id}/files/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long id, @PathVariable Long fileId) {
        attachments.delete(OWNER, id, fileId);
        return ResponseEntity.noContent().build();
    }
}
