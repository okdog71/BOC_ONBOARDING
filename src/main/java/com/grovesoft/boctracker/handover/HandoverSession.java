package com.grovesoft.boctracker.handover;

import com.grovesoft.boctracker.access.AccessItem;
import com.grovesoft.boctracker.common.entity.BaseEntity;
import com.grovesoft.boctracker.common.meta.CodeValues;
import com.grovesoft.boctracker.common.ordering.Sequenced;
import com.grovesoft.boctracker.milestone.Milestone;
import com.grovesoft.boctracker.supplydoc.SupplyDoc;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

@Getter
@Entity
@Table(name = "handover_session")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HandoverSession extends BaseEntity implements Sequenced {

    public static final String STATUS_DONE = "완료";
    public static final String STATUS_SCHEDULED = "예정";
    public static final String STATUS_IN_PROGRESS = "진행중";
    /** 선행 조건 문구: 다른 모든 세션이 끝나야 시작/완료할 수 있는 마무리 세션 표시 */
    public static final String ALL_SESSIONS_MARK = "전 세션 완료";

    /** 불변 식별자: {주차}.{순번} 예) W1.3 */
    @Column(length = 50)
    private String code;

    /** 주차 내 표시 순서(No.) */
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

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

    /** 선행 조건 원문(자유 텍스트). 정규화된 연결은 supplyDocs / accessItems 가 담는다. */
    @Column(length = 1000)
    private String precondition;

    @Column(name = "planned_date")
    private LocalDate plannedDate;

    /** 완료일(서버 관리): 완료 전환 시 기록, 완료에서 벗어나면 제거 */
    @Column(name = "completed_date")
    private LocalDate completedDate;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(length = 1000)
    private String note;

    /** 이 세션이 속한 인수인계 마일스톤(예: HO.1) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "milestone_id")
    private Milestone milestone;

    /** 시작 전에 수령되어 있어야 하는 수급 산출물(②) */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "handover_supply_doc",
            joinColumns = @JoinColumn(name = "handover_id"),
            inverseJoinColumns = @JoinColumn(name = "supply_doc_id"))
    @BatchSize(size = 50)
    private Set<SupplyDoc> supplyDocs = new LinkedHashSet<>();

    /** 시작 전에 승인되어 있어야 하는 접근권한(③) */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "handover_access_item",
            joinColumns = @JoinColumn(name = "handover_id"),
            inverseJoinColumns = @JoinColumn(name = "access_item_id"))
    @BatchSize(size = 50)
    private Set<AccessItem> accessItems = new LinkedHashSet<>();

    @Builder
    private HandoverSession(String code, String week, String category, String domain, String topic, String ktStage,
                            String handoverFrom, String handoverTo, String method, String output,
                            String precondition, LocalDate plannedDate, String status, String note) {
        this.code = code;
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

    /** 전체 필드 교체(PUT). 코드·순서·연결(FK)은 서비스가 따로 처리한다. */
    public void update(HandoverSession source, LocalDate today) {
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
        this.note = source.note;
        applyStatus(source.status, today);
    }

    /** 완료 전환 시 완료일 기록(최초 1회), 이탈 시 제거 */
    public void applyStatus(String newStatus, LocalDate today) {
        this.status = newStatus;
        if (STATUS_DONE.equals(newStatus)) {
            if (completedDate == null) {
                completedDate = today;
            }
        } else {
            completedDate = null;
        }
    }

    public boolean isDone() {
        return STATUS_DONE.equals(status);
    }

    public boolean isOverdue(LocalDate today) {
        return plannedDate != null && plannedDate.isBefore(today) && !isDone();
    }

    public boolean requiresAllSessions() {
        return precondition != null && precondition.contains(ALL_SESSIONS_MARK);
    }

    /** 시작 전 점검: 미준비 수급·접근 + (마무리 세션이면) 미완료 세션. 차단하지 않고 경고로만 쓴다. */
    public List<String> blockers(List<HandoverSession> otherSessions) {
        List<String> out = new ArrayList<>();
        supplyDocs.stream().filter(d -> !d.isReady()).sorted((a, b) -> a.getCode().compareTo(b.getCode()))
                .forEach(d -> out.add("수급 " + d.getCode() + " " + d.getName() + " (" + d.getSupplyStatus() + ")"));
        accessItems.stream().filter(a -> !a.isReady()).sorted((a, b) -> a.getCode().compareTo(b.getCode()))
                .forEach(a -> out.add("접근 " + a.getCode() + " " + a.getTarget() + " (" + a.getStatus() + ")"));
        if (requiresAllSessions()) {
            List<String> pending = otherSessions.stream()
                    .filter(s -> !s.getId().equals(getId()) && !s.isDone()).map(HandoverSession::getCode).toList();
            if (!pending.isEmpty()) {
                out.add("미완료 세션 " + pending.size() + "건: " + String.join(", ", pending));
            }
        }
        return out;
    }

    public List<String> warnings() {
        List<String> w = new ArrayList<>();
        boolean active = STATUS_SCHEDULED.equals(status) || STATUS_IN_PROGRESS.equals(status);
        if (active && plannedDate == null) {
            w.add("예정일이 입력되지 않았습니다.");
        }
        if (active && (handoverFrom == null || handoverFrom.isBlank() || handoverTo == null || handoverTo.isBlank())) {
            w.add("인계자/인수자가 지정되지 않았습니다.");
        }
        if (milestone == null) {
            w.add("연결된 인수인계 마일스톤이 없습니다.");
        }
        return w;
    }

    public void changeMilestone(Milestone milestone) {
        this.milestone = milestone;
    }

    public void replaceSupplyDocs(Set<SupplyDoc> docs) {
        this.supplyDocs.clear();
        this.supplyDocs.addAll(docs);
    }

    public void replaceAccessItems(Set<AccessItem> items) {
        this.accessItems.clear();
        this.accessItems.addAll(items);
    }

    @Override
    public String groupValue() {
        return week;
    }

    @Override
    public void assignCode(String code) {
        this.code = code;
    }

    @Override
    public void changeSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    public void changeGroup(String group) {
        this.week = group;
    }
}
