package com.grovesoft.boctracker.milestone;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PhaseKeyTest {

    @Test
    void 단계명_접두가_키가_된다() {
        assertThat(PhaseKey.of("P0. 수급·준비")).isEqualTo("P0");
        assertThat(PhaseKey.of("HO. 인수인계(KT)")).isEqualTo("HO");
        assertThat(PhaseKey.of("p1 . IA")).isEqualTo("P1");
        assertThat(PhaseKey.of("  P3. 통합")).isEqualTo("P3");
    }

    @Test
    void 접두가_없으면_X() {
        assertThat(PhaseKey.of("운영")).isEqualTo("X");
        assertThat(PhaseKey.of("1. 숫자시작")).isEqualTo("X");
        assertThat(PhaseKey.of(null)).isEqualTo("X");
    }
}
