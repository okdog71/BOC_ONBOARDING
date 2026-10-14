package com.grovesoft.boctracker.issue;

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
@Table(name = "issue_queue")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IssueQueue extends BaseEntity {

    @Column(length = 20)
    private String type;

    @Column(nullable = false, length = 255)
    private String item;

    @Column(length = 1000)
    private String content;

    @Column(length = 1000)
    private String impact;

    @Column(length = 1000)
    private String basis;

    @Column(name = "confirm_with", length = 255)
    private String confirmWith;

    @Column(length = 100)
    private String owner;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(length = 1000)
    private String action;

    @Column(length = 1000)
    private String note;

    @Builder
    private IssueQueue(String type, String item, String content, String impact,
                       String basis, String confirmWith, String owner, String status,
                       LocalDate targetDate, String action, String note) {
        this.type = type;
        this.item = item;
        this.content = content;
        this.impact = impact;
        this.basis = basis;
        this.confirmWith = confirmWith;
        this.owner = owner;
        this.status = status != null ? status : CodeValues.DEFAULT_STATUS;
        this.targetDate = targetDate;
        this.action = action;
        this.note = note;
    }

    /** 전체 필드 교체(PUT). source 는 Mapper 가 Request 로부터 만든 비영속 객체. */
    public void update(IssueQueue source) {
        this.type = source.type;
        this.item = source.item;
        this.content = source.content;
        this.impact = source.impact;
        this.basis = source.basis;
        this.confirmWith = source.confirmWith;
        this.owner = source.owner;
        this.status = source.status;
        this.targetDate = source.targetDate;
        this.action = source.action;
        this.note = source.note;
    }

    public void changeStatus(String status) {
        this.status = status;
    }
}
