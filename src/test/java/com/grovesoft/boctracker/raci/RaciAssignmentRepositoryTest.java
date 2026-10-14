package com.grovesoft.boctracker.raci;

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
class RaciAssignmentRepositoryTest {

    @Autowired
    RaciAssignmentRepository repository;
    @Autowired
    TestEntityManager em;

    private RaciAssignment a() {
        return RaciAssignment.builder()
                .domainGroup("redeem/order")
                .scope("쿠폰·주문 도메인")
                .complexity("상")
                .scale("중")
                .generation("B")
                .responsible("담당자 A")
                .backup("담당자 B")
                .reviewer("리드")
                .menuCount(12)
                .targetDate(LocalDate.of(2026, 12, 15))
                .status("미착수")
                .build();
    }

    private RaciAssignment b() {
        return RaciAssignment.builder()
                .domainGroup("redeem/order")
                .scope("쿠폰·주문 도메인")
                .complexity("상")
                .scale("중")
                .generation("B")
                .responsible("담당자 A")
                .backup("담당자 B")
                .reviewer("리드")
                .menuCount(12)
                .targetDate(LocalDate.of(2026, 12, 15))
                .status("진행중")
                .build();
    }

    @Test
    void 한글_데이터_저장_조회() {
        Long id = repository.saveAndFlush(a()).getId();
        em.clear();
        RaciAssignment found = repository.findById(id).orElseThrow();
        assertThat(found.getDomainGroup()).isEqualTo("redeem/order");
        assertThat(found.getScope()).isEqualTo("쿠폰·주문 도메인");
        assertThat(found.getComplexity()).isEqualTo("상");
        assertThat(found.getScale()).isEqualTo("중");
        assertThat(found.getGeneration()).isEqualTo("B");
        assertThat(found.getResponsible()).isEqualTo("담당자 A");
        assertThat(found.getBackup()).isEqualTo("담당자 B");
        assertThat(found.getReviewer()).isEqualTo("리드");
        assertThat(found.getMenuCount()).isEqualTo(12);
        assertThat(found.getTargetDate()).isEqualTo(LocalDate.of(2026, 12, 15));
        assertThat(found.getStatus()).isEqualTo("미착수");
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    @Test
    void search_필터() {
        repository.save(a());
        repository.save(b());
        em.flush();

        assertThat(repository.search("미착수", PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);
        assertThat(repository.search(null, PageRequest.of(0, 10)).getTotalElements()).isEqualTo(2);
        assertThat(repository.search("없는값", PageRequest.of(0, 10)).getTotalElements()).isZero();
    }

    @Test
    void 상태_미지정시_기본값과_countBy_그룹집계() {
        RaciAssignment noStatus = RaciAssignment.builder()
                .domainGroup("redeem/order")
                .scope("쿠폰·주문 도메인")
                .complexity("상")
                .scale("중")
                .generation("B")
                .responsible("담당자 A")
                .backup("담당자 B")
                .reviewer("리드")
                .menuCount(12)
                .targetDate(LocalDate.of(2026, 12, 15))
                .build();
        assertThat(noStatus.getStatus()).isEqualTo("미착수");
        repository.save(noStatus);
        repository.save(b());
        repository.save(b());
        em.flush();

        assertThat(repository.countByStatus("진행중")).isEqualTo(2);
        Map<String, Long> grouped = repository.countGroupByStatus().stream()
                .collect(Collectors.toMap(StatusCount::getStatus, StatusCount::getCount));
        assertThat(grouped).containsEntry("미착수", 1L).containsEntry("진행중", 2L);
    }

}
