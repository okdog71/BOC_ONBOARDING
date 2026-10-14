package com.grovesoft.boctracker.review;

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
class ReviewLogRepositoryTest {

    @Autowired
    ReviewLogRepository repository;
    @Autowired
    TestEntityManager em;

    private ReviewLog a() {
        return ReviewLog.builder()
                .round(1)
                .reviewDate(LocalDate.of(2026, 11, 10))
                .targetDomain("redeem")
                .presenter("담당자 A")
                .attendees("리드, 담당자 B")
                .coveragePct(80)
                .crossCheck("스토리보드 대조")
                .gaps("예외 케이스 누락")
                .devUnconfirmed("mock 응답 확인 필요")
                .action("보완 분석")
                .actionOwner("담당자 A")
                .targetDate(LocalDate.of(2026, 11, 17))
                .status("미착수")
                .build();
    }

    private ReviewLog b() {
        return ReviewLog.builder()
                .round(1)
                .reviewDate(LocalDate.of(2026, 11, 10))
                .targetDomain("redeem")
                .presenter("담당자 A")
                .attendees("리드, 담당자 B")
                .coveragePct(80)
                .crossCheck("스토리보드 대조")
                .gaps("예외 케이스 누락")
                .devUnconfirmed("mock 응답 확인 필요")
                .action("보완 분석")
                .actionOwner("담당자 A")
                .targetDate(LocalDate.of(2026, 11, 17))
                .status("완료")
                .build();
    }

    @Test
    void 한글_데이터_저장_조회() {
        Long id = repository.saveAndFlush(a()).getId();
        em.clear();
        ReviewLog found = repository.findById(id).orElseThrow();
        assertThat(found.getRound()).isEqualTo(1);
        assertThat(found.getReviewDate()).isEqualTo(LocalDate.of(2026, 11, 10));
        assertThat(found.getTargetDomain()).isEqualTo("redeem");
        assertThat(found.getPresenter()).isEqualTo("담당자 A");
        assertThat(found.getAttendees()).isEqualTo("리드, 담당자 B");
        assertThat(found.getCoveragePct()).isEqualTo(80);
        assertThat(found.getCrossCheck()).isEqualTo("스토리보드 대조");
        assertThat(found.getGaps()).isEqualTo("예외 케이스 누락");
        assertThat(found.getDevUnconfirmed()).isEqualTo("mock 응답 확인 필요");
        assertThat(found.getAction()).isEqualTo("보완 분석");
        assertThat(found.getActionOwner()).isEqualTo("담당자 A");
        assertThat(found.getTargetDate()).isEqualTo(LocalDate.of(2026, 11, 17));
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
        ReviewLog noStatus = ReviewLog.builder()
                .round(1)
                .reviewDate(LocalDate.of(2026, 11, 10))
                .targetDomain("redeem")
                .presenter("담당자 A")
                .attendees("리드, 담당자 B")
                .coveragePct(80)
                .crossCheck("스토리보드 대조")
                .gaps("예외 케이스 누락")
                .devUnconfirmed("mock 응답 확인 필요")
                .action("보완 분석")
                .actionOwner("담당자 A")
                .targetDate(LocalDate.of(2026, 11, 17))
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
