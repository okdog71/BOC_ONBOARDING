# CLAUDE.md — BOC 온보딩·인수인계 마일스톤 관리 시스템

> 이 파일은 IntelliJ의 **Claude Code**가 세션 시작 시 자동으로 읽는 프로젝트 컨텍스트입니다.
> 여기 적힌 스택·아키텍처·데이터 모델·규칙을 **프로젝트의 단일 기준**으로 삼아 개발하세요.
> 모호하면 추측해서 진행하지 말고, 이 문서의 규칙을 우선 적용하고, 그래도 불명확하면 질문하세요.

---

## 1. 프로젝트 개요

- **이름**: `boc-onboarding-tracker`
- **목적**: KRAFTON BOC/KEC 운영 인수인계·온보딩의 **마일스톤·인수인계(KT)·캐치업·수급/접근/신뢰도·메뉴 분석·이슈큐·RACI·회고**를 관리하는 백엔드 시스템. (기존 엑셀 `BOC_분석_온보딩_마일스톤_완전판_v3.xlsx`을 시스템화한 것)
- **범위(v1)**: **REST API 전용** (프런트/화면은 추후 별도). API는 향후 React/사내 공유 UI가 붙을 수 있도록 깔끔한 리소스 설계 유지.
- **운영 주체**: Grove Soft 내부 관리 도구(로컬 개발 우선). 민감정보(계정 비밀번호·토큰·개인정보)는 저장하지 않는다.

## 2. 기술 스택 (확정)

| 항목 | 선택 |
|---|---|
| 언어 | **Java 17** |
| 빌드 | **Gradle (Groovy DSL, `build.gradle`)** |
| 프레임워크 | **Spring Boot 3.2.x** (Java 17 호환 최신 안정 버전) |
| DB | **MySQL / MariaDB** (로컬 설치본) |
| 영속성 | Spring Data JPA (Hibernate) |
| 마이그레이션 | **Flyway** (`src/main/resources/db/migration`) |
| 검증 | Jakarta Bean Validation (`spring-boot-starter-validation`) |
| 문서화 | **springdoc-openapi** (Swagger UI: `/swagger-ui.html`) |
| 보조 | Lombok |
| 테스트 | JUnit 5, Spring Boot Test, H2(test profile) 또는 Testcontainers(MySQL) |

- Spring Boot 3.x는 Java 17 이상 필수 → 스택 정합.
- 운영 BOC는 세대 A(Java17/SB2.x)·세대 B(Kotlin/Java21/SB3.x)가 공존하나, **본 도구는 Java 17 + SB3.x**로 고정(세대 경계와 무관한 독립 도구).

## 3. 패키지 · 레이어 구조

base package: `com.grovesoft.boctracker`

```
com.grovesoft.boctracker
├─ BocOnboardingTrackerApplication.java
├─ common/              # 공통: 응답 래퍼, 예외, BaseEntity(Auditing), enum 메타
│  ├─ web/              # @RestControllerAdvice(GlobalExceptionHandler), ApiResponse, ErrorResponse
│  ├─ entity/           # BaseEntity(id, createdAt, updatedAt) @MappedSuperclass + JPA Auditing
│  └─ meta/             # EnumController: 허용 코드값(드롭다운) 제공
├─ milestone/           # 도메인 모듈 1개 = {entity, repository, service, dto, mapper, controller}
│  ├─ Milestone.java
│  ├─ MilestoneRepository.java
│  ├─ MilestoneService.java
│  ├─ MilestoneController.java
│  └─ dto/ (MilestoneRequest, MilestoneResponse)
├─ handover/            # 인수인계(KT)
├─ catchup/             # 캐치업
├─ supplydoc/           # 수급 산출물
├─ access/              # 접근권한
├─ docreliability/      # 문서 신뢰도
├─ menu/                # 메뉴 인벤토리
├─ megaprocess/         # 메가프로세스
├─ interfacelink/       # 연동 인터페이스
├─ analysiscard/        # 메뉴별 분석 카드
├─ issue/               # 이슈·미확인 큐
├─ raci/                # RACI 배정
├─ review/              # 회고 리뷰 로그
├─ summary/             # 집계(⑭요약 대응)
└─ seed/                # SeedDataLoader (최초 1회 seed JSON 적재)
```

