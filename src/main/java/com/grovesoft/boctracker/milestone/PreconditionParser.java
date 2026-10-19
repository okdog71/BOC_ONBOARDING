package com.grovesoft.boctracker.milestone;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Milestone.precondition 문자열에서 선행 마일스톤 코드를 뽑는다.
 * 지원: 쉼표/공백 구분 코드("M0.1,M0.2"), 범위("M0.1~M0.4" = 정렬된 코드 목록에서 두 코드 사이 전부).
 * 코드가 아닌 자유 텍스트("현업 인터뷰·접근 승인")는 무시한다.
 */
public final class PreconditionParser {

    private PreconditionParser() {
    }

    /** @param orderedCodes 전체 마일스톤 코드를 id 순으로 나열한 목록(범위 확장 기준) */
    public static Set<String> parse(String precondition, List<String> orderedCodes) {
        Set<String> result = new LinkedHashSet<>();
        if (precondition == null || precondition.isBlank()) {
            return result;
        }
        for (String token : precondition.split("[,\\s]+")) {
            int tilde = token.indexOf('~');
            if (tilde > 0) {
                int from = orderedCodes.indexOf(token.substring(0, tilde));
                int to = orderedCodes.indexOf(token.substring(tilde + 1));
                if (from >= 0 && to >= from) {
                    result.addAll(orderedCodes.subList(from, to + 1));
                }
            } else if (orderedCodes.contains(token)) {
                result.add(token);
            }
        }
        return result;
    }
}
