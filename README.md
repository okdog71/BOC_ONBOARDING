# boc-onboarding-tracker

KRAFTON BOC/KEC 운영 인수인계·온보딩의 **마일스톤·인수인계(KT)·캐치업·수급/접근/신뢰도·메뉴 분석·이슈큐·RACI·회고**를 관리하는 백엔드(REST API 전용).
기존 엑셀 `BOC_분석_온보딩_마일스톤_완전판_v3.xlsx`를 시스템화한 것이며, 스택·데이터 모델·규칙의 기준은 [CLAUDE.md](CLAUDE.md)입니다.

| 항목 | 내용 |
|---|---|
| 언어/빌드 | Java 17 (JDK 17 이상으로 빌드, `release 17` 고정) · Gradle (Groovy DSL) |
| 프레임워크 | Spring Boot 3.2.x · Spring Data JPA · Bean Validation · Lombok |
| DB | MySQL / MariaDB (utf8mb4) · Flyway 마이그레이션 (`V1`~`V3`) |
| 문서 | springdoc-openapi (Swagger UI) |
| 테스트 | JUnit 5 · Spring Boot Test · H2 (MySQL 모드, test 프로파일) |

---

## 1. 빠른 시작

### 1-1. DB 준비

```sql
CREATE DATABASE boc_tracker CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

스키마(테이블)는 앱 기동 시 Flyway가 만듭니다. 직접 만들 필요 없습니다.

### 1-2. 계정 설정 (환경변수)

비밀번호는 코드/설정 파일에 넣지 않고 환경변수로 줍니다. (기본값: `DB_USER=root`, `DB_PASSWORD=changeme`)

| 환경변수 | 설명 | 기본값 |
|---|---|---|
| `DB_USER` | MySQL 계정 | `root` |
| `DB_PASSWORD` | MySQL 비밀번호 | `changeme` |

```bash
# macOS / Linux / Git Bash
export DB_USER=root
export DB_PASSWORD='내_비밀번호'
```

```powershell
# Windows PowerShell
$env:DB_USER = 'root'
$env:DB_PASSWORD = '내_비밀번호'
```

접속 URL은 `jdbc:mysql://localhost:3306/boc_tracker` (타임존 `Asia/Seoul`, `characterEncoding=UTF-8`)입니다. 다른 호스트/포트를 쓰려면 `src/main/resources/application-local.yml`(gitignore 대상)에서 `spring.datasource.url`을 덮어쓰세요.

### 1-3. 실행

```bash
./gradlew bootRun            # Windows: .\gradlew.bat bootRun
```

기동 시 **① Flyway 마이그레이션 → ② seed 적재(테이블이 비어 있을 때만)** 순서로 진행됩니다.

### 1-4. 확인

- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>
- 예: <http://localhost:8080/api/summary>, <http://localhost:8080/api/meta/enums>

### 1-5. 빌드 / 테스트

```bash
./gradlew build              # 컴파일 + 테스트(H2, test 프로파일)
./gradlew test
```

커밋 전에는 `./gradlew build` 통과를 확인합니다.

---

## 2. 프로젝트 경로에 한글이 있는 경우 (Windows)

**가능하면 프로젝트를 ASCII 경로(예: `C:\work\boc-onboarding-tracker`)에 두는 것을 권장합니다.**

프로젝트 경로에 한글 등 비-ASCII 문자가 있으면(Windows MS949 환경) Gradle 테스트 워커가 클래스를 로드하지 못해
`ClassNotFoundException`으로 테스트가 실패합니다. 이를 피하려고 `build.gradle`은 **프로젝트 경로에 비-ASCII 문자가 있을 때만**
빌드 출력 디렉터리를 아래로 옮깁니다.

```
%USERPROFILE%\.gradle-build\boc-onboarding-tracker      (예: C:\Users\me\.gradle-build\boc-onboarding-tracker)
```

- 이 경우 프로젝트 안에 `build/` 폴더가 생기지 않습니다. 산출물(`libs/*.jar`), 테스트 리포트(`reports/tests/test/index.html`)는 위 경로에서 찾으세요.
- ASCII 경로로 옮기면 분기가 자동으로 비활성화되어 `./build`를 사용합니다. 별도 설정은 필요 없습니다.
- IntelliJ에서는 Gradle 프로젝트로 임포트하고 **Gradle JVM을 JDK 17 이상**으로 지정하세요.