**레이어 규칙**: `Controller → Service → Repository`. Entity는 API로 직접 노출 금지(항상 DTO 변환). 매핑은 수동 매퍼 또는 MapStruct(택1, 일관 유지). 의존성 주입은 **생성자 주입**만 사용(`@RequiredArgsConstructor`). 트랜잭션은 Service 계층(`@Transactional`, 조회는 `readOnly=true`).

## 4. 데이터 모델

공통 규칙:
- 모든 엔티티는 `BaseEntity` 상속: `id`(BIGINT, AUTO_INCREMENT PK), `createdAt`, `updatedAt`(JPA Auditing).
- 테이블/컬럼은 **snake_case**, 엔티티 필드는 camelCase. 긴 텍스트는 `TEXT`/`VARCHAR(1000)`.
- 상태·분류 등 **통제 어휘(controlled vocabulary)** 필드는 `VARCHAR`로 저장하고, 허용값을 **4장 말미 '코드값' 표**로 관리하며 Bize Validation(`@Pattern` 또는 커스텀 `@EnumValue`)으로 검증한다. (엑셀 드롭다운과 1:1 대응. Java enum 대신 String+검증을 쓰는 이유: 한글 드롭다운 값과의 정합·확장 유연성.)
- 날짜는 `LocalDate`, 시각은 `LocalDateTime`. 기본 `status`는 엔티티 생성 시 기본값 세팅.

### 엔티티 목록 (필드 = seed JSON 키와 1:1)

1. **Milestone** `milestone` — `phase, code, name, goal, checkpoint, output, precondition, owner, durationWeeks, status, doneCriteria, note`
2. **HandoverSession** `handover_session` (인수인계 KT) — `week, category, domain, topic, ktStage, handoverFrom, handoverTo, method, output, precondition, plannedDate(LocalDate), status, note`
3. **CatchupAction** `catchup_action` — `category, action, purpose, timing, owner, kpi, status, note`
4. **SupplyDoc** `supply_doc` — `category, name, expectedContent, usagePurpose, existence, location, freshness, owner, supplyStatus, note`
5. **AccessItem** `access_item` — `category, target, usage, envScope, procedure, status, requestedDate(LocalDate), approvedDate(LocalDate), owner, note`
6. **DocReliability** `doc_reliability` — `docName, source, lastUpdated, freshness, codeFit, grade, verifyMethod, gap, owner, note`
7. **MenuInventory** `menu_inventory` — `gnb, menu, submenu, path, domain, backendService, dataStore, externalLink, permission, generation, priority, knownIssue, devCheck, owner, analysisStatus, note`
8. **MegaProcess** `mega_process` — `l0, l1, l2, l3, relatedMenu, relatedService, externalLink, outputData, note`
9. **InterfaceLink** `interface_link` (연동) — `systemName, direction, interfaceName, usage, auth, data, devCheck, note`
10. **AnalysisCard** `analysis_card` — `menuName, path, domain, backendService, generation, summary, scenario, input, output, backendMapping, dataStore, externalLink, permission, relatedProcess, knownIssue, devCheck, risk, owner, analyzedDate(LocalDate), status`
11. **IssueQueue** `issue_queue` — `type, item, content, impact, basis, confirmWith, owner, status, targetDate(LocalDate), action, note`
12. **RaciAssignment** `raci_assignment` — `domainGroup, scope, complexity, scale, generation, responsible, backup, reviewer, menuCount(Integer), status, targetDate(LocalDate), note`
13. **ReviewLog** `review_log` — `round(Integer), reviewDate(LocalDate), targetDomain, presenter, attendees, coveragePct(Integer), crossCheck, gaps, devUnconfirmed, action, actionOwner, targetDate(LocalDate), status`

> 관계(FK)는 v1에서는 **느슨한 문자열 참조**로 둔다. 예: `MenuInventory.knownIssue`는 `IssueQueue`의 식별 문자열(예: "KEC-01"), `Milestone.output`은 시트/산출물명 문자열. (정규화 FK는 v2 과제.)

