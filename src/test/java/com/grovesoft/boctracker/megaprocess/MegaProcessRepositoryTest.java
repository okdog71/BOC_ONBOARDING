package com.grovesoft.boctracker.megaprocess;

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
class MegaProcessRepositoryTest {

    @Autowired
    MegaProcessRepository repository;
    @Autowired
    TestEntityManager em;

    private MegaProcess a() {
        return MegaProcess.builder()
                .l0("리딤/주문")
                .l1("쿠폰 리딤")
                .l2("코드 검증")
                .relatedMenu("리딤 > 쿠폰 등록")
                .build();
    }

    private MegaProcess b() {
        return MegaProcess.builder()
                .l0("이벤트 운영")
                .l1("쿠폰 리딤")
                .l2("코드 검증")
                .relatedMenu("리딤 > 쿠폰 등록")
                .build();
    }

    @Test
    void 한글_데이터_저장_조회() {
        Long id = repository.saveAndFlush(a()).getId();
        em.clear();
        MegaProcess found = repository.findById(id).orElseThrow();
        assertThat(found.getL0()).isEqualTo("리딤/주문");
        assertThat(found.getL1()).isEqualTo("쿠폰 리딤");
        assertThat(found.getL2()).isEqualTo("코드 검증");
        assertThat(found.getRelatedMenu()).isEqualTo("리딤 > 쿠폰 등록");
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    @Test
    void search_필터() {
        repository.save(a());
        repository.save(b());
        em.flush();

        assertThat(repository.search("리딤/주문", PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);
        assertThat(repository.search(null, PageRequest.of(0, 10)).getTotalElements()).isEqualTo(2);
        assertThat(repository.search("없는값", PageRequest.of(0, 10)).getTotalElements()).isZero();
    }

}
