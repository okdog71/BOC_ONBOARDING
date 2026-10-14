package com.grovesoft.boctracker.access;

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
@Table(name = "access_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccessItem extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false, length = 255)
    private String target;

    @Column(name = "usage_desc", length = 1000)
    private String usage;

    @Column(name = "env_scope", length = 255)
    private String envScope;

    @Column(name = "procedure_desc", length = 1000)
    private String procedure;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "requested_date")
    private LocalDate requestedDate;

    @Column(name = "approved_date")
    private LocalDate approvedDate;

    @Column(length = 100)
    private String owner;

    @Column(length = 1000)
    private String note;

    @Builder
    private AccessItem(String category, String target, String usage, String envScope,
                       String procedure, String status, LocalDate requestedDate, LocalDate approvedDate,
                       String owner, String note) {
        this.category = category;
        this.target = target;
        this.usage = usage;
        this.envScope = envScope;
        this.procedure = procedure;
        this.status = status != null ? status : CodeValues.DEFAULT_ACCESS_STATUS;
        this.requestedDate = requestedDate;
        this.approvedDate = approvedDate;
        this.owner = owner;
        this.note = note;
    }

    /** 전체 필드 교체(PUT). source 는 Mapper 가 Request 로부터 만든 비영속 객체. */
    public void update(AccessItem source) {
        this.category = source.category;
        this.target = source.target;
        this.usage = source.usage;
        this.envScope = source.envScope;
        this.procedure = source.procedure;
        this.status = source.status;
        this.requestedDate = source.requestedDate;
        this.approvedDate = source.approvedDate;
        this.owner = source.owner;
        this.note = source.note;
    }

    public void changeStatus(String status) {
        this.status = status;
    }
}