### 코드값 (허용 어휘 — 엑셀 드롭다운과 동일). `GET /api/meta/enums`로도 제공.

| 필드 | 허용값 |
|---|---|
| Milestone.status / Catchup.status / Menu.analysisStatus / Raci.status / Review.status / AnalysisCard.status | `미착수, 진행중, 완료, 보류` |
| HandoverSession.status | `미착수, 예정, 진행중, 완료, 보류` |
| HandoverSession.ktStage | `설명, 관찰, 병행, 독립` (복합 표기 허용: 예 "설명·병행") |
| HandoverSession.week | `W1, W2, W3, W4` |
| HandoverSession.category | `공통, KEC, BOC` |
| CatchupAction.timing | `투입전, 투입후, 상시` |
| SupplyDoc.supplyStatus | `미요청, 요청, 수령, 해당없음` |
| SupplyDoc.existence | `있음, 없음, 부분, 미확인` |
| SupplyDoc.freshness / DocReliability.freshness | `현행, 레거시, 혼재, 불명, 미확인` |
| AccessItem.status | `미신청, 신청, 승인, 거부, 보류` |
| DocReliability.grade | `A, B, C` |
| DocReliability.codeFit | `높음, 보통, 낮음, 미확인` |
| MenuInventory.priority / Raci.complexity / Raci.scale | `상, 중, 하` |
| MenuInventory.generation / Raci.generation | `A, B, 혼용, 미확인, -` |
| MenuInventory.devCheck / InterfaceLink.devCheck | `확인, dev미노출, mock, 미확인` |
| IssueQueue.type | `이슈, 미확인, 리스크` |
| IssueQueue.status | `미착수, 확인중, 진행중, 완료, 보류` |

## 5. REST API 규칙

- 리소스: 복수형 kebab-case. 엔티티별 표준 CRUD.
  - `GET /api/milestones`, `GET /api/milestones/{id}`, `POST /api/milestones`, `PUT /api/milestones/{id}`, `PATCH /api/milestones/{id}/status`, `DELETE /api/milestones/{id}`
  - handover-sessions, catchup-actions, supply-docs, access-items, doc-reliabilities, menu-inventories, mega-processes, interface-links, analysis-cards, issue-queues, raci-assignments, review-logs
- 목록은 Spring Data `Pageable` + 주요 필터 쿼리 파라미터(예: `?status=&week=&category=&priority=`). 정렬 기본값 지정.
- 요청 바디는 `*Request` DTO(+Bean Validation), 응답은 `*Response` DTO. Entity 직접 반환 금지.
- 공통 응답 래퍼 `ApiResponse<T>{ success, data, error }` 사용(혹은 순수 DTO+적절한 HTTP status 중 택1, 일관 유지).
- 예외는 `@RestControllerAdvice GlobalExceptionHandler`에서 처리: 검증오류 400, 미존재 404, 서버오류 500. `ErrorResponse{ code, message, fieldErrors }`.
- **집계 API** (`summary` — ⑭요약 대응):
  - `GET /api/summary` → 전체 대시보드: 각 트래커의 status/priority 별 count 묶음.
  - 예시 필드: `menu{total,완료,진행중,미착수,보류, priority{상,중,하}}`, `milestone{total, byStatus}`, `handover{totalSessions, byStatus}`, `catchup{total, byStatus}`, `supply{total, bySupplyStatus}`, `access{total, byStatus}`, `issue{total, byStatus}`.
  - 구현은 Repository의 `count*`/`countByStatus` 또는 JPQL group-by.
- OpenAPI: 각 컨트롤러·DTO에 `@Operation`/`@Schema` 요약 태깅. 한글 설명 허용.

## 6. DB · 마이그레이션 · Seed

