package com.grovesoft.boctracker.milestone;

import static org.assertj.core.api.Assertions.assertThat;

import com.grovesoft.boctracker.common.entity.StatusCount;
import java.util.List;
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
class MilestoneRepositoryTest {

    @Autowired
    MilestoneRepository repository;
    @Autowired
    TestEntityManager em;

    private Milestone milestone(String code, String status) {
        return Milestone.builder().phase("P0. 수급·준비").code(code).name("수급 산출물 확인")
                .goal("기존 BOC 운영 산출물의 존재/위치/최신성/owner 확인").status(status).build();
    }

    @Test
    void 한글_데이터_저장_조회_및_감사필드() {
        Milestone saved = repository.saveAndFlush(milestone("T-1", "진행중"));
        em.clear();

        Milestone found = repository.findById(saved.getId()).orElseThrow();
        assertThat(found.getPhase()).isEqualTo("P0. 수급·준비");
        assertThat(found.getName()).isEqualTo("수급 산출물 확인");
        assertThat(found.getGoal()).contains("존재/위치/최신성");
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    @Test
    void status_미지정시_기본값_미착수() {
        Milestone saved = repository.saveAndFlush(milestone("T-2", null));
        assertThat(saved.getStatus()).isEqualTo("미착수");
    }

    @Test
    void countByStatus_및_그룹집계() {
        repository.save(milestone("T-3", "미착수"));
        repository.save(milestone("T-4", "완료"));
        repository.save(milestone("T-5", "완료"));
        em.flush();

        assertThat(repository.countByStatus("완료")).isEqualTo(2);
        Map<String, Long> grouped = repository.countGroupByStatus().stream()
                .collect(Collectors.toMap(StatusCount::getStatus, StatusCount::getCount));
        assertThat(grouped).containsEntry("미착수", 1L).containsEntry("완료", 2L);
    }

    @Test
    void search_필터() {
        repository.save(milestone("T-6", "진행중"));
        repository.save(milestone("T-7", "완료"));
        em.flush();

        List<String> codes = repository.search("완료", null, PageRequest.of(0, 10)).map(Milestone::getCode).toList();
        assertThat(codes).containsExactly("T-7");
        assertThat(repository.search(null, "P0. 수급·준비", PageRequest.of(0, 10)).getTotalElements()).isEqualTo(2);
        assertThat(repository.search(null, "없는단계", PageRequest.of(0, 10)).getTotalElements()).isZero();
    }
}
