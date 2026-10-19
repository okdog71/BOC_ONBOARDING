package com.grovesoft.boctracker.common.ordering;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 그룹명에서 코드 접두 키를 뽑는 규칙 모음. */
public final class CodeKeys {

    private static final Pattern LEADING_LETTER = Pattern.compile("^([A-Za-z])\\s*\\.");

    private CodeKeys() {
    }

    /** "A. 기획·화면" → "A". 접두가 없으면 fallback. */
    public static String leadingLetter(String group, String fallback) {
        if (group == null) {
            return fallback;
        }
        Matcher m = LEADING_LETTER.matcher(group.trim());
        return m.find() ? m.group(1).toUpperCase() : fallback;
    }
}