---

## 3. 프로파일

| profile | 용도 | DB | Flyway | seed 적재 (`app.seed.enabled`) |
|---|---|---|---|---|
| `local` (기본) | 로컬 개발·운영 | MySQL/MariaDB (`boc_tracker`) | 활성 | **true** — 비어 있는 도메인만 적재 |
| `test` | 자동 테스트 | H2 in-memory (MySQL 모드) | 활성 | **false** — 비활성 |

- 프로파일을 지정하지 않으면 `local`이 적용됩니다 (`spring.profiles.default=local`).
- 공통: `ddl-auto=validate`, `open-in-view=false`. **스키마의 source of truth는 Flyway**입니다.
- 스키마를 바꿀 때는 기존 `V1`~`V3`를 **수정하지 말고** `V4__...sql`을 추가하세요.

---

## 4. Seed 데이터

`src/main/resources/seed/*.json`(14개)이 기동 시 적재됩니다. 프로젝트 루트의 `seed/`는 원본이며, 실행 시에는 **classpath 사본**을 읽습니다.
원본을 수정했다면 `src/main/resources/seed/`로 다시 복사하세요.

| 파일 | 도메인 |
|---|---|
| `milestones.json` | Milestone |
| `handover-sessions.json` | HandoverSession |
| `catchup-actions.json` | CatchupAction |
| `supply-docs.json` | SupplyDoc |
| `access-items.json` | AccessItem |
| `doc-reliability.json` | DocReliability |
| `menu-inventory.json` | MenuInventory |
| `mega-processes.json` | MegaProcess |
| `interfaces.json` | InterfaceLink |
| `issue-queue.json` | IssueQueue |
| `raci-assignments.json` | RaciAssignment |
| `review-logs.json` | ReviewLog (현재 빈 배열 `[]`) |
| `tech-debts.json` | TechDebt (`DOC/기술부채_관리대장_v0.1.xlsx` 24건) |
| `handover-questions.json` | HandoverQuestion (`DOC/KRAFTON_BOC_KEC_인수인계_질문지_v1.0.xlsx` 40건, 가설은 `hypothesis` 필드, 확인 대상의 개인 이름은 역할만 남김) |

`AnalysisCard`는 seed 파일이 없습니다.

**초기 사용자(`users.json`)** — 도메인 시드와 별개로 `UserSeedLoader`가 사용자 테이블이 비어 있을 때 5명을 적재합니다(초기 비밀번호 = 사용자 ID, BCrypt 해시로 저장, 첫 로그인 후 변경 안내).
저장소에는 업무 정보(소속·이름·투입일·업무 계정)만 두고, **연락처·개인 메일은 저장소 밖** `data/seed-private/users-contact.json`(`{ "loginId": { "phone": "...", "gmail": "..." } }`, `data/`는 git 제외)이 있을 때만 합쳐 넣습니다. 경로는 `app.seed.private-dir`로 바꿀 수 있습니다.

**적재 규칙**

1. **도메인별로** 테이블이 비어 있을 때만(`count()==0`) 적재합니다. 데이터가 하나라도 있으면 그 도메인은 skip → **재기동해도 중복 적재되지 않습니다.**
2. 각 JSON 배열을 `*Request` DTO로 읽어 검증(Bean Validation) 후 `service.create`로 저장합니다.
3. 도메인 단위 **all-or-nothing**: 한 건이라도 검증/저장에 실패하면 그 도메인은 통째로 롤백되고 `ERROR` 로그가 남습니다(다른 도메인과 기동은 계속).
4. JSON의 알 수 없는 필드는 무시하되 `WARN` 로그를 남깁니다. 파일이 없거나 빈 배열이면 조용히 skip합니다.
5. 특정 도메인만 다시 넣고 싶으면 해당 테이블을 비우고(`DELETE FROM milestone;`) 재기동하세요. 다른 도메인은 건드리지 않습니다.

---

## 5. 로컬(MySQL) 검증 절차

처음 한 번 아래 순서로 확인하세요. (자동 테스트는 H2 기준이므로 MySQL에서의 확인은 이 절차로 합니다.)

