package com.grovesoft.boctracker.analysiscard;

import static org.assertj.core.api.Assertions.assertThat;

import com.grovesoft.boctracker.common.entity.StatusCount;
import java.util.Map;
import java.time.LocalDate;
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
class AnalysisCardRepositoryTest {

    @Autowired
    AnalysisCardRepository repository;
    @Autowired
    TestEntityManager em;

    private AnalysisCard a() {
        return AnalysisCard.builder()
                .menuName("쿠폰 등록")
                .path("/redeem/coupon")
                .domain("redeem")
                .backendService("redeem-api")
                .generation("A")
                .summary("쿠폰 코드를 등록한다")
                .scenario("운영자가 코드를 입력하고 검증 후 저장")
                .input("쿠폰 코드")
                .output("등록 결과")
                .backendMapping("POST /coupons")
                .dataStore("MySQL")
                .externalLink("결제 게이트웨이")
                .permission("운영자")
                .relatedProcess("쿠폰 리딤")
                .knownIssue("KEC-01")
                .devCheck("확인")
                .risk("중복 등록")
                .owner("BOC 리드")
                .analyzedDate(LocalDate.of(2026, 11, 5))
                .status("미착수")
                .build();
    }

    private AnalysisCard b() {
        return AnalysisCard.builder()
                .menuName("쿠폰 등록")
                .path("/redeem/coupon")
                .domain("order")
                .backendService("redeem-api")
                .generation("A")
                .summary("쿠폰 코드를 등록한다")
                .scenario("운영자가 코드를 입력하고 검증 후 저장")
                .input("쿠폰 코드")
                .output("등록 결과")
                .backendMapping("POST /coupons")
                .dataStore("MySQL")
                .externalLink("결제 게이트웨이")
                .permission("운영자")
                .relatedProcess("쿠폰 리딤")
                .knownIssue("KEC-01")
                .devCheck("확인")
                .risk("중복 등록")
                .owner("BOC 리드")
                .analyzedDate(LocalDate.of(2026, 11, 5))
                .status("완료")
                .build();
    }

    @Test
    void 한글_데이터_저장_조회() {
        Long id = repository.saveAndFlush(a()).getId();
        em.clear();
        AnalysisCard found = repository.findById(id).orElseThrow();
        assertThat(found.getMenuName()).isEqualTo("쿠폰 등록");
        assertThat(found.getPath()).isEqualTo("/redeem/coupon");
        assertThat(found.getDomain()).isEqualTo("redeem");
        assertThat(found.getBackendService()).isEqualTo("redeem-api");
        assertThat(found.getGeneration()).isEqualTo("A");
        assertThat(found.getSummary()).isEqualTo("쿠폰 코드를 등록한다");
        assertThat(found.getScenario()).isEqualTo("운영자가 코드를 입력하고 검증 후 저장");
        assertThat(found.getInput()).isEqualTo("쿠폰 코드");
        assertThat(found.getOutput()).isEqualTo("등록 결과");
        assertThat(found.getBackendMapping()).isEqualTo("POST /coupons");
        assertThat(found.getDataStore()).isEqualTo("MySQL");
        assertThat(found.getExternalLink()).isEqualTo("결제 게이트웨이");
        assertThat(found.getPermission()).isEqualTo("운영자");
        assertThat(found.getRelatedProcess()).isEqualTo("쿠폰 리딤");
        assertThat(found.getKnownIssue()).isEqualTo("KEC-01");
        assertThat(found.getDevCheck()).isEqualTo("확인");
        assertThat(found.getRisk()).isEqualTo("중복 등록");
        assertThat(found.getOwner()).isEqualTo("BOC 리드");
        assertThat(found.getAnalyzedDate()).isEqualTo(LocalDate.of(2026, 11, 5));
        assertThat(found.getStatus()).isEqualTo("미착수");
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    @Test
    void search_필터() {
        repository.save(a());
        repository.save(b());
        em.flush();

        assertThat(repository.search("미착수", null, PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);
        assertThat(repository.search(null, "redeem", PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);
        assertThat(repository.search(null, null, PageRequest.of(0, 10)).getTotalElements()).isEqualTo(2);
        assertThat(repository.search("없는값", null, PageRequest.of(0, 10)).getTotalElements()).isZero();
    }

    @Test
    void 상태_미지정시_기본값과_countBy_그룹집계() {
        AnalysisCard noStatus = AnalysisCard.builder()
                .menuName("쿠폰 등록")
                .path("/redeem/coupon")
                .domain("redeem")
                .backendService("redeem-api")
                .generation("A")
                .summary("쿠폰 코드를 등록한다")
                .scenario("운영자가 코드를 입력하고 검증 후 저장")
                .input("쿠폰 코드")
                .output("등록 결과")
                .backendMapping("POST /coupons")
                .dataStore("MySQL")
                .externalLink("결제 게이트웨이")
                .permission("운영자")
                .relatedProcess("쿠폰 리딤")
                .knownIssue("KEC-01")
                .devCheck("확인")
                .risk("중복 등록")
                .owner("BOC 리드")
                .analyzedDate(LocalDate.of(2026, 11, 5))
                .build();
        assertThat(noStatus.getStatus()).isEqualTo("미착수");
        repository.save(noStatus);
        repository.save(b());
        repository.save(b());
        em.flush();

        assertThat(repository.countByStatus("완료")).isEqualTo(2);
        Map<String, Long> grouped = repository.countGroupByStatus().stream()
                .collect(Collectors.toMap(StatusCount::getStatus, StatusCount::getCount));
        assertThat(grouped).containsEntry("미착수", 1L).containsEntry("완료", 2L);
    }

}
