package com.grovesoft.boctracker.milestone;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 단계명("P1. IA", "HO. 인수인계(KT)")의 접두 키 → 마일스톤 코드 접두(P1, HO). 접두가 없으면 "X". */
public final class PhaseKey {

    public static final String FALLBACK = "X";
    private static final Pattern PREFIX = Pattern.compile("^([A-Za-z][A-Za-z0-9]*)\\s*\\.");

    private PhaseKey() {
    }

    public static String of(String phase) {
        if (phase == null) {
            return FALLBACK;
        }
        Matcher m = PREFIX.matcher(phase.trim());
        return m.find() ? m.group(1).toUpperCase() : FALLBACK;
    }
}
