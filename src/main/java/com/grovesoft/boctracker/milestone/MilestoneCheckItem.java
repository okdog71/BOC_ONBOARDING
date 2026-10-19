package com.grovesoft.boctracker.milestone;

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

/** 마일스톤 체크리스트 항목. 진척도 = 완료 항목 / 전체 항목. */
@Getter
@Entity
@Table(name = "milestone_check_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MilestoneCheckItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "milestone_id", nullable = false)
    private Milestone milestone;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false)
    private boolean done;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    MilestoneCheckItem(Milestone milestone, String title, boolean done, int sortOrder) {
        this.milestone = milestone;
        this.title = title;
        this.done = done;
        this.sortOrder = sortOrder;
    }

    void change(String title, boolean done, int sortOrder) {
        this.title = title;
        this.done = done;
        this.sortOrder = sortOrder;
    }

    void markDone(boolean done) {
        this.done = done;
    }
}