- **Flyway 사용**. `src/main/resources/db/migration/V1__schema.sql`(스키마), 이후 변경은 `V2__...`, `V3__...`로 증분.
- `spring.jpa.hibernate.ddl-auto=validate` (운영 안전). 스키마의 source of truth는 Flyway. 로컬 최초 개발 중에는 `update` 허용하되, 안정화 후 `validate`로 전환.
- **Seed 적재**: `seed/SeedDataLoader` (`CommandLineRunner` 또는 `ApplicationRunner`)가 애플리케이션 기동 시 **각 테이블이 비어 있으면** `src/main/resources/seed/*.json`을 읽어 insert. (idempotent — 이미 데이터 있으면 skip.) JSON은 Jackson으로 `List<*Request>` 역직렬화 후 저장.
  - seed 파일: `milestones.json, handover-sessions.json, catchup-actions.json, supply-docs.json, access-items.json, doc-reliability.json, menu-inventory.json, mega-processes.json, interfaces.json, issue-queue.json, raci-assignments.json, review-logs.json`
  - **이 seed 파일들은 이미 제공됨**(프로젝트의 `seed/` 폴더). `src/main/resources/seed/`로 복사해 사용. 키 이름은 엔티티 필드와 1:1.
- `application.yml` (환경별 profile: `local`, `test`):
  - `local`: MySQL/MariaDB datasource(아래 placeholder를 실제 로컬 계정으로), Flyway enabled, seed 활성.
  - `test`: H2 in-memory 또는 Testcontainers MySQL, Flyway enabled, seed 비활성(또는 fixture).

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/boc_tracker?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8
    username: ${DB_USER:root}
    password: ${DB_PASSWORD:changeme}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate.ddl-auto: validate
    properties.hibernate.dialect: org.hibernate.dialect.MySQLDialect
    open-in-view: false
  flyway:
    enabled: true
    baseline-on-migrate: true
```
- 문자셋: DB/테이블 `utf8mb4`. (한글 데이터)

## 7. 코딩 컨벤션

- 생성자 주입만(`@RequiredArgsConstructor`), 필드 주입 금지.
- Service에 비즈니스 로직, Controller는 얇게. Repository는 Spring Data 인터페이스.
- DTO ↔ Entity 매핑 일관(수동 매퍼 클래스 `*Mapper` 권장, 또는 MapStruct 전면 적용).
- 커스텀 예외 + `@RestControllerAdvice`. `printStackTrace()` 금지, SLF4J 로깅.
- 입력 검증은 DTO에 Bean Validation 애너테이션. 통제 어휘는 `@Pattern`/커스텀 검증.
- 커밋 전 `./gradlew build` 통과(테스트 포함). 포매팅 일관(기본 IntelliJ 포맷 또는 spotless 도입 택1).
- 민감정보 하드코딩 금지. DB 비밀번호는 환경변수(`DB_USER`,`DB_PASSWORD`).

## 8. 테스트

- 각 도메인: Repository 슬라이스 테스트(`@DataJpaTest`), Service 단위 테스트, Controller `@WebMvcTest` 또는 통합 `@SpringBootTest`.
- 최소: 핵심 도메인(Milestone, HandoverSession, Summary) happy-path + 검증 실패 케이스.
- test profile은 H2(빠름) 또는 Testcontainers MySQL(정합) 중 택1.

## 9. 실행 방법 (README에도 기재)

```bash
# 1) MySQL에 DB 생성
#    CREATE DATABASE boc_tracker CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
# 2) 환경변수 또는 application-local.yml 에 계정 설정
# 3) 실행
./gradlew bootRun               # 기동 시 Flyway 마이그레이션 + seed 적재(비어있을 때)
# 4) Swagger UI
#    http://localhost:8080/swagger-ui.html
```

## 10. 작업 우선순위 (Claude Code에게)

1. 프로젝트 골격(build.gradle, application.yml, 메인 클래스, common 레이어).
2. **Milestone / HandoverSession** 도메인 풀스택(entity→repo→service→dto→controller→test) — 패턴 확립.
3. 나머지 도메인 동일 패턴으로 확장(Catchup, SupplyDoc, Access, DocReliability, MenuInventory, MegaProcess, InterfaceLink, IssueQueue, Raci, ReviewLog, AnalysisCard).
4. Flyway V1 스키마 + SeedDataLoader + seed JSON 복사.
5. Summary 집계 API + `/api/meta/enums`.
6. OpenAPI 태깅, README.

> 한 번에 전부 생성하지 말고 **2번(핵심 2개 도메인) → 검증(빌드/테스트 통과) → 나머지 확장** 순으로 진행. 각 단계 후 빌드 통과를 확인하세요.
