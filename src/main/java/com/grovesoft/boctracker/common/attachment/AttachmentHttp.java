package com.grovesoft.boctracker.common.attachment;

import java.nio.charset.StandardCharsets;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/** 첨부 다운로드 응답 조립(항목별 컨트롤러 공용): attachment + nosniff. */
public final class AttachmentHttp {

    private AttachmentHttp() {
    }

    public static ResponseEntity<Resource> download(AttachmentService.Download d) {
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
}
