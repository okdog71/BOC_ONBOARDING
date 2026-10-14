package com.grovesoft.boctracker.docreliability;

import com.grovesoft.boctracker.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "doc_reliability")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocReliability extends BaseEntity {

    @Column(name = "doc_name", nullable = false, length = 255)
    private String docName;

    @Column(length = 500)
    private String source;

    @Column(name = "last_updated", length = 100)
    private String lastUpdated;

    @Column(length = 20)
    private String freshness;

    @Column(name = "code_fit", length = 20)
    private String codeFit;

    @Column(length = 10)
    private String grade;

    @Column(name = "verify_method", length = 1000)
    private String verifyMethod;

    @Column(length = 1000)
    private String gap;

    @Column(length = 100)
    private String owner;

    @Column(length = 1000)
    private String note;

    @Builder
    private DocReliability(String docName, String source, String lastUpdated, String freshness,
                           String codeFit, String grade, String verifyMethod, String gap,
                           String owner, String note) {
        this.docName = docName;
        this.source = source;
        this.lastUpdated = lastUpdated;
        this.freshness = freshness;
        this.codeFit = codeFit;
        this.grade = grade;
        this.verifyMethod = verifyMethod;
        this.gap = gap;
        this.owner = owner;
        this.note = note;
    }

    /** 전체 필드 교체(PUT). source 는 Mapper 가 Request 로부터 만든 비영속 객체. */
    public void update(DocReliability source) {
        this.docName = source.docName;
        this.source = source.source;
        this.lastUpdated = source.lastUpdated;
        this.freshness = source.freshness;
        this.codeFit = source.codeFit;
        this.grade = source.grade;
        this.verifyMethod = source.verifyMethod;
        this.gap = source.gap;
        this.owner = source.owner;
        this.note = source.note;
    }
}