**(a) Flyway V1~V3 마이그레이션 성공**

```bash
./gradlew bootRun
```
로그에 다음이 보여야 합니다.
```
Migrating schema `boc_tracker` to version "1 - schema"
Migrating schema `boc_tracker` to version "2 - schema"
Migrating schema `boc_tracker` to version "3 - schema"
Successfully applied 3 migrations ...
```
```sql
USE boc_tracker;
SELECT version, description, success FROM flyway_schema_history;   -- 3행, success = 1
SHOW TABLES;                                                        -- 13개 도메인 테이블 + flyway_schema_history
```

**(b) seed 도메인별 건수 로그** (최초 기동)
```
[seed] Milestone 21건 적재 (milestones.json)
[seed] HandoverSession 24건 적재 (handover-sessions.json)
 ... (도메인별)
[seed] 도메인별 적재 건수: {Milestone=21, HandoverSession=24, CatchupAction=13, SupplyDoc=27, AccessItem=11,
  DocReliability=3, MenuInventory=9, MegaProcess=14, InterfaceLink=8, IssueQueue=4, RaciAssignment=9, ReviewLog=0}
```
`GET /api/summary`의 `total`이 위 건수와 같고, 한글이 깨지지 않는지(예: `GET /api/milestones/1`의 `name`) 확인합니다.

**(c) 재기동 시 skip**

앱을 종료했다가 다시 `./gradlew bootRun` 하면 모두 skip되어야 합니다.
```
[seed] Milestone skip — 기존 데이터 21건
 ...
[seed] 도메인별 적재 건수: {Milestone=0, HandoverSession=0, ...}
```
```sql
SELECT COUNT(*) FROM milestone;   -- 재기동 전후 동일 (21)
```

문제가 있으면: Flyway 실패 → `flyway_schema_history`의 실패 행 확인 후 DB를 재생성 / 한글 깨짐 → DB·테이블이 `utf8mb4`인지, JDBC URL에 `characterEncoding=UTF-8`이 있는지 확인.

---

## 6. API

- 공통 응답 래퍼: `ApiResponse<T> { success, data, error }`. 오류는 `error: { code, message, fieldErrors[] }`.
- 목록은 `?page=0&size=20&sort=id,asc` (기본 size 20, 정렬 `id ASC`)이며 `data`는 `{ content, page, size, totalElements, totalPages }`입니다.
- 상태 변경 `PATCH /{id}/status`의 body는 모두 `{ "status": "<허용값>" }`입니다 (SupplyDoc은 `supplyStatus`, MenuInventory는 `analysisStatus` 필드를 변경).
- 오류 코드: 검증 실패 `400 VALIDATION_ERROR`, 형식 오류 `400 BAD_REQUEST`, 미존재 `404 NOT_FOUND`, 중복/제약 위반 `409 CONFLICT`, 그 외 `500 INTERNAL_ERROR`.
- Swagger UI에는 한글 태그 19개(도메인 15 + 사용자 + 인증 + 메타 + 요약)가 아래 표 순서대로 표시되고, 엔드포인트마다 공통 응답코드
  (목록 200/400 · 단건 200/404 · 생성 201/400/409 · 수정 200/400/404/409 · 상태변경 200/400/404 · 삭제 204/404)가 문서화됩니다.
  총 127개 엔드포인트이며, `OpenApiDocsTest`가 태그·응답코드·아래 표와의 일치를 검증합니다.
- 도메인 공통 엔드포인트: `GET /api/{resource}` · `GET /{id}` · `POST` · `PUT /{id}` · `DELETE /{id}` (+ status가 있는 도메인은 `PATCH /{id}/status`).

