package com.grovesoft.boctracker.handover;

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
@Table(name = "handover_session")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HandoverSession extends BaseEntity {

    @Column(nullable = false, length = 10)
    private String week;

    @Column(nullable = false, length = 20)
    private String category;

    @Column(length = 255)
    private String domain;

    @Column(nullable = false, length = 1000)
    private String topic;

    @Column(name = "kt_stage", length = 50)
    private String ktStage;

    @Column(name = "handover_from", length = 255)
    private String handoverFrom;

    @Column(name = "handover_to", length = 255)
    private String handoverTo;

    @Column(length = 255)
    private String method;

    @Column(length = 1000)
    private String output;

    @Column(length = 1000)
    private String precondition;

    @Column(name = "planned_date")
    private LocalDate plannedDate;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(length = 1000)
    private String note;

    @Builder
    private HandoverSession(String week, String category, String domain, String topic, String ktStage,
                            String handoverFrom, String handoverTo, String method, String output,
                            String precondition, LocalDate plannedDate, String status, String note) {
        this.week = week;
        this.category = category;
        this.domain = domain;
        this.topic = topic;
        this.ktStage = ktStage;
        this.handoverFrom = handoverFrom;
        this.handoverTo = handoverTo;
        this.method = method;
        this.output = output;
        this.precondition = precondition;
        this.plannedDate = plannedDate;
        this.status = status != null ? status : CodeValues.DEFAULT_STATUS;
        this.note = note;
    }

    /** 전체 필드 교체(PUT). source 는 Mapper 가 Request 로부터 만든 비영속 객체. */
    public void update(HandoverSession source) {
        this.week = source.week;
        this.category = source.category;
        this.domain = source.domain;
        this.topic = source.topic;
        this.ktStage = source.ktStage;
        this.handoverFrom = source.handoverFrom;
        this.handoverTo = source.handoverTo;
        this.method = source.method;
        this.output = source.output;
        this.precondition = source.precondition;
        this.plannedDate = source.plannedDate;
        this.status = source.status;
        this.note = source.note;
    }

    public void changeStatus(String status) {
        this.status = status;
    }
}
