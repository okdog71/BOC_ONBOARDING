# IntelliJ Claude Code — 부트스트랩 프롬프트

> **사용법**
> 1. 빈 프로젝트 폴더를 만들고 그 안에 `CLAUDE.md`와 `seed/` 폴더(제공된 JSON 12개)를 넣습니다.
> 2. IntelliJ에서 그 폴더를 열고 Claude Code 세션을 시작합니다.
> 3. 아래 "① 최초 부트스트랩 프롬프트" 전체를 복사해 Claude Code에 붙여넣습니다.
> 4. 1단계 완료 후, "② 이어가기 프롬프트"들을 순서대로 사용합니다.
> Claude Code는 `CLAUDE.md`를 자동으로 읽으므로, 프롬프트에는 "무엇을 지금 할지"만 지시하면 됩니다.

---

## ① 최초 부트스트랩 프롬프트 (복붙)

```
먼저 이 프로젝트 루트의 CLAUDE.md를 읽고, 거기 정의된 스택·아키텍처·데이터 모델·규칙을 절대 기준으로 삼아 작업해줘. CLAUDE.md와 충돌하는 임의 선택은 하지 말고, 불명확하면 질문해줘.

지금은 1단계: 프로젝트 골격만 생성한다. 아래를 수행해줘.

[스택] Java 17, Gradle(Groovy, build.gradle), Spring Boot 3.2.x, MySQL/MariaDB, Spring Data JPA, Flyway, Bean Validation, springdoc-openapi(Swagger UI), Lombok.

[할 일]
1. Gradle Spring Boot 프로젝트 골격 생성:
   - build.gradle: 위 의존성 + mysql-connector-j, flyway-core, flyway-mysql, springdoc-openapi-starter-webmvc-ui, lombok, validation, spring-boot-starter-web, spring-boot-starter-data-jpa, (test) spring-boot-starter-test + H2.
   - settings.gradle, gradle wrapper(가능하면), .gitignore(자바/그래들/인텔리제이).
2. 메인 클래스 com.grovesoft.boctracker.BocOnboardingTrackerApplication (@SpringBootApplication, JPA Auditing @EnableJpaAuditing).
3. common 레이어:
   - common/entity/BaseEntity: @MappedSuperclass, id(Long, IDENTITY), createdAt, updatedAt(@CreatedDate/@LastModifiedDate), AuditingEntityListener.
   - common/web/ApiResponse<T>, ErrorResponse, GlobalExceptionHandler(@RestControllerAdvice): 검증오류 400(fieldErrors 포함), EntityNotFound 404, 일반 500. printStackTrace 금지, SLF4J 로깅.
   - common/web/NotFoundException 등 커스텀 예외.
4. application.yml: profile local/test. local은 CLAUDE.md의 datasource 예시대로(DB_USER/DB_PASSWORD 환경변수, DB명 boc_tracker, utf8mb4, Asia/Seoul). jpa.ddl-auto=validate, open-in-view=false, flyway enabled, baseline-on-migrate=true. test는 H2 in-memory.
5. 아직 도메인 엔티티/테이블은 만들지 말고, 애플리케이션이 기동만 되도록(또는 datasource 없이 컨텍스트 로드되도록) 최소 구성으로.
6. README.md 초안(실행 방법은 CLAUDE.md 9장 기준).

작업 후 ./gradlew build 가 통과하도록 하고, 생성한 파일 목록과 다음 단계 제안을 알려줘. 전체를 한꺼번에 만들지 말고 이 1단계 범위만.
```

---

## ② 이어가기 프롬프트 (1단계 통과 후 순서대로)