| # | 도메인 | 경로 | 목록 필터 | 필수 필드 | PATCH `/status` |
|---|---|---|---|---|---|
| 1 | Milestone | `/api/milestones` | status, phase, owner, q, overdue | phase, code, name | O |
| 2 | HandoverSession | `/api/handover-sessions` | status, week, category, person, q, overdue | week, category, topic | O |
| 3 | CatchupAction | `/api/catchup-actions` | status, timing | category, action | O |
| 4 | SupplyDoc | `/api/supply-docs` | supplyStatus, category, owner, q, overdue | category, name | O (`supplyStatus`) |
| 5 | AccessItem | `/api/access-items` | status, category, owner, q, overdue | category, target | O |
| 6 | MegaProcess | `/api/mega-processes` | l0 | l0, l1 | - |
| 7 | InterfaceLink | `/api/interface-links` | devCheck | systemName | - |
| 8 | DocReliability | `/api/doc-reliabilities` | grade, freshness | docName | - |
| 9 | MenuInventory | `/api/menu-inventories` | status(=analysisStatus), priority, domain, devCheck | menu | O (`analysisStatus`) |
| 10 | IssueQueue | `/api/issue-queues` | status, type | item | O |
| 11 | RaciAssignment | `/api/raci-assignments` | status | domainGroup | O |
| 12 | ReviewLog | `/api/review-logs` | status | round (정수 ≥ 1) | O |
| 13 | AnalysisCard | `/api/analysis-cards` | status, domain | menuName | O |
| - | Meta | `GET /api/meta/enums` | - | - | - |
| - | Summary | `GET /api/summary` | - | - | - |

### 마일스톤 추가 API

| 엔드포인트 | 설명 |
|---|---|
| `GET /api/milestones/phases` | 단계 목록(첫 등장 순) + 단계별 전체/완료 건수 |
| `PUT /api/milestones/order` | 단계 내 순서(No.) 변경(드래그앤드롭) `{phase, ids[]}` — 그 단계 전체 id 를 새 순서로. 목록이 다르면 400 |
| `PATCH /api/milestones/phases` | 단계명 일괄 변경 `{from,to}` (이미 있는 이름이면 병합) |
| `GET /api/milestones/owners` | 입력된 담당자 목록(자동완성용) |
| `PUT /api/milestones/{id}/check-items` | 체크리스트 전체 교체(순서=정렬, 누락 항목 삭제) |
| `PATCH /api/milestones/{id}/check-items/{itemId}` | 항목 완료 토글 `{done}` |
| `POST /api/milestones/{id}/check-items/generate` | 체크포인트(`·`/줄바꿈/`;` 구분)로 체크리스트 생성 (이미 있으면 409) |

비즈니스 규칙: 완료 전환 시 `completedDate` 자동 기록(이탈 시 제거) · `overdue` = 목표일 경과 & 미완료 ·
`blockedBy` = 미완료 선행(경고용, 전이는 막지 않음) · 선행 순환 `400 BUSINESS_RULE` ·
선행으로 참조 중인 항목의 삭제 `409 CONFLICT` · 코드 중복 `409`.

**코드 체계**: 코드 = `{단계키}.{순번}` (예: `P0.1`, `HO.2`, `P1.5`). 단계명 접두(`P0.`·`HO.`·`P1.` …)가 단계키이며 접두가 없으면 `X`.
생성 시 서버가 자동 부여(같은 단계키 코드의 최대 순번 + 1, 삭제된 번호는 재사용하지 않음)하고 **이후 변경 불가**(`400`) —
선행 조건·링크가 코드를 참조하기 때문. 단계 이름을 바꾸거나 다른 단계로 옮겨도 기존 코드는 유지된다.
**No.**(`no`)는 코드와 별개인 단계 내 표시 순서(1부터 연속)이며 드래그로 바뀐다. 새 항목/단계 이동은 그 단계 맨 뒤.
`A~B` 범위 선행은 저장 시점의 **화면 표시 순서** 기준으로 풀린다.
(V8 마이그레이션이 기존 `M0.x`~`M3.x` → `P0.x`~`P3.x` 로 변환, `HO.x` 는 유지하고 선행 조건 문자열·seed JSON 도 함께 갱신)

### ② 수급 산출물 첨부 파일 (실제 수급 파일 업로드)

| 엔드포인트 | 설명 |
|---|---|
| `POST /api/supply-docs/{id}/files` | multipart `file` 파트 업로드. 갱신된 산출물(`files` 포함)을 `201` 로 반환 |
| `GET /api/supply-docs/{id}/files/{fileId}` | 다운로드(`attachment`, `nosniff`). 다른 산출물의 파일 id 는 `404` |
| `DELETE /api/supply-docs/{id}/files/{fileId}` | 파일 삭제(`204`) |

