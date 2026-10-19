package com.grovesoft.boctracker.supplydoc;

import com.grovesoft.boctracker.common.docs.ApiTags;
import com.grovesoft.boctracker.common.docs.CreateResponses;
import com.grovesoft.boctracker.common.docs.DeleteResponses;
import com.grovesoft.boctracker.common.docs.GetResponses;
import com.grovesoft.boctracker.common.web.ApiResponse;
import com.grovesoft.boctracker.supplydoc.dto.SupplyDocResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = ApiTags.SUPPLY_DOC, description = "수급 산출물 관리")
@RestController
@RequestMapping("/api/supply-docs/{id}/files")
@RequiredArgsConstructor
public class SupplyDocFileController {

    private final SupplyDocFileService fileService;
    private final SupplyDocService docService;

    @Operation(summary = "수급 산출물 파일 업로드",
            description = "multipart/form-data 의 file 파트. 갱신된 산출물을 돌려준다. 실행 파일(.exe 등)·빈 파일·산출물당 20개 초과는 400, 같은 내용이 이미 있으면 409, 50MB 초과는 413. 계정·토큰 등 민감정보가 담긴 파일은 올리지 않는다.")
    @CreateResponses
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<SupplyDocResponse>> upload(@PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        fileService.upload(id, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(docService.get(id)));
    }

    @Operation(summary = "수급 산출물 파일 다운로드", description = "attachment 로 내려준다. 산출물 id 와 파일 id 가 맞지 않으면 404")
    @GetResponses
    @GetMapping("/{fileId}")
    public ResponseEntity<Resource> download(@PathVariable Long id, @PathVariable Long fileId) {
        SupplyDocFileService.Download d = fileService.download(id, fileId);
        MediaType type = MediaType.APPLICATION_OCTET_STREAM;
        if (d.contentType() != null) {
            try {
                type = MediaType.parseMediaType(d.contentType());
            } catch (RuntimeException ignored) {
                // 알 수 없는 타입은 octet-stream
            }
        }
        return ResponseEntity.ok()
                .contentType(type)
                .contentLength(d.size())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(d.name(), StandardCharsets.UTF_8).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(new FileSystemResource(d.path()));
    }

    @Operation(summary = "수급 산출물 파일 삭제")
    @DeleteResponses
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @PathVariable Long fileId) {
        fileService.delete(id, fileId);
        return ResponseEntity.noContent().build();
    }
}
