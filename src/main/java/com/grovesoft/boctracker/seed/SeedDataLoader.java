package com.grovesoft.boctracker.seed;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 기동 시 seed JSON 을 적재한다 ({@code app.seed.enabled=true} 일 때만; local 프로파일 기본 true, test 는 false).
 *
 * <ul>
 *   <li>도메인별로 테이블이 비어 있을 때만(count()==0) 적재한다. 하나라도 데이터가 있으면 그 도메인은 skip.</li>
 *   <li>도메인 단위 all-or-nothing: 한 건이라도 검증/저장에 실패하면 그 도메인은 롤백되고 ERROR 로그를 남긴다
 *       (다른 도메인과 기동은 계속된다).</li>
 *   <li>알 수 없는 JSON 필드는 무시하되 WARN 으로 남긴다.</li>
 * </ul>
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class SeedDataLoader implements ApplicationRunner {

    private final List<SeedTarget<?>> targets;
    private final Validator validator;
    private final TransactionTemplate tx;
    private final String location;
    private final DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
    private final JsonMapper mapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .build();

    @Autowired
    public SeedDataLoader(SeedTargetRegistry registry, Validator validator, PlatformTransactionManager tm,
                          @Value("${app.seed.location:classpath:seed/}") String location) {
        this(registry.targets(), validator, tm, location);
    }

    /** 테스트에서 임의의 매핑/위치로 구성하기 위한 생성자. */
    public SeedDataLoader(List<SeedTarget<?>> targets, Validator validator, PlatformTransactionManager tm,
                          String location) {
        this.targets = targets;
        this.validator = validator;
        this.tx = new TransactionTemplate(tm);
        this.location = location.endsWith("/") ? location : location + "/";
    }

    @Override
    public void run(ApplicationArguments args) {
        loadAll();
    }

    /** @return 도메인명 → 이번에 적재한 건수(skip/없음/실패는 0) */
    public Map<String, Integer> loadAll() {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (SeedTarget<?> target : targets) {
            int loaded = 0;
            try {
                loaded = loadOne(target);
            } catch (Exception e) {
                log.error("[seed] {} 적재 실패 — 해당 도메인은 롤백됨: {}", target.domain(), e.getMessage(), e);
            }
            result.put(target.domain(), loaded);
        }
        log.info("[seed] 도메인별 적재 건수: {}", result);
        return result;
    }

    private <R> int loadOne(SeedTarget<R> target) throws IOException {
        long existing = target.counter().getAsLong();
        if (existing > 0) {
            log.info("[seed] {} skip — 기존 데이터 {}건", target.domain(), existing);
            return 0;
        }

        Resource resource = resourceLoader.getResource(location + target.file());
        if (!resource.exists()) {
            log.debug("[seed] {} — 파일 없음({}), skip", target.domain(), target.file());
            return 0;
        }
        JsonNode root;
        try (InputStream in = resource.getInputStream()) {
            root = mapper.readTree(in);
        }
        if (root == null || root.isMissingNode() || root.isEmpty()) {
            log.debug("[seed] {} — 빈 파일/배열({}), skip", target.domain(), target.file());
            return 0;
        }
        if (!root.isArray()) {
            throw new IllegalStateException(target.file() + " 은(는) JSON 배열이어야 합니다.");
        }

        warnUnknownFields(target, root);

        List<R> rows = new ArrayList<>();
        for (JsonNode node : root) {
            rows.add(mapper.treeToValue(node, target.requestType()));
        }
        validate(target, rows);

        tx.executeWithoutResult(status -> rows.forEach(target.creator()));
        log.info("[seed] {} {}건 적재 ({})", target.domain(), rows.size(), target.file());
        return rows.size();
    }

    private void warnUnknownFields(SeedTarget<?> target, JsonNode array) {
        Set<String> known = new LinkedHashSet<>();
        Arrays.stream(target.requestType().getRecordComponents()).map(RecordComponent::getName).forEach(known::add);
        Set<String> unknown = new LinkedHashSet<>();
        for (JsonNode node : array) {
            node.fieldNames().forEachRemaining(name -> {
                if (!known.contains(name)) {
                    unknown.add(name);
                }
            });
        }
        if (!unknown.isEmpty()) {
            log.warn("[seed] {} 알 수 없는 필드 무시: {}", target.domain(), unknown);
        }
    }

    private <R> void validate(SeedTarget<R> target, List<R> rows) {
        List<String> problems = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            for (ConstraintViolation<R> v : validator.validate(rows.get(i))) {
                problems.add("#" + i + " " + v.getPropertyPath() + ": " + v.getMessage());
            }
        }
        if (!problems.isEmpty()) {
            throw new IllegalStateException(target.file() + " 검증 실패 " + problems.size() + "건: "
                    + problems.subList(0, Math.min(5, problems.size())));
        }
    }
}
