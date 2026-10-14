package com.grovesoft.boctracker.raci;

import com.grovesoft.boctracker.common.entity.BaseEntity;
import com.grovesoft.boctracker.common.meta.CodeValues;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "raci_assignment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RaciAssignment extends BaseEntity {

    @Column(name = "domain_group", nullable = false, length = 255)
    private String domainGroup;

    @Column(length = 1000)
    private String scope;

    @Column(length = 10)
    private String complexity;

    @Column(length = 10)
    private String scale;

    @Column(length = 20)
    private String generation;

    @Column(length = 255)
    private String responsible;

    @Column(length = 255)
    private String backup;

    @Column(length = 255)
    private String reviewer;

    @Column(name = "menu_count")
    private Integer menuCount;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(length = 1000)
    private String note;

    @Builder
    private RaciAssignment(String domainGroup, String scope, String complexity, String scale,
                           String generation, String responsible, String backup, String reviewer,
                           Integer menuCount, String status, LocalDate targetDate, String note) {
        this.domainGroup = domainGroup;
        this.scope = scope;
        this.complexity = complexity;
        this.scale = scale;
        this.generation = generation;
        this.responsible = responsible;
        this.backup = backup;
        this.reviewer = reviewer;
        this.menuCount = menuCount;
        this.status = status != null ? status : CodeValues.DEFAULT_STATUS;
        this.targetDate = targetDate;
        this.note = note;
    }

    /** 전체 필드 교체(PUT). source 는 Mapper 가 Request 로부터 만든 비영속 객체. */
    public void update(RaciAssignment source) {
        this.domainGroup = source.domainGroup;
        this.scope = source.scope;
        this.complexity = source.complexity;
        this.scale = source.scale;
        this.generation = source.generation;
        this.responsible = source.responsible;
        this.backup = source.backup;
        this.reviewer = source.reviewer;
        this.menuCount = source.menuCount;
        this.status = source.status;
        this.targetDate = source.targetDate;
        this.note = source.note;
    }

    public void changeStatus(String status) {
        this.status = status;
    }
}
