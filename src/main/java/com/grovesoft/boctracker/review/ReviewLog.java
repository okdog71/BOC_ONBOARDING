package com.grovesoft.boctracker.review;

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
@Table(name = "review_log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewLog extends BaseEntity {

    @Column(name = "round_no", nullable = false)
    private Integer round;

    @Column(name = "review_date")
    private LocalDate reviewDate;

    @Column(name = "target_domain", length = 255)
    private String targetDomain;

    @Column(length = 255)
    private String presenter;

    @Column(length = 1000)
    private String attendees;

    @Column(name = "coverage_pct")
    private Integer coveragePct;

    @Column(name = "cross_check", length = 1000)
    private String crossCheck;

    @Column(length = 1000)
    private String gaps;

    @Column(name = "dev_unconfirmed", length = 1000)
    private String devUnconfirmed;

    @Column(length = 1000)
    private String action;

    @Column(name = "action_owner", length = 100)
    private String actionOwner;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(nullable = false, length = 20)
    private String status;

    @Builder
    private ReviewLog(Integer round, LocalDate reviewDate, String targetDomain, String presenter,
                      String attendees, Integer coveragePct, String crossCheck, String gaps,
                      String devUnconfirmed, String action, String actionOwner, LocalDate targetDate,
                      String status) {
        this.round = round;
        this.reviewDate = reviewDate;
        this.targetDomain = targetDomain;
        this.presenter = presenter;
        this.attendees = attendees;
        this.coveragePct = coveragePct;
        this.crossCheck = crossCheck;
        this.gaps = gaps;
        this.devUnconfirmed = devUnconfirmed;
        this.action = action;
        this.actionOwner = actionOwner;
        this.targetDate = targetDate;
        this.status = status != null ? status : CodeValues.DEFAULT_STATUS;
    }

    /** 전체 필드 교체(PUT). source 는 Mapper 가 Request 로부터 만든 비영속 객체. */
    public void update(ReviewLog source) {
        this.round = source.round;
        this.reviewDate = source.reviewDate;
        this.targetDomain = source.targetDomain;
        this.presenter = source.presenter;
        this.attendees = source.attendees;
        this.coveragePct = source.coveragePct;
        this.crossCheck = source.crossCheck;
        this.gaps = source.gaps;
        this.devUnconfirmed = source.devUnconfirmed;
        this.action = source.action;
        this.actionOwner = source.actionOwner;
        this.targetDate = source.targetDate;
        this.status = source.status;
    }

    public void changeStatus(String status) {
        this.status = status;
    }
}
