package com.grovesoft.boctracker.menu;

import static org.assertj.core.api.Assertions.assertThat;

import com.grovesoft.boctracker.common.entity.StatusCount;
import java.util.Map;
import java.util.stream.Collectors;
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
class MenuInventoryRepositoryTest {

    @Autowired
    MenuInventoryRepository repository;
    @Autowired
    TestEntityManager em;

    private MenuInventory a() {
        return MenuInventory.builder()
                .gnb("운영")
                .menu("쿠폰 관리")
                .submenu("쿠폰 등록")
                .path("/redeem/coupon")
                .domain("redeem")
                .backendService("redeem-api")
                .dataStore("MySQL")
                .externalLink("결제 게이트웨이")
                .permission("운영자")
                .generation("A")
                .priority("상")
                .knownIssue("KEC-01")
                .devCheck("확인")
                .owner("BOC 리드")
                .analysisStatus("미착수")
                .build();
    }

    private MenuInventory b() {
        return MenuInventory.builder()
                .gnb("운영")
                .menu("쿠폰 관리")
                .submenu("쿠폰 등록")
                .path("/redeem/coupon")
                .domain("order")
                .backendService("redeem-api")
                .dataStore("MySQL")
                .externalLink("결제 게이트웨이")
                .permission("운영자")
                .generation("A")
                .priority("하")
                .knownIssue("KEC-01")
                .devCheck("mock")
                .owner("BOC 리드")
                .analysisStatus("완료")
                .build();
    }

    @Test
    void 한글_데이터_저장_조회() {
        Long id = repository.saveAndFlush(a()).getId();
        em.clear();
        MenuInventory found = repository.findById(id).orElseThrow();
        assertThat(found.getGnb()).isEqualTo("운영");
        assertThat(found.getMenu()).isEqualTo("쿠폰 관리");
        assertThat(found.getSubmenu()).isEqualTo("쿠폰 등록");
        assertThat(found.getPath()).isEqualTo("/redeem/coupon");
        assertThat(found.getDomain()).isEqualTo("redeem");
        assertThat(found.getBackendService()).isEqualTo("redeem-api");
        assertThat(found.getDataStore()).isEqualTo("MySQL");
        assertThat(found.getExternalLink()).isEqualTo("결제 게이트웨이");
        assertThat(found.getPermission()).isEqualTo("운영자");
        assertThat(found.getGeneration()).isEqualTo("A");
        assertThat(found.getPriority()).isEqualTo("상");
        assertThat(found.getKnownIssue()).isEqualTo("KEC-01");
        assertThat(found.getDevCheck()).isEqualTo("확인");
        assertThat(found.getOwner()).isEqualTo("BOC 리드");
        assertThat(found.getAnalysisStatus()).isEqualTo("미착수");
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    @Test
    void search_필터() {
        repository.save(a());
        repository.save(b());
        em.flush();

        assertThat(repository.search("미착수", null, null, null, PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);
        assertThat(repository.search(null, "상", null, null, PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);
        assertThat(repository.search(null, null, "redeem", null, PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);
        assertThat(repository.search(null, null, null, "확인", PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);
        assertThat(repository.search(null, null, null, null, PageRequest.of(0, 10)).getTotalElements()).isEqualTo(2);
        assertThat(repository.search("없는값", null, null, null, PageRequest.of(0, 10)).getTotalElements()).isZero();
    }

    @Test
    void 상태_미지정시_기본값과_countBy_그룹집계() {
        MenuInventory noStatus = MenuInventory.builder()
                .gnb("운영")
                .menu("쿠폰 관리")
                .submenu("쿠폰 등록")
                .path("/redeem/coupon")
                .domain("redeem")
                .backendService("redeem-api")
                .dataStore("MySQL")
                .externalLink("결제 게이트웨이")
                .permission("운영자")
                .generation("A")
                .priority("상")
                .knownIssue("KEC-01")
                .devCheck("확인")
                .owner("BOC 리드")
                .build();
        assertThat(noStatus.getAnalysisStatus()).isEqualTo("미착수");
        repository.save(noStatus);
        repository.save(b());
        repository.save(b());
        em.flush();

        assertThat(repository.countByAnalysisStatus("완료")).isEqualTo(2);
        Map<String, Long> grouped = repository.countGroupByStatus().stream()
                .collect(Collectors.toMap(StatusCount::getStatus, StatusCount::getCount));
        assertThat(grouped).containsEntry("미착수", 1L).containsEntry("완료", 2L);
    }

}
