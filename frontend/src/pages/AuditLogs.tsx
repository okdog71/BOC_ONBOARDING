import { Fragment, useState, type FormEvent } from 'react';
import { api, query, type Page } from '../api';
import { State } from '../components';
import { useAsync } from '../hooks';
import { useUsers } from '../users';

interface Change {
  field: string;
  from: string | null;
  to: string | null;
}
interface AuditLog {
  id: number;
  actedAt: string;
  actor: string;
  actorName: string | null;
  action: 'CREATE' | 'UPDATE' | 'DELETE';
  domain: string | null;
  entityId: number;
  targetCode: string | null;
  targetLabel: string | null;
  changes: Change[];
  clientIp: string | null;
  request: string | null;
}

const ACTIONS: Record<string, string> = { CREATE: '등록', UPDATE: '수정', DELETE: '삭제' };
const PAGE_SIZE = 50;

/** 필드(영문 속성명) → 화면 표시 이름. 없으면 속성명 그대로 */
const FIELD_LABELS: Record<string, string> = {
  phase: '단계', code: '코드', name: '이름', goal: '목표', checkpoint: '체크포인트', output: '산출물', precondition: '선행 조건',
  owner: '담당', durationWeeks: '소요 기간(주)', status: '상태', doneCriteria: '완료 기준', note: '비고', targetDate: '목표일',
  completedDate: '완료일', sortOrder: 'No.', title: '제목', done: '완료', category: '구분', expectedContent: '예상 내용',
  usagePurpose: '활용 목적', existence: '존재 여부', location: '위치', freshness: '최신성', supplyStatus: '수급 상태',
  target: '대상', usage: '용도', envScope: '환경 범위', procedure: '절차', requestedDate: '신청일', approvedDate: '승인일',
  week: '주차', domain: '도메인', topic: '주제', ktStage: 'KT 단계', handoverFrom: '인계자', handoverTo: '인수자', method: '방식',
  plannedDate: '예정일', priority: '우선순위', system: '시스템', area: '영역', summary: '내용 요약', impact: '리스크·영향', basis: '근거',
  remedy: '대응 방안', confirmWith: '확인 필요처', verifyCriteria: '검증 기준', kind: '구분', relatedNode: '관련 I/F·노드',
  question: '질문', askTo: '확인 대상', hypothesis: '가설', reflectPlan: '구성도 반영 방법', answer: '확인 답변',
  confirmedBy: '확인자', confirmedDate: '확인일', loginId: '사용자 ID', affiliation: '소속', joinDate: '투입일', leaveDate: '철수일',
  kraftonAccount: '크래프톤 계정', phone: '연락처', groveMail: 'Grove 메일', gmail: 'G메일', ip: 'IP', vpn: 'VPN', bocAccount: 'BOC 계정',
  kecAccount: 'KEC 계정', aws: 'AWS', password: '비밀번호', mustChangePassword: '초기 비밀번호 상태', originalName: '파일명',
  fileName: '파일명', sizeBytes: '크기', contentType: '형식', sha256: '해시', ownerType: '소유 구분', ownerId: '소유 ID',
  systemName: '시스템명', direction: '방향', interfaceName: '인터페이스명', auth: '인증', data: '데이터', devCheck: 'dev 확인',
  docName: '문서명', source: '출처', lastUpdated: '최종 수정', codeFit: '코드 일치도', grade: '등급', verifyMethod: '검증 방법', gap: '차이',
};
const label = (f: string) => FIELD_LABELS[f] ?? f;

const when = (iso: string) => iso.replace('T', ' ').slice(0, 19);

/** 요약 한 줄: 수정은 "필드: 전 → 후", 등록·삭제는 "필드: 값" */
function line(a: AuditLog, c: Change) {
  if (a.action === 'UPDATE') {
    return (
      <>
        <b>{label(c.field)}</b>: <span className="old">{c.from || '(없음)'}</span> → <span className="new">{c.to || '(없음)'}</span>
      </>
    );
  }
  return (
    <>
      <b>{label(c.field)}</b>: {a.action === 'CREATE' ? c.to : c.from}
    </>
  );
}

interface Filters {
  from: string;
  to: string;
  domain: string;
  actor: string;
  action: string;
  code: string;
  ip: string;
  q: string;
}
const EMPTY: Filters = { from: '', to: '', domain: '', actor: '', action: '', code: '', ip: '', q: '' };

