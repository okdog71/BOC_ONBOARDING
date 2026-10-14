package com.grovesoft.boctracker.interfacelink;

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
class InterfaceLinkRepositoryTest {

    @Autowired
    InterfaceLinkRepository repository;
    @Autowired
    TestEntityManager em;

    private InterfaceLink a() {
        return InterfaceLink.builder()
                .systemName("결제 게이트웨이")
                .direction("BOC→")
                .interfaceName("결제 승인 API")
                .usage("결제 승인 요청")
                .auth("API Key")
                .data("주문번호, 금액")
                .devCheck("확인")
                .build();
    }

    private InterfaceLink b() {
        return InterfaceLink.builder()
                .systemName("결제 게이트웨이")
                .direction("BOC→")
                .interfaceName("결제 승인 API")
                .usage("결제 승인 요청")
                .auth("API Key")
                .data("주문번호, 금액")
                .devCheck("mock")
                .build();
    }

    @Test
    void 한글_데이터_저장_조회() {
        Long id = repository.saveAndFlush(a()).getId();
        em.clear();
        InterfaceLink found = repository.findById(id).orElseThrow();
        assertThat(found.getSystemName()).isEqualTo("결제 게이트웨이");
        assertThat(found.getDirection()).isEqualTo("BOC→");
        assertThat(found.getInterfaceName()).isEqualTo("결제 승인 API");
        assertThat(found.getUsage()).isEqualTo("결제 승인 요청");
        assertThat(found.getAuth()).isEqualTo("API Key");
        assertThat(found.getData()).isEqualTo("주문번호, 금액");
        assertThat(found.getDevCheck()).isEqualTo("확인");
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    @Test
    void search_필터() {
        repository.save(a());
        repository.save(b());
        em.flush();

        assertThat(repository.search("확인", PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);
        assertThat(repository.search(null, PageRequest.of(0, 10)).getTotalElements()).isEqualTo(2);
        assertThat(repository.search("없는값", PageRequest.of(0, 10)).getTotalElements()).isZero();
    }

}
