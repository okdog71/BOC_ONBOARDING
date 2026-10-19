package com.grovesoft.boctracker.common.storage;

import com.grovesoft.boctracker.common.web.BusinessRuleException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 업로드 파일을 로컬 디스크에 보관한다. 저장 이름은 서버가 만든 UUID 라서 사용자가 준 파일명이 경로에 쓰이지 않는다.
 * 원본 이름·타입 같은 메타데이터는 DB 가 가진다.
 */
@Slf4j
@Component
public class FileStorage {

    public record Stored(String storedName, long size, String sha256) {
    }

    private final Path root;

    public FileStorage(@Value("${app.storage.dir:./data/uploads}") String dir) {
        this.root = Path.of(dir).toAbsolutePath().normalize();
    }

    public Stored store(InputStream in) {
        String storedName = UUID.randomUUID().toString();
        Path target = resolve(storedName);
        try {
            Files.createDirectories(root);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (DigestInputStream din = new DigestInputStream(in, digest)) {
                Files.copy(din, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return new Stored(storedName, Files.size(target), HexFormat.of().formatHex(digest.digest()));
        } catch (IOException e) {
            deleteQuietly(storedName);
            throw new UncheckedIOException("파일 저장에 실패했습니다.", e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** 저장된 파일 경로. 없으면 null */
    public Path find(String storedName) {
        Path p = resolve(storedName);
        return Files.isRegularFile(p) ? p : null;
    }

    public void deleteQuietly(String storedName) {
        try {
            Files.deleteIfExists(resolve(storedName));
        } catch (IOException | BusinessRuleException e) {
            log.warn("첨부 파일 삭제 실패(무시): {}", storedName, e);
        }
    }

    private Path resolve(String storedName) {
        Path p = root.resolve(storedName).normalize();
        if (!p.startsWith(root) || p.equals(root)) {
            throw new BusinessRuleException("잘못된 파일 경로입니다.");
        }
        return p;
    }
}
