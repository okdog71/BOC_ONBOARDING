package com.grovesoft.boctracker.handover;

import static org.assertj.core.api.Assertions.assertThat;

import com.grovesoft.boctracker.common.entity.StatusCount;
import java.time.LocalDate;
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
class HandoverSessionRepositoryTest {

    @Autowired
    HandoverSessionRepository repository;
    @Autowired
    TestEntityManager em;

    private HandoverSession session(String week, String category, String status) {
        return HandoverSession.builder().week(week).category(category).domain("환경·배포")
                .topic("KEC/BOC 시스템 개론·아키텍처·도메인 맵").ktStage("설명·병행").status(status).build();
    }

    @Test
    void 한글_데이터와_LocalDate_저장_조회() {
        HandoverSession s = HandoverSession.builder().week("W1").category("공통").domain("전체 개요")
                .topic("아키텍처 개론").ktStage("설명·병행").plannedDate(LocalDate.of(2026, 11, 2))
                .status("예정").build();
        Long id = repository.saveAndFlush(s).getId();
        em.clear();

        HandoverSession found = repository.findById(id).orElseThrow();
        assertThat(found.getDomain()).isEqualTo("전체 개요");
        assertThat(found.getKtStage()).isEqualTo("설명·병행");
        assertThat(found.getPlannedDate()).isEqualTo(LocalDate.of(2026, 11, 2));
        assertThat(found.getCreatedAt()).isNotNull();
    }

    @Test
    void plannedDate_null_허용_및_기본상태() {
        HandoverSession saved = repository.saveAndFlush(session("W2", "KEC", null));
        assertThat(saved.getPlannedDate()).isNull();
        assertThat(saved.getStatus()).isEqualTo("미착수");
    }

    @Test
    void countByStatus_및_그룹집계() {
        repository.save(session("W1", "공통", "미착수"));
        repository.save(session("W1", "BOC", "예정"));
        repository.save(session("W2", "BOC", "예정"));
        em.flush();

        assertThat(repository.countByStatus("예정")).isEqualTo(2);
        Map<String, Long> grouped = repository.countGroupByStatus().stream()
                .collect(Collectors.toMap(StatusCount::getStatus, StatusCount::getCount));
        assertThat(grouped).containsEntry("미착수", 1L).containsEntry("예정", 2L);
    }

    @Test
    void search_복합필터() {
        repository.save(session("W1", "공통", "미착수"));
        repository.save(session("W1", "BOC", "예정"));
        repository.save(session("W2", "BOC", "예정"));
        em.flush();

        assertThat(repository.search(null, "W1", null, null, null, false, LocalDate.now(), PageRequest.of(0, 10)).getTotalElements()).isEqualTo(2);
        assertThat(repository.search("예정", "W1", "BOC", null, null, false, LocalDate.now(), PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);
        assertThat(repository.search(null, null, null, null, null, false, LocalDate.now(), PageRequest.of(0, 10)).getTotalElements()).isEqualTo(3);
    }
}
