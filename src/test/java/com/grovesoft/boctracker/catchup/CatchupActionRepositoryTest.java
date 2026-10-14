package com.grovesoft.boctracker.catchup;

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
class CatchupActionRepositoryTest {

    @Autowired
    CatchupActionRepository repository;
    @Autowired
    TestEntityManager em;

    private CatchupAction a() {
        return CatchupAction.builder()
                .category("투입 전 활용")
                .action("스토리보드 사전 리뷰")
                .purpose("도메인 이해도 선확보")
                .timing("투입전")
                .owner("BOC 리드")
                .kpi("리뷰 완료율 100%")
                .status("미착수")
                .build();
    }

    private CatchupAction b() {
        return CatchupAction.builder()
                .category("투입 전 활용")
                .action("스토리보드 사전 리뷰")
                .purpose("도메인 이해도 선확보")
                .timing("상시")
                .owner("BOC 리드")
                .kpi("리뷰 완료율 100%")
                .status("완료")
                .build();
    }

    @Test
    void 한글_데이터_저장_조회() {
        Long id = repository.saveAndFlush(a()).getId();
        em.clear();
        CatchupAction found = repository.findById(id).orElseThrow();
        assertThat(found.getCategory()).isEqualTo("투입 전 활용");
        assertThat(found.getAction()).isEqualTo("스토리보드 사전 리뷰");
        assertThat(found.getPurpose()).isEqualTo("도메인 이해도 선확보");
        assertThat(found.getTiming()).isEqualTo("투입전");
        assertThat(found.getOwner()).isEqualTo("BOC 리드");
        assertThat(found.getKpi()).isEqualTo("리뷰 완료율 100%");
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
        assertThat(repository.search(null, "투입전", PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);
        assertThat(repository.search(null, null, PageRequest.of(0, 10)).getTotalElements()).isEqualTo(2);
        assertThat(repository.search("없는값", null, PageRequest.of(0, 10)).getTotalElements()).isZero();
    }

    @Test
    void 상태_미지정시_기본값과_countBy_그룹집계() {
        CatchupAction noStatus = CatchupAction.builder()
                .category("투입 전 활용")
                .action("스토리보드 사전 리뷰")
                .purpose("도메인 이해도 선확보")
                .timing("투입전")
                .owner("BOC 리드")
                .kpi("리뷰 완료율 100%")
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
