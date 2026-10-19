package com.grovesoft.boctracker.milestone;

import com.grovesoft.boctracker.common.entity.BaseEntity;
import com.grovesoft.boctracker.common.meta.CodeValues;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Builder;
import org.hibernate.annotations.BatchSize;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@Getter
@Entity
@Table(name = "milestone")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Milestone extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String phase;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 1000)
    private String goal;

    @Column(length = 1000)
    private String checkpoint;

    @Column(length = 1000)
    private String output;

    @Column(length = 1000)
    private String precondition;

    @Column(length = 100)
    private String owner;

    @Column(name = "duration_weeks", length = 50)
    private String durationWeeks;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "done_criteria", length = 1000)
    private String doneCriteria;

    @Column(length = 1000)
    private String note;

    /** 선행 마일스톤(precondition 문자열의 정규화 표현). */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "milestone_dependency",
            joinColumns = @JoinColumn(name = "milestone_id"),
            inverseJoinColumns = @JoinColumn(name = "predecessor_id"))
    @BatchSize(size = 50)
    private Set<Milestone> predecessors = new LinkedHashSet<>();

    @Builder
    private Milestone(String phase, String code, String name, String goal, String checkpoint, String output,
                      String precondition, String owner, String durationWeeks, String status,
                      String doneCriteria, String note) {
        this.phase = phase;
        this.code = code;
        this.name = name;
        this.goal = goal;
        this.checkpoint = checkpoint;
        this.output = output;
        this.precondition = precondition;
        this.owner = owner;
        this.durationWeeks = durationWeeks;
        this.status = status != null ? status : CodeValues.DEFAULT_STATUS;
        this.doneCriteria = doneCriteria;
        this.note = note;
    }

    /** 전체 필드 교체(PUT). source 는 Mapper 가 Request 로부터 만든 비영속 객체. */
    public void update(Milestone source) {
        this.phase = source.phase;
        this.code = source.code;
        this.name = source.name;
        this.goal = source.goal;
        this.checkpoint = source.checkpoint;
        this.output = source.output;
        this.precondition = source.precondition;
        this.owner = source.owner;
        this.durationWeeks = source.durationWeeks;
        this.status = source.status;
        this.doneCriteria = source.doneCriteria;
        this.note = source.note;
    }

    public void changeStatus(String status) {
        this.status = status;
    }

    public void replacePredecessors(Set<Milestone> newPredecessors) {
        this.predecessors.clear();
        this.predecessors.addAll(newPredecessors);
    }
}
