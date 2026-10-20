package com.grovesoft.boctracker.auditlog;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 엔티티 클래스 → 화면에 보일 도메인 이름. 변경 이력의 "무엇을" 분류. */
public final class AuditDomains {

    private static final Map<String, String> LABELS = new LinkedHashMap<>();

    static {
        LABELS.put("Milestone", "마일스톤");
        LABELS.put("MilestoneCheckItem", "마일스톤 체크항목");
        LABELS.put("SupplyDoc", "수급 산출물");
        LABELS.put("SupplyDocFile", "수급 산출물 첨부");
        LABELS.put("AccessItem", "접근권한");
        LABELS.put("DocReliability", "문서 신뢰도");
        LABELS.put("HandoverSession", "인수인계");
        LABELS.put("HandoverQuestion", "인수인계 질의");
        LABELS.put("TechDebt", "기술부채");
        LABELS.put("CatchupAction", "캐치업");
        LABELS.put("MenuInventory", "메뉴 인벤토리");
        LABELS.put("MegaProcess", "메가프로세스");
        LABELS.put("InterfaceLink", "연동 인터페이스");
        LABELS.put("AnalysisCard", "분석 카드");
        LABELS.put("IssueQueue", "이슈 큐");
        LABELS.put("RaciAssignment", "RACI");
        LABELS.put("ReviewLog", "회고 로그");
        LABELS.put("Attachment", "첨부 파일");
        LABELS.put("AppUser", "사용자");
    }

    private AuditDomains() {
    }

    public static String label(String entityType) {
        return LABELS.getOrDefault(entityType, entityType);
    }

    /** 필터 목록용(화면 메뉴 순서). */
    public static List<String> all() {
        return List.copyOf(LABELS.values());
    }
}
