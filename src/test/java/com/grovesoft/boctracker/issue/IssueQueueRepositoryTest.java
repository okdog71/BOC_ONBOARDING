package com.grovesoft.boctracker.issue;

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
class IssueQueueRepositoryTest {

    @Autowired
    IssueQueueRepository repository;
    @Autowired
    TestEntityManager em;

    private IssueQueue a() {
        return IssueQueue.builder()
                .type("이슈")
                .item("KEC-01")
                .content("쿠폰 중복 등록 허용 여부 불명확")
                .impact("정산 오류 가능")
                .basis("dev 화면 확인")
                .confirmWith("현업 담당자")
                .owner("BOC 리드")
                .targetDate(LocalDate.of(2026, 11, 30))
                .action("현업 인터뷰")
                .status("미착수")
                .build();
    }

    private IssueQueue b() {
        return IssueQueue.builder()
                .type("미확인")
                .item("KEC-01")
                .content("쿠폰 중복 등록 허용 여부 불명확")
                .impact("정산 오류 가능")
                .basis("dev 화면 확인")
                .confirmWith("현업 담당자")
                .owner("BOC 리드")
                .targetDate(LocalDate.of(2026, 11, 30))
                .action("현업 인터뷰")
                .status("확인중")
                .build();
    }

    @Test
    void 한글_데이터_저장_조회() {
        Long id = repository.saveAndFlush(a()).getId();
        em.clear();
        IssueQueue found = repository.findById(id).orElseThrow();
        assertThat(found.getType()).isEqualTo("이슈");
        assertThat(found.getItem()).isEqualTo("KEC-01");
        assertThat(found.getContent()).isEqualTo("쿠폰 중복 등록 허용 여부 불명확");
        assertThat(found.getImpact()).isEqualTo("정산 오류 가능");
        assertThat(found.getBasis()).isEqualTo("dev 화면 확인");
        assertThat(found.getConfirmWith()).isEqualTo("현업 담당자");
        assertThat(found.getOwner()).isEqualTo("BOC 리드");
        assertThat(found.getTargetDate()).isEqualTo(LocalDate.of(2026, 11, 30));
        assertThat(found.getAction()).isEqualTo("현업 인터뷰");
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
        assertThat(repository.search(null, "이슈", PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);
        assertThat(repository.search(null, null, PageRequest.of(0, 10)).getTotalElements()).isEqualTo(2);
        assertThat(repository.search("없는값", null, PageRequest.of(0, 10)).getTotalElements()).isZero();
    }

    @Test
    void 상태_미지정시_기본값과_countBy_그룹집계() {
        IssueQueue noStatus = IssueQueue.builder()
                .type("이슈")
                .item("KEC-01")
                .content("쿠폰 중복 등록 허용 여부 불명확")
                .impact("정산 오류 가능")
                .basis("dev 화면 확인")
                .confirmWith("현업 담당자")
                .owner("BOC 리드")
                .targetDate(LocalDate.of(2026, 11, 30))
                .action("현업 인터뷰")
                .build();
        assertThat(noStatus.getStatus()).isEqualTo("미착수");
        repository.save(noStatus);
        repository.save(b());
        repository.save(b());
        em.flush();

        assertThat(repository.countByStatus("확인중")).isEqualTo(2);
        Map<String, Long> grouped = repository.countGroupByStatus().stream()
                .collect(Collectors.toMap(StatusCount::getStatus, StatusCount::getCount));
        assertThat(grouped).containsEntry("미착수", 1L).containsEntry("확인중", 2L);
    }

}