/** 변경 이력: 모든 도메인의 등록·수정·삭제를 누가·언제·어디서·무엇을·어떻게 했는지 조회 */
export default function AuditLogs() {
  const users = useUsers();
  const domains = useAsync(() => api.get<string[]>('/api/audit-logs/domains'), []).data ?? [];
  const [draft, setDraft] = useState<Filters>(EMPTY);
  const [filters, setFilters] = useState<Filters>(EMPTY);
  const [page, setPage] = useState(0);
  const [open, setOpen] = useState<number>();

  const list = useAsync(
    () => api.get<Page<AuditLog>>('/api/audit-logs' + query({ ...filters, page, size: PAGE_SIZE })),
    [filters, page],
  );
  const rows = list.data?.content ?? [];
  const total = list.data?.totalElements ?? 0;
  const pages = list.data?.totalPages ?? 0;
  const active = Object.values(filters).some(Boolean);

  const set = (k: keyof Filters, v: string) => setDraft((d) => ({ ...d, [k]: v }));
  const apply = (next: Filters) => {
    setFilters(next);
    setPage(0);
    setOpen(undefined);
  };
  const submit = (e: FormEvent) => {
    e.preventDefault();
    apply(draft);
  };
  // 선택 목록·날짜는 바꾸는 즉시 적용
  const pick = (k: keyof Filters, v: string) => {
    const next = { ...draft, [k]: v };
    setDraft(next);
    apply(next);
  };

  return (
    <>
      <h2 className="page-title">변경 이력</h2>
      <p className="muted small">모든 메뉴의 등록·수정·삭제가 자동으로 기록됩니다(누가 · 언제 · 어디서(접속 IP) · 무엇을 · 어떻게). 이 화면에서는 수정·삭제할 수 없습니다.</p>

      <form className="toolbar audit-filters" onSubmit={submit}>
        <label className="flt">
          기간
          <input type="date" value={draft.from} max={draft.to || undefined} onChange={(e) => pick('from', e.target.value)} />
          <span aria-hidden="true">~</span>
          <input type="date" value={draft.to} min={draft.from || undefined} onChange={(e) => pick('to', e.target.value)} />
        </label>
        <label className="flt">
          도메인
          <select value={draft.domain} onChange={(e) => pick('domain', e.target.value)}>
            <option value="">전체</option>
            {domains.map((d) => (
              <option key={d}>{d}</option>
            ))}
          </select>
        </label>
        <label className="flt">
          사용자
          <select value={draft.actor} onChange={(e) => pick('actor', e.target.value)}>
            <option value="">전체</option>
            {users.map((u) => (
              <option key={u.loginId} value={u.loginId}>
                {u.name} ({u.loginId})
              </option>
            ))}
            <option value="system">시스템</option>
          </select>
        </label>
        <label className="flt">
          작업
          <select value={draft.action} onChange={(e) => pick('action', e.target.value)}>
            <option value="">전체</option>
            {Object.entries(ACTIONS).map(([k, v]) => (
              <option key={k} value={k}>
                {v}
              </option>
            ))}
          </select>
        </label>
        <input className="narrow-in" placeholder="대상 코드 (예: KEC-01)" value={draft.code} onChange={(e) => set('code', e.target.value)} />
        <input className="narrow-in" placeholder="접속 IP" value={draft.ip} onChange={(e) => set('ip', e.target.value)} />
        <input type="search" className="search" placeholder="대상 이름·변경 내용 검색" value={draft.q} onChange={(e) => set('q', e.target.value)} />
        <button type="submit" className="primary">
          검색
        </button>
        {active && (
          <button
            type="button"
            onClick={() => {
              setDraft(EMPTY);
              apply(EMPTY);
            }}
          >
            필터 초기화
          </button>
        )}
        <span className="spacer" />
        <span className="muted small">{total.toLocaleString()}건</span>
      </form>

      <State loading={list.loading && !list.data} error={list.error}>
        <div className="table-scroll">
          <table className="ms-table audit-table">
            <thead>
              <tr>
                <th>일시</th>
                <th>사용자</th>
                <th>작업</th>
                <th>도메인</th>
                <th>대상</th>
                <th>변경 내용</th>
                <th>접속 · 요청</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((a) => {
                const expanded = open === a.id;
                const shown = expanded ? a.changes : a.changes.slice(0, 2);
                return (
                  <Fragment key={a.id}>
                    <tr className="ms-row clickable" onClick={() => setOpen(expanded ? undefined : a.id)}>
                      <td className="nowrap small">{when(a.actedAt)}</td>
                      <td className="nowrap">
                        {a.actorName ?? a.actor}
                        <div className="sub">{a.actor}</div>
                      </td>
                      <td className="nowrap">
                        <span className={`act act-${a.action}`}>{ACTIONS[a.action]}</span>
                      </td>
                      <td className="nowrap small">{a.domain}</td>
                      <td>
                        {a.targetCode && <span className="code-tag">{a.targetCode}</span>} {a.targetLabel}
                        {!a.targetCode && !a.targetLabel && <span className="muted">#{a.entityId}</span>}
                      </td>
                      <td className="small">
                        {shown.map((c) => (
                          <div key={c.field} className="chg">
                            {line(a, c)}
                          </div>
                        ))}
                        {!expanded && a.changes.length > 2 && <span className="muted">외 {a.changes.length - 2}개 (눌러서 펼치기)</span>}
                        {a.changes.length === 0 && <span className="muted">-</span>}
                      </td>
                      <td className="small">
                        <div>{a.clientIp ?? '-'}</div>
                        <div className="sub">{a.request}</div>
                      </td>
                    </tr>
                  </Fragment>
                );
              })}
              {rows.length === 0 && (
                <tr>
                  <td colSpan={7} className="muted empty">
                    {active ? '조건에 맞는 이력이 없습니다.' : '기록된 이력이 없습니다.'}
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
        {pages > 1 && (
          <div className="pager">
            <button disabled={page === 0} onClick={() => setPage(page - 1)}>
              이전
            </button>
            <span>
              {page + 1} / {pages}
            </span>
            <button disabled={page + 1 >= pages} onClick={() => setPage(page + 1)}>
              다음
            </button>
          </div>
        )}
      </State>
    </>
  );
}
