package com.grovesoft.boctracker.docreliability;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class DocReliabilityRepositoryTest {

    @Autowired
    DocReliabilityRepository repository;
    @Autowired
    TestEntityManager em;

    private DocReliability a() {
        return DocReliability.builder()
                .docName("IA 정의서")
                .source("Confluence")
                .lastUpdated("2026-09")
                .freshness("현행")
                .codeFit("높음")
                .grade("A")
                .verifyMethod("코드 대조")
                .gap("신규 메뉴 누락")
                .build();
    }

    private DocReliability b() {
        return DocReliability.builder()
                .docName("IA 정의서")
                .source("Confluence")
                .lastUpdated("2026-09")
                .freshness("레거시")
                .codeFit("높음")
                .grade("B")
                .verifyMethod("코드 대조")
                .gap("신규 메뉴 누락")
                .build();
    }

    @Test
    void 한글_데이터_저장_조회() {
        Long id = repository.saveAndFlush(a()).getId();
        em.clear();
        DocReliability found = repository.findById(id).orElseThrow();
        assertThat(found.getDocName()).isEqualTo("IA 정의서");
        assertThat(found.getSource()).isEqualTo("Confluence");
        assertThat(found.getLastUpdated()).isEqualTo("2026-09");
        assertThat(found.getFreshness()).isEqualTo("현행");
        assertThat(found.getCodeFit()).isEqualTo("높음");
        assertThat(found.getGrade()).isEqualTo("A");
        assertThat(found.getVerifyMethod()).isEqualTo("코드 대조");
        assertThat(found.getGap()).isEqualTo("신규 메뉴 누락");
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    @Test
    void search_필터() {
        repository.save(a());
        repository.save(b());
        em.flush();

        assertThat(repository.search("A", null, PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);
        assertThat(repository.search(null, "현행", PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);
        assertThat(repository.search(null, null, PageRequest.of(0, 10)).getTotalElements()).isEqualTo(2);
        assertThat(repository.search("없는값", null, PageRequest.of(0, 10)).getTotalElements()).isZero();
    }

}
