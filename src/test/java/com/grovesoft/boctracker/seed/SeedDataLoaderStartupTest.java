package com.grovesoft.boctracker.seed;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grovesoft.boctracker.analysiscard.AnalysisCardRepository;
import com.grovesoft.boctracker.catchup.CatchupActionRepository;
import com.grovesoft.boctracker.milestone.MilestoneRepository;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;

/**
 * app.seed.enabled=true 로 기동하면 실제 seed(classpath:seed/*.json)가 ApplicationRunner 로 적재되는지 검증.
 * 공유 H2 를 오염시키지 않도록 별도 인메모리 DB 를 사용한다.
 */
@SpringBootTest(properties = {
        "app.seed.enabled=true",
        "spring.datasource.url=jdbc:h2:mem:seed_startup;MODE=MySQL;DB_CLOSE_DELAY=-1"})
@ActiveProfiles("test")
class SeedDataLoaderStartupTest {

    @Autowired
    SeedDataLoader loader;
    @Autowired
    SeedTargetRegistry registry;
    @Autowired
    MilestoneRepository milestoneRepository;
    @Autowired
    AnalysisCardRepository analysisCardRepository;
    @Autowired
    CatchupActionRepository catchupRepository;
    @Autowired
    ObjectMapper om;

    private int jsonSize(String file) throws Exception {
        try (InputStream in = new ClassPathResource("seed/" + file).getInputStream()) {
            JsonNode root = om.readTree(in);
            return root.size();
        }
    }

    private Map<String, Long> counts() {
        Map<String, Long> m = new LinkedHashMap<>();
        registry.targets().forEach(t -> m.put(t.domain(), t.counter().getAsLong()));
        return m;
    }

    @Test
    void 기동_시_12개_매핑_모두_seed_건수와_일치하게_적재된다() throws Exception {
        assertThat(registry.targets()).hasSize(12);
        for (SeedTarget<?> t : registry.targets()) {
            assertThat(t.counter().getAsLong()).as(t.domain() + " <- " + t.file()).isEqualTo(jsonSize(t.file()));
        }
        // 비어 있는 seed / seed 없는 도메인
        assertThat(counts()).containsEntry("ReviewLog", 0L);
        assertThat(analysisCardRepository.count()).isZero();
        // 한글 데이터
        assertThat(milestoneRepository.findAll()).extracting("name").contains("수급 산출물 확인");
    }

    @Test
    void 재실행하면_전부_skip되고_건수가_변하지_않는다() {
        Map<String, Long> before = counts();

        Map<String, Integer> result = loader.loadAll();

        assertThat(result.values()).containsOnly(0);
        assertThat(counts()).isEqualTo(before);
    }

    @Test
    void 도메인별로_판단한다_비워진_도메인만_다시_적재된다() throws Exception {
        Map<String, Long> before = counts();
        // 마일스톤은 인수인계 세션이 FK 로 참조하므로(삭제 불가), 참조되지 않는 도메인으로 확인한다.
        catchupRepository.deleteAll();

        Map<String, Integer> result = loader.loadAll();

        assertThat(result).containsEntry("CatchupAction", jsonSize("catchup-actions.json"));
        result.forEach((domain, n) -> {
            if (!domain.equals("CatchupAction")) {
                assertThat(n).as(domain).isZero();
            }
        });
        assertThat(counts()).isEqualTo(before);
    }
}
