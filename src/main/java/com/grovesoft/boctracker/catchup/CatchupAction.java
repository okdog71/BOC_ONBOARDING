package com.grovesoft.boctracker.catchup;

import com.grovesoft.boctracker.common.entity.BaseEntity;
import com.grovesoft.boctracker.common.meta.CodeValues;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "catchup_action")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CatchupAction extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false, length = 1000)
    private String action;

    @Column(length = 1000)
    private String purpose;

    @Column(length = 20)
    private String timing;

    @Column(length = 100)
    private String owner;

    @Column(length = 1000)
    private String kpi;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(length = 1000)
    private String note;

    @Builder
    private CatchupAction(String category, String action, String purpose, String timing,
                          String owner, String kpi, String status, String note) {
        this.category = category;
        this.action = action;
        this.purpose = purpose;
        this.timing = timing;
        this.owner = owner;
        this.kpi = kpi;
        this.status = status != null ? status : CodeValues.DEFAULT_STATUS;
        this.note = note;
    }

    /** 전체 필드 교체(PUT). source 는 Mapper 가 Request 로부터 만든 비영속 객체. */
    public void update(CatchupAction source) {
        this.category = source.category;
        this.action = source.action;
        this.purpose = source.purpose;
        this.timing = source.timing;
        this.owner = source.owner;
        this.kpi = source.kpi;
        this.status = source.status;
        this.note = source.note;
    }

    public void changeStatus(String status) {
        this.status = status;
    }
}
