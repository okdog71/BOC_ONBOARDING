package com.grovesoft.boctracker.seed;

import static org.assertj.core.api.Assertions.assertThat;

import com.grovesoft.boctracker.milestone.Milestone;
import com.grovesoft.boctracker.milestone.MilestoneRepository;
import com.grovesoft.boctracker.milestone.MilestoneService;
import com.grovesoft.boctracker.milestone.dto.MilestoneRequest;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;

/** SeedDataLoader 의 적재 규칙 검증. 공유 H2 를 오염시키지 않도록 별도 인메모리 DB 를 사용한다(트랜잭션 커밋 필요). */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:seed_edge;MODE=MySQL;DB_CLOSE_DELAY=-1")
@ActiveProfiles("test")
class SeedDataLoaderEdgeCaseTest {

    @Autowired
    MilestoneRepository repository;
    @Autowired
    MilestoneService service;
    @Autowired
    Validator validator;
    @Autowired
    PlatformTransactionManager tm;
    @Autowired
    ApplicationContext context;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    private SeedDataLoader loaderFor(String file) {
        List<SeedTarget<?>> targets = List.of(new SeedTarget<>("Milestone", file, MilestoneRequest.class,
                repository::count, service::create));
        return new SeedDataLoader(targets, validator, tm, "classpath:seed-test/");
    }

    @Test
    void test_프로파일에서는_로더_빈이_없다() {
        assertThat(context.getBeanNamesForType(SeedDataLoader.class)).isEmpty();
        assertThat(context.getBeanNamesForType(SeedTargetRegistry.class)).isEmpty();
    }

    @Test
    void 비어_있으면_적재하고_알수없는_필드는_무시한다() {
        Map<String, Integer> result = loaderFor("good.json").loadAll();

        assertThat(result).containsEntry("Milestone", 2);
        assertThat(repository.findAll()).extracting(Milestone::getName)
                .containsExactlyInAnyOrder("수급 산출물 확인", "접근권한 확보");
        assertThat(repository.findAll()).extracting(Milestone::getStatus)
                .containsExactlyInAnyOrder("미착수", "진행중");
    }

    @Test
    void 데이터가_이미_있으면_skip하고_재실행해도_늘지_않는다() {
        SeedDataLoader loader = loaderFor("good.json");
        loader.loadAll();
        assertThat(loader.loadAll()).containsEntry("Milestone", 0);
        assertThat(repository.count()).isEqualTo(2);

        repository.deleteAll();
        service.create(new MilestoneRequest("P9", "X-1", "기존 데이터", null, null, null, null, null, null,
                null, null, null, null));
        assertThat(loaderFor("good.json").loadAll()).containsEntry("Milestone", 0);
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void 빈_배열과_없는_파일은_조용히_skip한다() {
        assertThat(loaderFor("empty.json").loadAll()).containsEntry("Milestone", 0);
        assertThat(loaderFor("does-not-exist.json").loadAll()).containsEntry("Milestone", 0);
        assertThat(repository.count()).isZero();
    }

    @Test
    void 검증_실패_행이_있으면_도메인_전체를_적재하지_않는다() {
        assertThat(loaderFor("invalid.json").loadAll()).containsEntry("Milestone", 0);
        assertThat(repository.count()).isZero();
    }

    @Test
    void 저장_중_실패하면_이미_저장한_행도_롤백된다() {
        assertThat(loaderFor("duplicate.json").loadAll()).containsEntry("Milestone", 0);
        assertThat(repository.count()).isZero();
    }

    @Test
    void 배열이_아닌_파일은_실패로_처리하되_예외를_던지지_않는다() {
        assertThat(loaderFor("not-array.json").loadAll()).containsEntry("Milestone", 0);
        assertThat(repository.count()).isZero();
    }
}
