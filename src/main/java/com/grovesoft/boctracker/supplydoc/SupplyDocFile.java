package com.grovesoft.boctracker.supplydoc;

import com.grovesoft.boctracker.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 수급 산출물에 첨부된 실제 파일의 메타데이터(본문은 디스크). */
@Getter
@Entity
@Table(name = "supply_doc_file")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SupplyDocFile extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supply_doc_id", nullable = false)
    private SupplyDoc supplyDoc;

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

    public SupplyDocFile(SupplyDoc supplyDoc, String originalName, String storedName, String contentType,
                         long sizeBytes, String sha256) {
        this.supplyDoc = supplyDoc;
        this.originalName = originalName;
        this.storedName = storedName;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.sha256 = sha256;
    }
}
