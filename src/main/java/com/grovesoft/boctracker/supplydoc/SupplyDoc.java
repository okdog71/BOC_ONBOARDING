package com.grovesoft.boctracker.supplydoc;

import com.grovesoft.boctracker.common.entity.BaseEntity;
import com.grovesoft.boctracker.common.meta.CodeValues;
import com.grovesoft.boctracker.common.ordering.Sequenced;
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

@Getter
@Entity
@Table(name = "supply_doc")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SupplyDoc extends BaseEntity implements Sequenced {

    public static final String REQUESTED = "요청";
    public static final String RECEIVED = "수령";
    public static final String NOT_REQUESTED = "미요청";
    public static final String NOT_APPLICABLE = "해당없음";

    /** 불변 식별자: {구분 알파벳}.{순번} 예) A.1 */
    @Column(length = 50)
    private String code;

    /** 구분 내 표시 순서(No.) */
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "expected_content", length = 1000)
    private String expectedContent;

    @Column(name = "usage_purpose", length = 1000)
    private String usagePurpose;

    @Column(length = 20)
    private String existence;

    @Column(length = 500)
    private String location;

    @Column(length = 20)
    private String freshness;

    @Column(length = 100)
    private String owner;

    @Column(name = "supply_status", nullable = false, length = 20)
    private String supplyStatus;

    @Column(length = 1000)
    private String note;

    /** 필요일(이 날까지 수령해야 함) — 지연 판단 기준 */
    @Column(name = "target_date")
    private LocalDate targetDate;

    /** 요청일/수령일: 서버가 상태 전이 시 기록 */
    @Column(name = "requested_date")
    private LocalDate requestedDate;

    @Column(name = "received_date")
    private LocalDate receivedDate;

    @Builder
    private SupplyDoc(String code, String category, String name, String expectedContent, String usagePurpose,
                      String existence, String location, String freshness, String owner,
                      String supplyStatus, String note, LocalDate targetDate) {
        this.code = code;
        this.category = category;
        this.name = name;
        this.expectedContent = expectedContent;
        this.usagePurpose = usagePurpose;
        this.existence = existence;
        this.location = location;
        this.freshness = freshness;
        this.owner = owner;
        this.supplyStatus = supplyStatus != null ? supplyStatus : CodeValues.DEFAULT_SUPPLY_STATUS;
        this.note = note;
        this.targetDate = targetDate;
    }

    /** 전체 필드 교체(PUT). 코드·순서·상태 일자는 건드리지 않는다(상태는 applyStatus 규칙). */
    public void update(SupplyDoc source, LocalDate today) {
        this.category = source.category;
        this.name = source.name;
        this.expectedContent = source.expectedContent;
        this.usagePurpose = source.usagePurpose;
        this.existence = source.existence;
        this.location = source.location;
        this.freshness = source.freshness;
        this.owner = source.owner;
        this.note = source.note;
        this.targetDate = source.targetDate;
        applyStatus(source.supplyStatus, today);
    }

    /**
     * 상태 전이 + 일자 자동 기록.
     * 요청: 요청일 기록 · 수령: 수령일(+요청일 없으면 요청일) 기록 · 미요청: 요청/수령일 제거 · 해당없음: 수령일 제거.
     */
    public void applyStatus(String status, LocalDate today) {
        this.supplyStatus = status;
        switch (status) {
            case REQUESTED -> {
                if (requestedDate == null) {
                    requestedDate = today;
                }
                receivedDate = null;
            }
            case RECEIVED -> {
                if (requestedDate == null) {
                    requestedDate = today;
                }
                if (receivedDate == null) {
                    receivedDate = today;
                }
            }
            case NOT_REQUESTED -> {
                requestedDate = null;
                receivedDate = null;
            }
            default -> receivedDate = null;
        }
    }

    /** 더 기다릴 필요가 없는 상태(수령 또는 해당없음) */
    public boolean isReady() {
        return RECEIVED.equals(supplyStatus) || NOT_APPLICABLE.equals(supplyStatus);
    }

    public boolean isOverdue(LocalDate today) {
        return targetDate != null && targetDate.isBefore(today) && !isReady();
    }

    /** 데이터 품질 경고(차단하지 않음) */
    public List<String> warnings() {
        return warnings(0);
    }

    /** @param fileCount 첨부된 파일 수 — 파일이 있으면 위치가 비어 있어도 경고하지 않는다 */
    public List<String> warnings(int fileCount) {
        List<String> w = new ArrayList<>();
        boolean received = RECEIVED.equals(supplyStatus);
        if (received && (existence == null || "없음".equals(existence) || "미확인".equals(existence))) {
            w.add("수령했지만 존재 여부가 '" + (existence == null ? "미입력" : existence) + "'입니다.");
        }
        if (received && fileCount == 0 && (location == null || location.isBlank())) {
            w.add("수령했지만 위치가 비어 있습니다.");
        }
        if (received && (freshness == null || "미확인".equals(freshness) || "불명".equals(freshness))) {
            w.add("최신성 확인이 필요합니다(④ 문서 신뢰도 평가 대상).");
        }
        if ((received || REQUESTED.equals(supplyStatus)) && (owner == null || owner.isBlank())) {
            w.add("담당자가 지정되지 않았습니다.");
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
