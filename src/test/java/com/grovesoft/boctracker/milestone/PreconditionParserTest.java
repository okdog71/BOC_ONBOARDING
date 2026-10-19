package com.grovesoft.boctracker.milestone;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class PreconditionParserTest {

    private final List<String> codes = List.of("M0.1", "M0.2", "M0.3", "M0.4", "M0.5");

    @Test
    void 쉼표_목록() {
        assertThat(PreconditionParser.parse("M0.1,M0.2", codes)).containsExactly("M0.1", "M0.2");
    }

    @Test
    void 범위_확장() {
        assertThat(PreconditionParser.parse("M0.1~M0.4", codes)).containsExactly("M0.1", "M0.2", "M0.3", "M0.4");
    }

    @Test
    void 자유텍스트와_미존재_코드는_무시() {
        assertThat(PreconditionParser.parse("현업 인터뷰·접근 승인", codes)).isEmpty();
        assertThat(PreconditionParser.parse("M9.9", codes)).isEmpty();
        assertThat(PreconditionParser.parse(null, codes)).isEmpty();
    }
}
