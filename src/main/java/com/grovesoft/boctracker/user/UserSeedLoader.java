package com.grovesoft.boctracker.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grovesoft.boctracker.user.dto.UserRequest;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 사용자 테이블이 비어 있으면 초기 사용자를 적재한다(초기 비밀번호 = 로그인 ID, 첫 로그인 후 변경 안내).
 * 저장소에는 업무용 정보(소속·이름·투입일·업무 계정)만 두고, 연락처·개인 메일은 저장소 밖의
 * {app.seed.private-dir}/users-contact.json({loginId: {phone, gmail}})이 있을 때만 합쳐 넣는다.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.auth.bootstrap-users", havingValue = "true", matchIfMissing = true)
public class UserSeedLoader implements ApplicationRunner {

    private final UserRepository repository;
    private final UserService service;
    private final ObjectMapper mapper;
    private final TransactionTemplate tx;
    private final String privateDir;

    public UserSeedLoader(UserRepository repository, UserService service, ObjectMapper mapper, TransactionTemplate tx,
            @Value("${app.seed.private-dir:./data/seed-private}") String privateDir) {
        this.repository = repository;
        this.service = service;
        this.mapper = mapper;
        this.tx = tx;
        this.privateDir = privateDir;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (repository.count() > 0) {
            return;
        }
        List<UserRequest> seed;
        try (InputStream in = new ClassPathResource("seed/users.json").getInputStream()) {
            seed = mapper.readValue(in, new TypeReference<>() { });
        }
        Map<String, Map<String, String>> contact = readContact();
        tx.executeWithoutResult(s -> seed.forEach(u -> {
            Map<String, String> c = contact.getOrDefault(u.loginId(), Map.of());
            service.create(new UserRequest(u.loginId(), u.affiliation(), u.name(), u.joinDate(), u.leaveDate(),
                    u.kraftonAccount(), c.get("phone"), u.groveMail(), u.ip(), c.get("gmail"), u.vpn(), u.bocAccount(),
                    u.kecAccount(), u.aws()));
        }));
        log.info("초기 사용자 {}명 적재(연락처 파일 {})", seed.size(), contact.isEmpty() ? "없음" : "병합");
    }

    private Map<String, Map<String, String>> readContact() {
        Path p = Path.of(privateDir, "users-contact.json");
        if (!Files.isRegularFile(p)) {
            return Map.of();
        }
        try {
            return mapper.readValue(p.toFile(), new TypeReference<>() { });
        } catch (Exception e) {
            log.warn("연락처 시드 파일을 읽지 못해 건너뜁니다: {}", e.getMessage());
            return Map.of();
        }
    }
}
