package com.grovesoft.boctracker.supplydoc;

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
class SupplyDocRepositoryTest {

    @Autowired
    SupplyDocRepository repository;
    @Autowired
    TestEntityManager em;

    private SupplyDoc a() {
        return SupplyDoc.builder()
                .category("A. 기획·화면")
                .name("화면 스토리보드")
                .expectedContent("메뉴별 화면 정의")
                .usagePurpose("메뉴 분석 기준")
                .existence("있음")
                .location("Confluence/BOC/스토리보드")
                .freshness("혼재")
                .owner("기획 담당")
                .supplyStatus("미요청")
                .build();
    }

    private SupplyDoc b() {
        return SupplyDoc.builder()
                .category("B. 설계·기술")
                .name("화면 스토리보드")
                .expectedContent("메뉴별 화면 정의")
                .usagePurpose("메뉴 분석 기준")
                .existence("있음")
                .location("Confluence/BOC/스토리보드")
                .freshness("혼재")
                .owner("기획 담당")
                .supplyStatus("수령")
                .build();
    }

    @Test
    void 한글_데이터_저장_조회() {
        Long id = repository.saveAndFlush(a()).getId();
        em.clear();
        SupplyDoc found = repository.findById(id).orElseThrow();
        assertThat(found.getCategory()).isEqualTo("A. 기획·화면");
        assertThat(found.getName()).isEqualTo("화면 스토리보드");
        assertThat(found.getExpectedContent()).isEqualTo("메뉴별 화면 정의");
        assertThat(found.getUsagePurpose()).isEqualTo("메뉴 분석 기준");
        assertThat(found.getExistence()).isEqualTo("있음");
        assertThat(found.getLocation()).isEqualTo("Confluence/BOC/스토리보드");
        assertThat(found.getFreshness()).isEqualTo("혼재");
        assertThat(found.getOwner()).isEqualTo("기획 담당");
        assertThat(found.getSupplyStatus()).isEqualTo("미요청");
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    @Test
    void search_필터() {
        repository.save(a());
        repository.save(b());
        em.flush();

        assertThat(repository.search("미요청", null, PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);
        assertThat(repository.search(null, "A. 기획·화면", PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);
        assertThat(repository.search(null, null, PageRequest.of(0, 10)).getTotalElements()).isEqualTo(2);
        assertThat(repository.search("없는값", null, PageRequest.of(0, 10)).getTotalElements()).isZero();
    }

    @Test
    void 상태_미지정시_기본값과_countBy_그룹집계() {
        SupplyDoc noStatus = SupplyDoc.builder()
                .category("A. 기획·화면")
                .name("화면 스토리보드")
                .expectedContent("메뉴별 화면 정의")
                .usagePurpose("메뉴 분석 기준")
                .existence("있음")
                .location("Confluence/BOC/스토리보드")
                .freshness("혼재")
                .owner("기획 담당")
                .build();
        assertThat(noStatus.getSupplyStatus()).isEqualTo("미요청");
        repository.save(noStatus);
        repository.save(b());
        repository.save(b());
        em.flush();

        assertThat(repository.countBySupplyStatus("수령")).isEqualTo(2);
        Map<String, Long> grouped = repository.countGroupByStatus().stream()
                .collect(Collectors.toMap(StatusCount::getStatus, StatusCount::getCount));
        assertThat(grouped).containsEntry("미요청", 1L).containsEntry("수령", 2L);
    }

}