- 본문은 디스크(`app.storage.dir`, 기본 `./data/uploads`, 환경변수 `APP_STORAGE_DIR`), 메타데이터(원본명·크기·타입·SHA-256)는 DB(`supply_doc_file`, V11). 저장 이름은 서버가 만든 UUID 라 사용자가 준 파일명이 경로에 쓰이지 않는다. `data/` 는 git 에 올리지 않는다.
- 규칙: 파일당 50MB(초과 `413`) · 산출물당 20개 · 빈 파일/실행 파일(`exe bat cmd com msi scr sh ps1 vbs jar dll js`) `400` · 같은 내용(SHA-256) 중복 `409`. 산출물을 삭제하면 첨부도 함께 삭제된다. DB 롤백 시 방금 쓴 파일도 지운다.
- 파일이 첨부돼 있으면 "수령했지만 위치가 비어 있습니다" 경고를 내지 않는다. 계정·토큰 등 민감정보가 담긴 파일은 올리지 않는다.
- 화면: 수급 상세 팝업의 "첨부 파일" 구역(끌어놓기/선택 업로드, 다운로드, 삭제). 미요청/요청 상태에서 올리면 "수령"으로 바꿀지 묻는다.

### 인증 · 사용자 · 감사(Audit)

- **로그인 필수**: `/api/auth/login`을 제외한 모든 `/api/**`는 세션 쿠키(HttpOnly, SameSite=Lax, 8시간)가 필요하며 없으면 `401 UNAUTHORIZED`입니다. 같은 ID로 5회 연속 실패하면 1분간 `429 TOO_MANY_ATTEMPTS`. 서버를 재시작하면 세션이 사라져 다시 로그인해야 합니다. (`app.auth.enabled=false`로 끄면 검사 없이 동작하며 감사 값은 `system` — test 프로파일 기본)
- 메뉴별 권한은 없습니다(로그인한 내부 사용자는 모든 메뉴 사용). 비밀번호는 BCrypt 해시만 저장하고 어떤 응답에도 포함하지 않습니다.

| 엔드포인트 | 설명 |
|---|---|
| `POST /api/auth/login` · `POST /api/auth/logout` · `GET /api/auth/me` | 로그인 · 로그아웃 · 내 정보(로그인 확인) |
| `PUT /api/auth/password` | 내 비밀번호 변경(현재 비밀번호 확인, 6자 이상, ID와 달라야 함) |
| `GET/POST /api/users` · `GET/PUT/DELETE /api/users/{id}` | 사용자 관리. 목록은 비페이징 전체(담당자 선택 후보로도 사용). ID는 등록 후 변경 불가, 본인·마지막 사용자는 삭제 불가 |
| `POST /api/users/{id}/password-reset` | 비밀번호를 초기값(로그인 ID)으로 되돌림 |

- **감사 컬럼**: 모든 테이블에 `created_at/created_by/updated_at/updated_by`가 있고 응답에 `createdBy`·`updatedBy`(로그인 ID)가 실립니다. 등록·수정 시 로그인 사용자로 자동 기록되며(시드·시스템 작업은 `system`), 화면 상세 팝업 맨 아래에 이름·일시로 표시됩니다.
- **삭제 이력**: 삭제는 행이 사라지므로 `audit_log` 테이블에 (엔티티 종류, ID, 삭제자, 시각)을 남깁니다(조회 API 없음, DB에서 확인).
- 각 화면의 담당자·확인자·인수자 선택 목록은 사용자 관리의 사용자 이름입니다(값은 이름 문자열로 저장).

### 기술부채 `/api/tech-debts` · 인수인계 질의 `/api/handover-questions`

두 엑셀(`DOC/기술부채_관리대장_v0.1.xlsx`, `DOC/KRAFTON_BOC_KEC_인수인계_질문지_v1.0.xlsx`)을 시스템화한 메뉴입니다. 수급·접근권한과 같은 방식(불변 코드 · 우선순위 그룹 내 No. · 드래그 순서 · 팝업 조회/편집)입니다.

