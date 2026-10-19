package com.grovesoft.boctracker.access;

import com.grovesoft.boctracker.common.entity.BaseEntity;
import com.grovesoft.boctracker.common.meta.CodeValues;
import com.grovesoft.boctracker.common.ordering.Sequenced;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "access_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccessItem extends BaseEntity implements Sequenced {

    public static final String NOT_REQUESTED = "미신청";
    public static final String REQUESTED = "신청";
    public static final String APPROVED = "승인";
    public static final String REJECTED = "거부";
    public static final String ON_HOLD = "보류";
    /** 다른 접근의 전제가 되는 항목(예: VPN/VDI/SSO)을 가리키는 용도 문구 */
    public static final String PREREQUISITE_MARK = "접근 전제";

    /** 불변 식별자: AC.{순번} */
    @Column(length = 50)
    private String code;

    /** 구분 내 표시 순서(No.) */
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

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

    /** 필요일(투입 전까지 승인되어야 함) — 지연 판단 기준 */
    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(length = 100)
    private String owner;

    @Column(length = 1000)
    private String note;

    @Builder
    private AccessItem(String code, String category, String target, String usage, String envScope,
                       String procedure, String status, LocalDate requestedDate, LocalDate approvedDate,
                       LocalDate targetDate, String owner, String note) {
        this.code = code;
        this.category = category;
        this.target = target;
        this.usage = usage;
        this.envScope = envScope;
        this.procedure = procedure;
        this.status = status != null ? status : CodeValues.DEFAULT_ACCESS_STATUS;
        this.requestedDate = requestedDate;
        this.approvedDate = approvedDate;
        this.targetDate = targetDate;
        this.owner = owner;
        this.note = note;
    }

    /** 전체 필드 교체(PUT). 코드·순서는 건드리지 않는다. 일자는 요청 값을 받은 뒤 상태 규칙으로 보정한다. */
    public void update(AccessItem source, LocalDate today) {
        this.category = source.category;
        this.target = source.target;
        this.usage = source.usage;
        this.envScope = source.envScope;
        this.procedure = source.procedure;
        this.requestedDate = source.requestedDate;
        this.approvedDate = source.approvedDate;
        this.targetDate = source.targetDate;
        this.owner = source.owner;
        this.note = source.note;
        applyStatus(source.status, today);
    }

    /**
     * 상태 전이 + 일자 보정.
     * 신청: 신청일 기록, 승인일 제거 · 승인: 신청일(없으면)·승인일 기록 · 거부/보류: 승인일 제거 · 미신청: 신청/승인일 제거.
     */
    public void applyStatus(String newStatus, LocalDate today) {
        this.status = newStatus;
        switch (newStatus) {
            case REQUESTED -> {
                if (requestedDate == null) {
                    requestedDate = today;
                }
                approvedDate = null;
            }
            case APPROVED -> {
                if (requestedDate == null) {
                    requestedDate = today;
                }
                if (approvedDate == null) {
                    approvedDate = today;
                }
            }
            case NOT_REQUESTED -> {
                requestedDate = null;
                approvedDate = null;
            }
            default -> approvedDate = null;
        }
    }

    public boolean isReady() {
        return APPROVED.equals(status);
    }

    public boolean isPrerequisite() {
        return usage != null && usage.contains(PREREQUISITE_MARK);
    }

    public boolean isOverdue(LocalDate today) {
        return targetDate != null && targetDate.isBefore(today) && !isReady();
    }

    /** 신청→승인 소요일(둘 다 있을 때) */
    public Long leadTimeDays() {
        return requestedDate != null && approvedDate != null
                ? ChronoUnit.DAYS.between(requestedDate, approvedDate) : null;
    }

    /** 데이터 품질/선행 경고(차단하지 않음). @param prerequisitesMissing 접근 전제 항목이 아직 승인되지 않았는가 */
    public List<String> warnings(boolean prerequisitesMissing) {
        List<String> w = new ArrayList<>();
        boolean active = REQUESTED.equals(status) || APPROVED.equals(status);
        if (active && (owner == null || owner.isBlank())) {
            w.add("담당자가 지정되지 않았습니다.");
        }
        if (REJECTED.equals(status) && (note == null || note.isBlank())) {
            w.add("거부 사유를 비고에 남겨 주세요.");
        }
        if (active && !isPrerequisite() && prerequisitesMissing) {
            w.add("접근 전제(VPN/VDI/SSO 등)가 아직 승인되지 않았습니다.");
        }
        return w;
    }

    @Override
    public String groupValue() {
        return category;
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
        this.category = group;
    }
}
