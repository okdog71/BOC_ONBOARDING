package com.grovesoft.boctracker.access;

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
class AccessItemRepositoryTest {

    @Autowired
    AccessItemRepository repository;
    @Autowired
    TestEntityManager em;

    private AccessItem a() {
        return AccessItem.builder()
                .category("저장소")
                .target("Git 저장소")
                .usage("소스 열람")
                .envScope("dev")
                .procedure("사내 포털 신청")
                .requestedDate(LocalDate.of(2026, 11, 2))
                .owner("인프라 담당")
                .status("미신청")
                .build();
    }

    private AccessItem b() {
        return AccessItem.builder()
                .category("계정")
                .target("Git 저장소")
                .usage("소스 열람")
                .envScope("dev")
                .procedure("사내 포털 신청")
                .requestedDate(LocalDate.of(2026, 11, 2))
                .owner("인프라 담당")
                .status("승인")
                .build();
    }

    @Test
    void 한글_데이터_저장_조회() {
        Long id = repository.saveAndFlush(a()).getId();
        em.clear();
        AccessItem found = repository.findById(id).orElseThrow();
        assertThat(found.getCategory()).isEqualTo("저장소");
        assertThat(found.getTarget()).isEqualTo("Git 저장소");
        assertThat(found.getUsage()).isEqualTo("소스 열람");
        assertThat(found.getEnvScope()).isEqualTo("dev");
        assertThat(found.getProcedure()).isEqualTo("사내 포털 신청");
        assertThat(found.getRequestedDate()).isEqualTo(LocalDate.of(2026, 11, 2));
        assertThat(found.getOwner()).isEqualTo("인프라 담당");
        assertThat(found.getStatus()).isEqualTo("미신청");
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    @Test
    void search_필터() {
        repository.save(a());
        repository.save(b());
        em.flush();

        assertThat(repository.search("미신청", null, PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);
        assertThat(repository.search(null, "저장소", PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);
        assertThat(repository.search(null, null, PageRequest.of(0, 10)).getTotalElements()).isEqualTo(2);
        assertThat(repository.search("없는값", null, PageRequest.of(0, 10)).getTotalElements()).isZero();
    }

    @Test
    void 상태_미지정시_기본값과_countBy_그룹집계() {
        AccessItem noStatus = AccessItem.builder()
                .category("저장소")
                .target("Git 저장소")
                .usage("소스 열람")
                .envScope("dev")
                .procedure("사내 포털 신청")
                .requestedDate(LocalDate.of(2026, 11, 2))
                .owner("인프라 담당")
                .build();
        assertThat(noStatus.getStatus()).isEqualTo("미신청");
        repository.save(noStatus);
        repository.save(b());
        repository.save(b());
        em.flush();

        assertThat(repository.countByStatus("승인")).isEqualTo(2);
        Map<String, Long> grouped = repository.countGroupByStatus().stream()
                .collect(Collectors.toMap(StatusCount::getStatus, StatusCount::getCount));
        assertThat(grouped).containsEntry("미신청", 1L).containsEntry("승인", 2L);
    }

}