| 엔드포인트(두 리소스 공통) | 설명 |
|---|---|
| `GET` · `POST` · `GET/PUT/DELETE /{id}` · `PATCH /{id}/status` | 표준 CRUD + 상태 변경 |
| `PUT /order` · `PATCH /groups` | 우선순위 그룹 내 순서 변경 · 그룹명 일괄 변경(허용된 우선순위로만) |
| `GET /export` | 목록과 같은 필터를 적용한 엑셀(.xlsx) 내려받기. 원본 양식의 열 순서, 값은 모두 문자열(수식 실행 없음) |
| `POST` · `GET` · `DELETE /{id}/files[/{fileId}]` | 증빙·근거 파일 첨부(수급 산출물과 같은 규칙, 공용 `attachment` 테이블 · V12) |

- **기술부채**: 코드 `KEC-01`/`BOC-01`/`CMN-01`(시스템 접두 + 순번, 자동 부여·불변). 우선순위 P1~P4 가 그룹, 상태 `미착수·확인중·진행중·검증중·완료·보류`. **완료로 바꾸려면 담당이 필요**(400), 완료일은 서버가 기록, 완료에서 벗어나면 제거. 목표 시점이 지난 미완료(완료·보류 제외)는 `overdue`. 목록 필터: `status, priority, system, owner, q, overdue`. "최종 업데이트"는 `updatedAt`.
- **인수인계 질의**: 코드 `Q-01`…(자동 부여·불변). 우선순위 높음/중/낮음이 그룹, 상태 `미확인·진행중·확인완료·보류`. **확인완료로 바꾸려면 확인 답변·확인자가 필요**(400), 확인일은 서버가 기록(직접 지정 가능)하고 다른 상태로 되돌리면 제거. 엑셀의 "답변 샘플 [예시·가설]"은 사실이 아니므로 `hypothesis`(가설) 필드로 분리해 화면에 "사실 아님"으로 표기한다. 목록 필터: `status, priority, kind, askTo, q`.
- 두 메뉴는 기존 메뉴와 확실한 연관이 있는 항목이 없어 링크 필드를 두지 않았다(필요해지면 새 메뉴 → 기존 메뉴 방향으로만 추가).
- 민감정보(계정·비밀번호·토큰·개인정보)는 입력하지 않는다 — 시드의 개인 이름은 역할로 바꿔 적재했다.

### ②수급 산출물 · ③접근권한 · ⑤인수인계 (마일스톤과 같은 수준으로 고도화)

| 항목 | 규칙 |
|---|---|
| 코드(불변) | 수급 `{구분 알파벳}.{n}` (A.1, B.4 …, 접두 없으면 `S`) · 접근 `AC.{n}` · 인수인계 `{주차}.{n}` (W1.2 …). 생성 시 서버 자동 부여, 변경 시 `400` |
| No.(순서) | 그룹(구분/주차) 내 표시 순서. 새 항목·그룹 이동은 맨 뒤, 삭제 시 재정렬. `PUT /{base}/order {group, ids[]}` 로 드래그 순서 저장 |
| 그룹명 변경 | 수급·접근: `PATCH /{base}/groups {from,to}` (병합 가능, 코드 유지). 인수인계 주차(W1~W4)는 고정 |
| 수급 | 요청/수령일 자동 기록 · `overdue`(필요일 경과 & 미수령) · 경고(수령인데 존재=없음/위치 비어 있음/최신성 미확인/담당 없음) |
| 접근 | 신청/승인일 보정 · 일자-상태 모순 `400`(승인일은 승인 상태에서만, 신청일은 미신청 제외, 승인일≥신청일) · `leadTimeDays` · 용도에 `접근 전제`가 있는 항목이 미승인이면 다른 접근에 경고 · 민감정보 입력 금지 |
| 인수인계 | 완료일 자동 기록 · `overdue`(예정일 경과 & 미완료) · `blockers`(미수령 수급·미승인 접근·"전 세션 완료"인 마무리 세션의 미완료 세션 — 차단하지 않고 경고) |

