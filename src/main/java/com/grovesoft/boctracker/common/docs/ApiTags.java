package com.grovesoft.boctracker.common.docs;

import java.util.List;

/** Swagger 태그명(한글) 단일 출처. 컨트롤러의 {@code @Tag} 와 {@code OpenApiConfig} 의 태그 순서가 함께 참조한다. */
public final class ApiTags {

    private ApiTags() {
    }

    public static final String MILESTONE = "마일스톤";
    public static final String HANDOVER = "인수인계(KT)";
    public static final String CATCHUP = "캐치업";
    public static final String SUPPLY_DOC = "수급 산출물";
    public static final String ACCESS = "접근권한";
    public static final String DOC_RELIABILITY = "문서 신뢰도";
    public static final String MENU = "메뉴 인벤토리";
    public static final String MEGA_PROCESS = "메가프로세스";
    public static final String INTERFACE_LINK = "연동 인터페이스";
    public static final String ANALYSIS_CARD = "메뉴 분석 카드";
    public static final String ISSUE = "이슈·미확인 큐";
    public static final String RACI = "RACI 배정";
    public static final String REVIEW = "회고 리뷰 로그";
    public static final String TECH_DEBT = "기술부채";
    public static final String HANDOVER_QUESTION = "인수인계 질의";
    public static final String META = "메타(코드값)";
    public static final String SUMMARY = "요약(대시보드)";

    /** Swagger UI 에 표시할 태그 순서. */
    public static final List<String> ORDER = List.of(MILESTONE, HANDOVER, CATCHUP, SUPPLY_DOC, ACCESS,
            DOC_RELIABILITY, MENU, MEGA_PROCESS, INTERFACE_LINK, ANALYSIS_CARD, ISSUE, RACI, REVIEW, TECH_DEBT, HANDOVER_QUESTION, META, SUMMARY);
}