### 2단계 — 핵심 2개 도메인 (패턴 확립)
```
2단계: Milestone, HandoverSession 두 도메인을 풀스택으로 구현한다. CLAUDE.md의 4장(데이터 모델·코드값)과 5장(API 규칙)을 그대로 따른다.

각 도메인마다:
- Entity(BaseEntity 상속, snake_case 테이블, 통제어휘 필드는 String + 검증)
- Repository(Spring Data JPA, 필요한 countByStatus 등)
- *Request/*Response DTO(Bean Validation, 통제어휘는 @Pattern 또는 커스텀 @EnumValue)
- Mapper(수동 또는 MapStruct 일관)
- Service(@Transactional, 생성자 주입)
- Controller(표준 CRUD + PATCH /{id}/status + 목록 Pageable·필터)
- 테스트: @DataJpaTest(repo), Service 단위, Controller(@WebMvcTest 또는 통합)

통제어휘 검증용 공통 애너테이션 @EnumValue(allowed=...)를 common/validation에 만들어 재사용해줘. ./gradlew build 통과 확인 후 파일 목록 보고.
```

### 3단계 — 나머지 도메인 확장
```
3단계: 2단계와 '완전히 동일한 패턴'으로 나머지 도메인을 구현한다: CatchupAction, SupplyDoc, AccessItem, DocReliability, MenuInventory, MegaProcess, InterfaceLink, IssueQueue, RaciAssignment, ReviewLog, AnalysisCard. CLAUDE.md 4장의 필드·코드값을 정확히 반영. 각 도메인 추가 후 빌드가 깨지지 않게 점진적으로. 완료되면 전체 엔드포인트 목록을 표로 정리해줘.
```

### 4단계 — Flyway 스키마 + Seed 적재
```
4단계: 영속화 기준을 Flyway로 확정한다.
1. src/main/resources/db/migration/V1__schema.sql: 전체 엔티티에 대응하는 MySQL DDL(utf8mb4, snake_case, PK AUTO_INCREMENT, created_at/updated_at). 엔티티와 1:1 정합.
2. jpa.ddl-auto를 validate로 유지(스키마 소스는 Flyway).
3. 프로젝트 루트 seed/ 폴더의 JSON 12개를 src/main/resources/seed/ 로 복사.
4. seed/SeedDataLoader(ApplicationRunner): 각 테이블이 비어있을 때만 해당 seed JSON을 Jackson으로 읽어 저장(idempotent). JSON 키는 엔티티 필드와 1:1. 날짜 필드(null 허용)·status 기본값 처리.
5. 로컬 MySQL에 boc_tracker DB를 만들고 ./gradlew bootRun 시 마이그레이션+seed가 정상 적재되는지 확인하는 방법을 README에 추가.
빌드·기동 확인 후 보고.
```

### 5단계 — 집계 API + 메타
```
5단계: 
1. summary 모듈: GET /api/summary 가 CLAUDE.md 5장의 집계(각 트래커 status/priority별 count)를 반환하도록 구현(Repository count 쿼리 또는 JPQL group by). 엑셀 ⑭요약 시트와 동일한 집계 축.
2. common/meta: GET /api/meta/enums 가 CLAUDE.md 4장 코드값 표를 필드별 허용값 맵으로 반환(향후 UI 드롭다운용).
빌드·테스트 통과 후 보고.
```

### 6단계 — 문서화·마감
```
6단계: 모든 컨트롤러/DTO에 springdoc @Operation/@Schema(한글 요약) 태깅, Swagger UI 동작 확인. README 완성(실행·API 개요·seed 설명). 전체 ./gradlew build 통과 및 엔드포인트 최종 목록 정리.
```

---

## ③ 팁

- Claude Code가 임의로 스택/구조를 바꾸려 하면 "CLAUDE.md 기준을 지켜줘"라고 되돌리세요.
- 단계마다 `./gradlew build` 통과를 확인하고 넘어가면 회귀가 적습니다.
- DB 비밀번호 등은 환경변수(`DB_USER`,`DB_PASSWORD`)로. 커밋에 비밀번호가 들어가지 않게 하세요.
- 추후 화면(React 등)을 붙일 때, 이 API의 `/api/meta/enums`와 `/api/summary`가 드롭다운·대시보드의 기반이 됩니다.