**메뉴·문서 링크** — 텍스트로만 있던 참조를 FK로 정규화(원문은 유지):
- 인수인계 세션 → 마일스톤(`milestoneCode`, 예: W1.* → HO.1), 선행 수급(`supplyDocCodes`), 선행 접근(`accessItemCodes`). PUT 은 전체 교체, 없는 코드는 `400`.
- 참조 중인 수급·접근·마일스톤의 삭제는 `409`(참조하는 세션 코드를 알려 줌). 수급·접근 응답의 `requiredBySessions` 가 역방향 링크.
- 마일스톤 응답의 `links`: 산출물의 시트 기호가 가리키는 메뉴의 **실제 진행 현황** — ② 수령/해당없음 n/전체, ③ 승인 n/전체, ⑤ 연결 세션 완료 n/전체(연결 세션이 없으면 예정일 확정 n/전체).
- `GET /api/summary/links`: 끊어진 링크 점검(세션의 "수급:/접근:" 텍스트에 연결 FK 없음, 마일스톤 없는 세션, 존재하지 않는 선행 코드, 세션 없는 HO 마일스톤). 대시보드에 표시.
- 프런트 시트 기호 매핑은 `frontend/src/links.tsx`(②③⑤ 활성, 나머지는 "준비 중" 칩).
- seed 적재 순서: 마일스톤 → 수급/접근 → 인수인계(코드로 참조). V9/V10 이 기존 DB의 코드·순서·연결을 백필한다.

### 메타: `GET /api/meta/enums`

통제 어휘(드롭다운) 허용값을 `{ 그룹명: [허용값...] }`으로 돌려줍니다. 그룹: `milestoneStatus, handoverStatus, ktStage, week, category,
catchupTiming, supplyStatus, existence, freshness, accessStatus, grade, codeFit, priority, generation, devCheck, issueType, issueStatus,
techDebtPriority, techDebtStatus, techDebtSystem, questionPriority, questionStatus`.
`ktStage`는 `설명·병행`처럼 `·`로 이은 복합 표기도 허용됩니다.

### 요약: `GET /api/summary`

도메인별 `{ total, byStatus }` 구조입니다. 허용 상태값은 0건이어도 코드값 표 순서대로 포함됩니다.
`menu`·`techDebt`·`handoverQuestion`은 `byPriority`(menu 상/중/하, 기술부채 P1~P4, 질의 높음/중/낮음. 미지정 행이 있으면 `미지정`)가 추가되고, 상태가 없는 `megaProcess`/`interfaceLink`/`docReliability`는 `total`만 있습니다.
도메인당 group-by 1쿼리(menu·techDebt·handoverQuestion은 2쿼리), 총 18쿼리로 계산합니다.

```json
{
  "success": true,
  "data": {
    "milestone": { "total": 21, "byStatus": { "미착수": 21, "진행중": 0, "완료": 0, "보류": 0 } },
    "menu": {
      "total": 9,
      "byStatus": { "미착수": 9, "진행중": 0, "완료": 0, "보류": 0 },
      "byPriority": { "상": 0, "중": 0, "하": 0, "미지정": 9 }
    },
    "megaProcess": { "total": 14 }
  }
}
```
(일부 도메인만 발췌)

---

## 7. 통제 어휘(코드값) 관리

- 허용값의 단일 출처는 `common/meta/CodeValues`이고 `GET /api/meta/enums`가 이를 노출합니다 (CLAUDE.md 4장 '코드값' 표와 동일).
- DTO의 `@EnumValue(allowed = {...})`는 어노테이션 제약상 **리터럴**로 적습니다.
- `CodeValuesDriftTest`가 모든 DTO의 `@EnumValue` 리터럴이 `CodeValues`와 **순서까지 일치**하는지 검증하고, 등록되지 않은 `@EnumValue` 필드가 있으면 실패합니다.
  새 코드값/도메인을 추가하면 이 테스트의 `bindings()`에 한 줄 추가하세요.

---

## 8. 패키지 구조

```
com.grovesoft.boctracker
├─ common/        web(ApiResponse, ErrorResponse, GlobalExceptionHandler, PageResponse)
│                 entity(BaseEntity, StatusCount, GroupCount) · validation(@EnumValue) · meta(CodeValues, /api/meta)
│                 docs(ApiTags, OpenApiConfig, *Responses 공통 응답코드 애너테이션)
├─ milestone/ handover/ catchup/ supplydoc/ access/ docreliability/ menu/
│  megaprocess/ interfacelink/ analysiscard/ issue/ raci/ review/ techdebt/ handoverquestion/
│                 # 도메인 = Entity · Repository · Service · Mapper(수동) · Controller · dto/(Request, Response, StatusRequest)
├─ common/attachment, common/export   # 공용 첨부 파일(소유자 유형별) · 의존성 없는 xlsx 작성기
├─ summary/       # GET /api/summary
└─ seed/          # SeedDataLoader · SeedTargetRegistry(파일↔도메인 매핑) · SeedTarget
```

