package com.grovesoft.boctracker.handoverquestion;

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

/** 인수인계 시 확인할 질문 한 건. 우선순위(높음/중/낮음)가 그룹이고, 코드는 Q-01 형식이다. */
@Getter
@Entity
@Table(name = "handover_question")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HandoverQuestion extends BaseEntity implements Sequenced {

    public static final String STATUS_UNCONFIRMED = "미확인";
    public static final String STATUS_IN_PROGRESS = "진행중";
    public static final String STATUS_CONFIRMED = "확인완료";
    public static final String STATUS_ON_HOLD = "보류";

    /** 불변 식별자: Q-{순번} 예) Q-01 */
    @Column(length = 50)
    private String code;

    /** 우선순위 그룹 내 표시 순서(No.) */
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(nullable = false, length = 10)
    private String priority;

    /** 구분(추정 I/F, 확인필요 #n, 명칭 매핑 …) */
    @Column(length = 100)
    private String kind;

    /** 관련 I/F·노드 */
    @Column(name = "related_node", length = 500)
    private String relatedNode;

    @Column(nullable = false, length = 1000)
    private String question;

    @Column(length = 1000)
    private String basis;

    @Column(name = "ask_to", length = 255)
    private String askTo;

    /** 확인 전 가설(참고용). 사실로 취급하지 않는다. */
    @Column(length = 1000)
    private String hypothesis;

    /** 구성도 반영 방법 */
    @Column(name = "reflect_plan", length = 1000)
    private String reflectPlan;

    @Column(nullable = false, length = 20)
    private String status;

    /** 실제 확인한 답변 */
    @Column(length = 2000)
    private String answer;

    @Column(name = "confirmed_by", length = 100)
    private String confirmedBy;

    @Column(name = "confirmed_date")
    private LocalDate confirmedDate;

    @Column(length = 1000)
    private String note;

    @Builder
    private HandoverQuestion(String code, String priority, String kind, String relatedNode, String question,
                             String basis, String askTo, String hypothesis, String reflectPlan, String status,
                             String answer, String confirmedBy, LocalDate confirmedDate, String note) {
        this.code = code;
        this.priority = priority;
        this.kind = kind;
        this.relatedNode = relatedNode;
        this.question = question;
        this.basis = basis;
        this.askTo = askTo;
        this.hypothesis = hypothesis;
        this.reflectPlan = reflectPlan;
        this.status = status != null ? status : STATUS_UNCONFIRMED;
        this.answer = answer;
        this.confirmedBy = confirmedBy;
        this.confirmedDate = confirmedDate;
        this.note = note;
    }

    /** 전체 필드 교체(PUT). 코드·순서는 건드리지 않는다(상태·확인일은 applyStatus 규칙). */
    public void update(HandoverQuestion source, LocalDate today) {
        this.priority = source.priority;
        this.kind = source.kind;
        this.relatedNode = source.relatedNode;
        this.question = source.question;
        this.basis = source.basis;
        this.askTo = source.askTo;
        this.hypothesis = source.hypothesis;
        this.reflectPlan = source.reflectPlan;
        this.answer = source.answer;
        this.confirmedBy = source.confirmedBy;
        this.note = source.note;
        if (source.confirmedDate != null) {
            this.confirmedDate = source.confirmedDate;
        }
        applyStatus(source.status, today);
    }

    /**
     * 상태 전이. 확인완료로 바꾸려면 확인 답변과 확인자가 있어야 한다.
     * 확인일은 확인완료일 때만 갖고(없으면 오늘), 다른 상태로 되돌리면 지운다.
     */
    public void applyStatus(String next, LocalDate today) {
        if (STATUS_CONFIRMED.equals(next)) {
            if (answer == null || answer.isBlank()) {
                throw new BusinessRuleException("확인완료로 바꾸려면 '확인 답변'을 먼저 입력하세요.");
            }
            if (confirmedBy == null || confirmedBy.isBlank()) {
                throw new BusinessRuleException("확인완료로 바꾸려면 '확인자'를 먼저 입력하세요.");
            }
            if (confirmedDate == null) {
                confirmedDate = today;
            }
        } else {
            confirmedDate = null;
        }
        this.status = next;
    }

    public boolean isConfirmed() {
        return STATUS_CONFIRMED.equals(status);
    }

    /** 데이터 품질 경고(차단하지 않음) */
    public List<String> warnings() {
        List<String> w = new ArrayList<>();
        boolean hasAnswer = answer != null && !answer.isBlank();
        if (hasAnswer && STATUS_UNCONFIRMED.equals(status)) {
            w.add("확인 답변이 입력됐지만 상태가 '미확인'입니다.");
        }
        if (STATUS_IN_PROGRESS.equals(status) && (askTo == null || askTo.isBlank())) {
            w.add("진행 중인데 확인 대상이 비어 있습니다.");
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
