package com.grovesoft.boctracker.common.attachment;

import com.grovesoft.boctracker.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** 항목(기술부채·인수인계 질의 등)에 첨부된 파일의 메타데이터. 본문은 디스크에 있다. */
@Getter
@Entity
@Table(name = "attachment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Attachment extends BaseEntity {

    /** MySQL 네이티브 ENUM 이 아닌 VARCHAR 로 저장(스키마 검증과 일치) */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "owner_type", nullable = false, length = 30)
    private AttachmentOwner ownerType;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column(name = "stored_name", nullable = false, length = 100)
    private String storedName;

    @Column(name = "content_type", length = 150)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(nullable = false, length = 64)
    private String sha256;

    public Attachment(AttachmentOwner ownerType, Long ownerId, String originalName, String storedName,
                      String contentType, long sizeBytes, String sha256) {
        this.ownerType = ownerType;
        this.ownerId = ownerId;
        this.originalName = originalName;
        this.storedName = storedName;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.sha256 = sha256;
    }
}