레이어 규칙: `Controller → Service → Repository`, Entity는 API에 직접 노출하지 않음(항상 DTO), 생성자 주입, 트랜잭션은 Service(조회는 `readOnly`).

## 9. 참고

- **컬럼명**: MySQL 예약어와 충돌하는 필드는 컬럼명만 바꿨습니다 — `usage → usage_desc`, `procedure → procedure_desc`, `data → data_desc`, `round → round_no`. 엔티티 필드/JSON 키는 CLAUDE.md 그대로입니다.
- **관계(FK)**: v1은 느슨한 문자열 참조입니다(예: `MenuInventory.knownIssue`는 IssueQueue의 `item` 식별 문자열). 정규화 FK는 v2 과제.
- **민감정보**: 계정 비밀번호·토큰·개인정보는 저장하지 않습니다.

---

# 프런트엔드 (`frontend/`)

React 18 + TypeScript + Vite 5. 현재 화면: 대시보드(+링크 점검), 마일스톤, ② 수급 산출물, ③ 접근권한, ⑤ 인수인계(KT).
항목 단위 링크 칩(세션의 마일스톤·선행 수급/접근, 수급·접근의 필요 세션, 관련 마일스톤)은 페이지를 이동하지 않고 현재 화면 위에 **확인 팝업**(`src/peek/`)을 쌓는다. 팝업은 조회와 **상태 변경**까지만 허용하고(선행 미충족·체크리스트 확인창 동일), 변경하면 뒤쪽 화면이 다시 불러온다. Esc 는 맨 위 팝업만 닫고, "…에서 열기" 로 해당 화면(`?code=`)에 이동할 수 있다. 메뉴 전체를 가리키는 칩(`② 수급 산출물 0/27`)은 이동한다.
메뉴 구조는 `src/nav.tsx` 레지스트리 한 곳에서 정의한다(라우트·좌측 메뉴·브레드크럼·시트 기호 링크 공용, 엑셀 시트 순서, 화면이 있는 항목만 메뉴에 노출). 새 화면은 `component` 를 채우면 메뉴·라우트·링크 칩이 함께 활성화된다. 레이아웃은 상단 바(브레드크럼) + 좌측 메뉴(900px 이하 드로어), 색·간격은 `styles.css` 의 CSS 변수. 화면 모드는 라이트/다크 2단계 토글(선택 전에는 OS 설정을 따르고, 누르면 선택을 저장). 그룹 띠 색은 마일스톤 단계(P0·HO·P1..)만 고유색이고 수급 구분·인수인계 주차 등은 공통 파랑. 기술부채·인수인계 질의는 우선순위 색(높을수록 붉게, 낮을수록 푸르게)을 쓴다.
②③⑤는 `src/resource/` 의 공통 화면(`ManagedPage`/`ResourceDialog`)을 설정만 달리해 만들며, 다음 도메인도 같은 방식으로 추가한다.

마일스톤 화면: 단계별 색상 그룹 · No./코드 열 · 드래그앤드롭(같은 단계 내, ▲▼ 버튼 병행) 순서 변경 · 상단 요약 칩(상태/지연) · 검색/단계/담당 필터 · 팝업(보기/편집/추가/삭제) ·
체크리스트 진척 · 선행/산출물 링크 칩(`src/links.tsx` 에 시트기호 ②~⑬ → 라우트 매핑, 화면이 생기면 `ready: true`) ·
선행 미완료/체크리스트 미완료 시 상태 변경 확인창 · 단계 이름 일괄 변경.
딥링크: `/milestones?code=P0.1`, `/milestones?new=1&phase=...`.

```bash
# 백엔드(8080)를 먼저 기동한 뒤
cd frontend
npm install
npm run dev      # http://localhost:5173  (/api 는 Vite 프록시로 8080에 전달 → CORS 설정 불필요)
npm run build    # 타입체크 + 프로덕션 빌드(dist/)
```

Node 18 이상. 나머지 도메인 화면은 `src/pages/Milestones.tsx` 패턴으로 확장합니다.
