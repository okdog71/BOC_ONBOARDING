package com.grovesoft.boctracker.supplydoc;

import com.grovesoft.boctracker.common.storage.FileStorage;
import com.grovesoft.boctracker.common.web.BusinessRuleException;
import com.grovesoft.boctracker.common.web.ConflictException;
import com.grovesoft.boctracker.common.web.NotFoundException;
import com.grovesoft.boctracker.supplydoc.dto.SupplyDocFileResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

/** 수급 산출물 첨부 파일 업로드·다운로드·삭제. 디스크 정리는 DB 트랜잭션 결과에 맞춘다. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplyDocFileService {

    /** 산출물당 첨부 최대 개수 */
    static final int MAX_FILES_PER_DOC = 20;
    /** 실행 파일·스크립트는 받지 않는다(보관·공유 목적이지 실행 대상이 아님) */
    static final Set<String> BLOCKED_EXTENSIONS = Set.of(
            "exe", "bat", "cmd", "com", "msi", "scr", "sh", "ps1", "vbs", "jar", "dll", "js");

    private final SupplyDocFileRepository repository;
    private final SupplyDocRepository docRepository;
    private final FileStorage storage;

    public record Download(Path path, String name, String contentType, long size) {
    }

    public Map<Long, List<SupplyDocFileResponse>> byDoc(Collection<Long> docIds) {
        if (docIds.isEmpty()) {
            return Map.of();
        }
        return repository.findBySupplyDocIdInOrderByIdAsc(docIds).stream()
                .collect(Collectors.groupingBy(f -> f.getSupplyDoc().getId(),
                        Collectors.mapping(this::toResponse, Collectors.toList())));
    }

    public List<SupplyDocFileResponse> of(Long docId) {
        return repository.findBySupplyDocIdOrderByIdAsc(docId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public void upload(Long docId, MultipartFile file) {
        SupplyDoc doc = docRepository.findById(docId).orElseThrow(() -> new NotFoundException("SupplyDoc", docId));
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("빈 파일은 올릴 수 없습니다.");
        }
        String name = sanitizeName(file.getOriginalFilename());
        String ext = extensionOf(name);
        if (BLOCKED_EXTENSIONS.contains(ext)) {
            throw new BusinessRuleException("올릴 수 없는 파일 형식입니다: ." + ext);
        }
        if (repository.countBySupplyDocId(docId) >= MAX_FILES_PER_DOC) {
            throw new BusinessRuleException("산출물당 첨부는 최대 " + MAX_FILES_PER_DOC + "개입니다.");
        }
        FileStorage.Stored stored;
        try (InputStream in = file.getInputStream()) {
            stored = storage.store(in);
        } catch (IOException e) {
            throw new UncheckedIOException("파일을 읽지 못했습니다.", e);
        }
        // 이 트랜잭션이 롤백되면 방금 쓴 파일도 지운다
        cleanupAfter(false, stored.storedName());
        if (repository.existsBySupplyDocIdAndSha256(docId, stored.sha256())) {
            throw new ConflictException("같은 내용의 파일이 이미 첨부되어 있습니다: " + name);
        }
        repository.save(new SupplyDocFile(doc, name, stored.storedName(), blankToNull(file.getContentType()),
                stored.size(), stored.sha256()));
    }

    public Download download(Long docId, Long fileId) {
        SupplyDocFile f = find(docId, fileId);
        Path path = storage.find(f.getStoredName());
        if (path == null) {
            throw new NotFoundException("SupplyDocFile(디스크 본문 없음)", f.getOriginalName());
        }
        return new Download(path, f.getOriginalName(), f.getContentType(), f.getSizeBytes());
    }

    @Transactional
    public void delete(Long docId, Long fileId) {
        SupplyDocFile f = find(docId, fileId);
        repository.delete(f);
        cleanupAfter(true, f.getStoredName());
    }

    /** 산출물 삭제 시 첨부 전체 정리 */
    @Transactional
    public void deleteAllOf(Long docId) {
        List<SupplyDocFile> files = repository.findBySupplyDocIdOrderByIdAsc(docId);
        repository.deleteAll(files);
        files.forEach(f -> cleanupAfter(true, f.getStoredName()));
    }

    public SupplyDocFileResponse toResponse(SupplyDocFile f) {
        Long docId = f.getSupplyDoc().getId();
        return new SupplyDocFileResponse(f.getId(), f.getOriginalName(), f.getContentType(), f.getSizeBytes(),
                "/api/supply-docs/" + docId + "/files/" + f.getId(), f.getCreatedAt());
    }

    private SupplyDocFile find(Long docId, Long fileId) {
        return repository.findById(fileId)
                .filter(f -> f.getSupplyDoc().getId().equals(docId))
                .orElseThrow(() -> new NotFoundException("SupplyDocFile", fileId));
    }

    /** @param onCommit true 면 커밋된 뒤에, false 면 롤백된 뒤에 디스크 파일을 지운다 */
    private void cleanupAfter(boolean onCommit, String storedName) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if ((status == STATUS_COMMITTED) == onCommit) {
                    storage.deleteQuietly(storedName);
                }
            }
        });
    }

    /** 경로 구분자·제어문자를 제거하고 길이를 제한한 표시용 파일명 */
    static String sanitizeName(String original) {
        String n = original == null ? "" : original.replace('\\', '/');
        n = n.substring(n.lastIndexOf('/') + 1).replaceAll("\\p{Cntrl}", "").trim();
        if (n.isEmpty() || n.equals(".") || n.equals("..")) {
            return "unnamed";
        }
        return n.length() > 200 ? n.substring(n.length() - 200) : n;
    }

    static String extensionOf(String name) {
        int i = name.lastIndexOf('.');
        return i < 0 ? "" : name.substring(i + 1).toLowerCase(Locale.ROOT);
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
