package com.grovesoft.boctracker.common.meta;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CLAUDE.md 4장 '코드값' 표 — 통제 어휘 허용값의 단일 출처 (엑셀 드롭다운과 동일).
 *
 * <p>{@code @EnumValue(allowed=...)} 는 컴파일 타임 상수만 받으므로 DTO 에는 리터럴로 적고,
 * 일치 여부는 {@code CodeValuesDriftTest} 가 검증한다. 값을 바꾸면 이 클래스와 DTO 를 함께 수정할 것.
 */
public final class CodeValues {

    private CodeValues() {
    }

    public static final String DEFAULT_STATUS = "미착수";
    public static final String DEFAULT_SUPPLY_STATUS = "미요청";
    public static final String DEFAULT_ACCESS_STATUS = "미신청";
    public static final String KT_STAGE_DELIMITER = "·";

    /** Milestone / CatchupAction / MenuInventory.analysisStatus / RaciAssignment / ReviewLog / AnalysisCard 의 status 공용 */
    public static final List<String> MILESTONE_STATUS = List.of("미착수", "진행중", "완료", "보류");
    public static final List<String> HANDOVER_STATUS = List.of("미착수", "예정", "진행중", "완료", "보류");
    /** 복합 표기 허용(예: "설명·병행") */
    public static final List<String> KT_STAGE = List.of("설명", "관찰", "병행", "독립");
    public static final List<String> WEEK = List.of("W1", "W2", "W3", "W4");
    /** HandoverSession.category */
    public static final List<String> CATEGORY = List.of("공통", "KEC", "BOC");
    public static final List<String> CATCHUP_TIMING = List.of("투입전", "투입후", "상시");
    public static final List<String> SUPPLY_STATUS = List.of("미요청", "요청", "수령", "해당없음");
    public static final List<String> EXISTENCE = List.of("있음", "없음", "부분", "미확인");
    /** SupplyDoc.freshness / DocReliability.freshness */
    public static final List<String> FRESHNESS = List.of("현행", "레거시", "혼재", "불명", "미확인");
    public static final List<String> ACCESS_STATUS = List.of("미신청", "신청", "승인", "거부", "보류");
    public static final List<String> GRADE = List.of("A", "B", "C");
    public static final List<String> CODE_FIT = List.of("높음", "보통", "낮음", "미확인");
    /** MenuInventory.priority / RaciAssignment.complexity / RaciAssignment.scale */
    public static final List<String> PRIORITY = List.of("상", "중", "하");
    /** MenuInventory.generation / RaciAssignment.generation */
    public static final List<String> GENERATION = List.of("A", "B", "혼용", "미확인", "-");
    /** MenuInventory.devCheck / InterfaceLink.devCheck */
    public static final List<String> DEV_CHECK = List.of("확인", "dev미노출", "mock", "미확인");
    public static final List<String> ISSUE_TYPE = List.of("이슈", "미확인", "리스크");
    public static final List<String> ISSUE_STATUS = List.of("미착수", "확인중", "진행중", "완료", "보류");

    /** TechDebt.priority — P1 즉시 / P2 단기 / P3 중기 / P4 위생 */
    public static final List<String> TECH_DEBT_PRIORITY = List.of("P1", "P2", "P3", "P4");
    public static final List<String> TECH_DEBT_STATUS = List.of("미착수", "확인중", "진행중", "검증중", "완료", "보류");
    /** TechDebt.system — 코드 접두(KEC-01, BOC-01 …)가 된다 */
    public static final List<String> TECH_DEBT_SYSTEM = List.of("KEC", "BOC", "공통");
    /** HandoverQuestion.priority */
    public static final List<String> QUESTION_PRIORITY = List.of("높음", "중", "낮음");
    public static final List<String> QUESTION_STATUS = List.of("미확인", "진행중", "확인완료", "보류");

    /** GET /api/meta/enums 응답용(그룹명 → 허용값). 선언 순서 유지. */
    public static Map<String, List<String>> all() {
        Map<String, List<String>> m = new LinkedHashMap<>();
        m.put("milestoneStatus", MILESTONE_STATUS);
        m.put("handoverStatus", HANDOVER_STATUS);
        m.put("ktStage", KT_STAGE);
        m.put("week", WEEK);
        m.put("category", CATEGORY);
        m.put("catchupTiming", CATCHUP_TIMING);
        m.put("supplyStatus", SUPPLY_STATUS);
        m.put("existence", EXISTENCE);
        m.put("freshness", FRESHNESS);
        m.put("accessStatus", ACCESS_STATUS);
        m.put("grade", GRADE);
        m.put("codeFit", CODE_FIT);
        m.put("priority", PRIORITY);
        m.put("generation", GENERATION);
        m.put("devCheck", DEV_CHECK);
        m.put("issueType", ISSUE_TYPE);
        m.put("issueStatus", ISSUE_STATUS);
        m.put("techDebtPriority", TECH_DEBT_PRIORITY);
        m.put("techDebtStatus", TECH_DEBT_STATUS);
        m.put("techDebtSystem", TECH_DEBT_SYSTEM);
        m.put("questionPriority", QUESTION_PRIORITY);
        m.put("questionStatus", QUESTION_STATUS);
        return m;
    }
}
