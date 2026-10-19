package com.grovesoft.boctracker.techdebt;

import com.grovesoft.boctracker.common.entity.BaseEntity;
import com.grovesoft.boctracker.common.ordering.Sequenced;
import com.grovesoft.boctracker.common.web.BusinessRuleException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 기술부채 관리대장의 한 항목. 우선순위(P1~P4)가 그룹이고, 코드는 시스템 접두(KEC-01)다. */
@Getter
@Entity
@Table(name = "tech_debt")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TechDebt extends BaseEntity implements Sequenced {

    public static final String STATUS_NOT_STARTED = "미착수";
    public static final String STATUS_CHECKING = "확인중";
    public static final String STATUS_IN_PROGRESS = "진행중";
    public static final String STATUS_VERIFYING = "검증중";
    public static final String STATUS_DONE = "완료";
    public static final String STATUS_ON_HOLD = "보류";

    /** 불변 식별자: {시스템}-{순번} 예) KEC-01 */
    @Column(length = 50)
    private String code;

    /** 우선순위 그룹 내 표시 순서(No.) */
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(nullable = false, length = 10)
    private String priority;

    @Column(name = "system_name", nullable = false, length = 20)
    private String system;

    @Column(length = 100)
    private String area;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 2000)
    private String summary;

    @Column(length = 1000)
    private String impact;

    @Column(length = 500)
    private String basis;

    @Column(length = 2000)
    private String remedy;

    @Column(name = "confirm_with", length = 255)
    private String confirmWith;

    @Column(length = 100)
    private String owner;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "verify_criteria", length = 1000)
    private String verifyCriteria;

    @Column(name = "target_date")
    private LocalDate targetDate;

    /** 완료일(서버 관리): 완료 전환 시 기록, 완료에서 벗어나면 제거 */
    @Column(name = "completed_date")
    private LocalDate completedDate;

    @Column(length = 1000)
    private String note;

    @Builder
    private TechDebt(String code, String priority, String system, String area, String name, String summary,
                     String impact, String basis, String remedy, String confirmWith, String owner, String status,
                     String verifyCriteria, LocalDate targetDate, String note) {
        this.code = code;
        this.priority = priority;
        this.system = system;
        this.area = area;
        this.name = name;
        this.summary = summary;
        this.impact = impact;
        this.basis = basis;
        this.remedy = remedy;
        this.confirmWith = confirmWith;
        this.owner = owner;
        this.status = status != null ? status : STATUS_NOT_STARTED;
        this.verifyCriteria = verifyCriteria;
        this.targetDate = targetDate;
        this.note = note;
    }

    /** 전체 필드 교체(PUT). 코드·순서·완료일은 건드리지 않는다(상태는 applyStatus 규칙). */
    public void update(TechDebt source, LocalDate today) {
        this.priority = source.priority;
        this.system = source.system;
        this.area = source.area;
        this.name = source.name;
        this.summary = source.summary;
        this.impact = source.impact;
        this.basis = source.basis;
        this.remedy = source.remedy;
        this.confirmWith = source.confirmWith;
        this.owner = source.owner;
        this.verifyCriteria = source.verifyCriteria;
        this.targetDate = source.targetDate;
        this.note = source.note;
        applyStatus(source.status, today);
    }

    /**
     * 상태 전이. 완료로 바꾸려면 담당이 지정되어 있어야 한다(담당을 같은 요청에서 바꿨다면 그 값 기준).
     * 완료일은 서버가 기록하고, 완료에서 벗어나면 지운다.
     */
    public void applyStatus(String next, LocalDate today) {
        if (STATUS_DONE.equals(next) && (owner == null || owner.isBlank())) {
            throw new BusinessRuleException("완료 처리하려면 담당을 먼저 지정하세요.");
        }
        this.status = next;
        if (STATUS_DONE.equals(next)) {
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

    /** 목표 시점이 지났는데 끝나지(완료·보류) 않음 */
    public boolean isOverdue(LocalDate today) {
        return targetDate != null && targetDate.isBefore(today) && !isDone() && !STATUS_ON_HOLD.equals(status);
    }

    /** 데이터 품질 경고(차단하지 않음) */
    public List<String> warnings() {
        List<String> w = new ArrayList<>();
        boolean active = STATUS_CHECKING.equals(status) || STATUS_IN_PROGRESS.equals(status)
                || STATUS_VERIFYING.equals(status);
        if (active && (owner == null || owner.isBlank())) {
            w.add("진행 중인데 담당이 지정되지 않았습니다.");
        }
        if (active && ("P1".equals(priority) || "P2".equals(priority)) && targetDate == null) {
            w.add(priority + " 항목은 목표 시점을 정해야 합니다.");
        }
        if (isDone() && (verifyCriteria == null || verifyCriteria.isBlank())) {
            w.add("완료했지만 검증 기준이 비어 있습니다.");
        }
        return w;
    }

    @Override
    public String groupValue() {
        return priority;
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
        this.priority = group;
    }
}
