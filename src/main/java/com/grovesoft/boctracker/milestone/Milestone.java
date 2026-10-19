package com.grovesoft.boctracker.milestone;

import com.grovesoft.boctracker.common.entity.BaseEntity;
import com.grovesoft.boctracker.common.meta.CodeValues;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

@Getter
@Entity
@Table(name = "milestone")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Milestone extends BaseEntity {

    public static final String STATUS_DONE = "완료";

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

    /** 목표일(입력). 지연 판단 기준. */
    @Column(name = "target_date")
    private LocalDate targetDate;

    /** 단계 내 표시 순서(No., 1부터 연속). 코드와 무관 — 순서를 바꿔도 코드는 그대로. */
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    /** 완료일(서버 관리): 상태가 완료로 바뀔 때 기록, 완료에서 벗어나면 제거. */
    @Column(name = "completed_date")
    private LocalDate completedDate;

    /** 선행 마일스톤(precondition 문자열의 정규화 표현). */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "milestone_dependency",
            joinColumns = @JoinColumn(name = "milestone_id"),
            inverseJoinColumns = @JoinColumn(name = "predecessor_id"))
    @BatchSize(size = 50)
    private Set<Milestone> predecessors = new LinkedHashSet<>();

    @OneToMany(mappedBy = "milestone", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, id ASC")
    @BatchSize(size = 50)
    private List<MilestoneCheckItem> checkItems = new ArrayList<>();

    /** 체크리스트 저장 입력: id 가 있으면 기존 항목 수정, 없으면 신규. */
    public record CheckItemDraft(Long id, String title, boolean done) {
    }

    @Builder
    private Milestone(String phase, String code, String name, String goal, String checkpoint, String output,
                      String precondition, String owner, String durationWeeks, String status,
                      String doneCriteria, String note, LocalDate targetDate) {
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
        this.targetDate = targetDate;
    }

    /** 전체 필드 교체(PUT). source 는 Mapper 가 Request 로부터 만든 비영속 객체. 상태 전이는 applyStatus 규칙을 따른다. */
    public void update(Milestone source, LocalDate today) {
        this.phase = source.phase;
        this.name = source.name;
        this.goal = source.goal;
        this.checkpoint = source.checkpoint;
        this.output = source.output;
        this.precondition = source.precondition;
        this.owner = source.owner;
        this.durationWeeks = source.durationWeeks;
        this.doneCriteria = source.doneCriteria;
        this.note = source.note;
        this.targetDate = source.targetDate;
        applyStatus(source.status, today);
    }

    /** 상태 변경 + 완료일 규칙(완료 진입 시 최초 1회 기록, 이탈 시 제거). */
    public void applyStatus(String newStatus, LocalDate today) {
        this.status = newStatus;
        if (STATUS_DONE.equals(newStatus)) {
            if (this.completedDate == null) {
                this.completedDate = today;
            }
        } else {
            this.completedDate = null;
        }
    }

    /** 코드는 최초 부여 후 불변. 생성 경로에서만 호출. */
    void assignCode(String code) {
        this.code = code;
    }

    public void changePhase(String phase) {
        this.phase = phase;
    }

    public void changeSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void replacePredecessors(Set<Milestone> newPredecessors) {
        this.predecessors.clear();
        this.predecessors.addAll(newPredecessors);
    }

    /** 선행 중 아직 완료되지 않은 것들. */
    public List<Milestone> incompletePredecessors() {
        return predecessors.stream().filter(p -> !STATUS_DONE.equals(p.getStatus())).toList();
    }

    public boolean isOverdue(LocalDate today) {
        return targetDate != null && targetDate.isBefore(today) && !STATUS_DONE.equals(status);
    }

    /** 체크리스트 전체 교체: id 일치 항목은 수정, 없는 id 는 삭제, id 없는 항목은 추가. 입력 순서가 곧 정렬 순서. */
    public void replaceCheckItems(List<CheckItemDraft> drafts) {
        Map<Long, MilestoneCheckItem> existing = new HashMap<>();
        checkItems.forEach(i -> existing.put(i.getId(), i));
        List<MilestoneCheckItem> next = new ArrayList<>();
        for (int idx = 0; idx < drafts.size(); idx++) {
            CheckItemDraft d = drafts.get(idx);
            MilestoneCheckItem item = d.id() == null ? null : existing.get(d.id());
            if (item == null) {
                item = new MilestoneCheckItem(this, d.title(), d.done(), idx);
            } else {
                item.change(d.title(), d.done(), idx);
            }
            next.add(item);
        }
        checkItems.clear();
        checkItems.addAll(next);
    }

    /** @return 항목이 없으면 false */
    public boolean toggleCheckItem(Long itemId, boolean done) {
        return checkItems.stream().filter(i -> i.getId().equals(itemId)).findFirst()
                .map(i -> {
                    i.markDone(done);
                    return true;
                }).orElse(false);
    }
}
