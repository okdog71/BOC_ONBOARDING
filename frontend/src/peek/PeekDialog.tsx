import { useEffect, useRef, useState, type ReactNode } from 'react';
import { Link } from 'react-router-dom';
import { api, type Page } from '../api';
import { StatusBadge, StatusSelect } from '../components';
import { useAsync, useEnums } from '../hooks';
import { ItemChip, md, orDash } from '../resource/chips';
import FilesSection from '../resource/FilesSection';
import { Banner, DialogFrame, Facts, FootBar, KV, KVGrid, Section } from '../ui/detail';
import type { AccessItem, HandoverSession, Milestone, SupplyDoc } from '../types';
import { usePeek, type PeekKind } from './context';

/** 종류별로 팝업에 무엇을 어떻게 보여 줄지 정의한다. */
interface Spec<T> {
  label: string;
  base: string;
  route: string;
  enumKey: string;
  statuses: string[];
  titleOf: (r: T) => string;
  statusOf: (r: T) => string;
  /** 상태 변경 전 확인 메시지(없으면 null) */
  confirm?: (r: T, next: string) => string | null;
  warnings?: (r: T) => string[];
  facts: (r: T) => { label: string; value: ReactNode; tone?: 'ok' | 'warn' | 'danger' | 'muted' }[];
  fields: (r: T) => [string, ReactNode][];
  links: (r: T) => ReactNode;
}

const date = (d: string | null, overdue?: boolean) =>
  d ? <span className={overdue ? 'overdue' : ''}>{overdue ? `${d} ⚠` : d}</span> : <span className="muted">-</span>;

const SPECS: { [K in PeekKind]: Spec<K extends 'milestone' ? Milestone : K extends 'supply' ? SupplyDoc : K extends 'access' ? AccessItem : HandoverSession> } = {
  milestone: {
    label: '마일스톤',
    base: '/api/milestones',
    route: '/milestones',
    enumKey: 'milestoneStatus',
    statuses: ['미착수', '진행중', '완료', '보류'],
    titleOf: (r) => r.name,
    statusOf: (r) => r.status,
    confirm: (r, next) => {
      if ((next === '진행중' || next === '완료') && r.blockedBy.length > 0)
        return `선행 마일스톤이 아직 완료되지 않았습니다: ${r.blockedBy.join(', ')}\n그래도 "${next}"로 변경할까요?`;
      if (next === '완료' && r.progress.total > r.progress.done)
        return `체크리스트 ${r.progress.total - r.progress.done}개가 미완료입니다.\n그래도 "완료"로 변경할까요?`;
      return null;
    },
    facts: (r) => [
      { label: '상태', value: r.status, tone: r.status === '완료' ? 'ok' : undefined },
      { label: '목표일', value: r.targetDate ?? '미정', tone: r.overdue ? 'danger' : r.targetDate ? undefined : 'muted' },
      { label: '체크리스트', value: r.progress.total ? `${r.progress.done}/${r.progress.total}` : '없음', tone: r.progress.total ? undefined : 'muted' },
      { label: '담당', value: r.owner ?? '미지정', tone: r.owner ? undefined : 'warn' },
    ],
    fields: (r) => [
      ['단계', r.phase],
      ['목표', orDash(r.goal)],
      ['체크포인트', orDash(r.checkpoint)],
      ['산출물', orDash(r.output)],
      ['담당', r.owner ?? <span className="muted">미지정</span>],
      ['목표일', date(r.targetDate, r.overdue)],
      ['완료일', date(r.completedDate)],
      ['진척', r.progress.total ? `체크리스트 ${r.progress.done}/${r.progress.total}` : <span className="muted">체크리스트 없음</span>],
      ['완료 기준', orDash(r.doneCriteria)],
    ],
    links: (r) => (
      <>
        <KV label="선행" wide>
          {r.predecessorCodes.length === 0 && <span className="muted">없음</span>}
          {r.predecessorCodes.map((c) => (
            <ItemChip key={c} peek={{ kind: 'milestone', code: c }} ready={!r.blockedBy.includes(c)} label={c} title="선행 마일스톤" />
          ))}
        </KV>
      </>
    ),
  },
  supply: {
    label: '수급 산출물',
    base: '/api/supply-docs',
    route: '/supply-docs',
    enumKey: 'supplyStatus',
    statuses: ['미요청', '요청', '수령', '해당없음'],
    titleOf: (r) => r.name,
    statusOf: (r) => r.supplyStatus,
    warnings: (r) => r.warnings,
    facts: (r) => [
      { label: '수급 상태', value: r.supplyStatus, tone: r.ready ? 'ok' : r.overdue ? 'danger' : undefined },
      { label: '필요일', value: r.targetDate ?? '미정', tone: r.overdue ? 'danger' : r.targetDate ? undefined : 'muted' },
      { label: '담당', value: r.owner ?? '미지정', tone: r.owner ? undefined : 'warn' },
      { label: '첨부', value: r.files.length > 0 ? `${r.files.length}개` : '없음', tone: r.files.length > 0 ? 'ok' : 'muted' },
    ],
    fields: (r) => [
      ['구분', r.category],
      ['기대 내용', orDash(r.expectedContent)],
      ['용도', orDash(r.usagePurpose)],
      ['존재 · 최신성', `${r.existence ?? '-'} · ${r.freshness ?? '-'}`],
      ['위치', orDash(r.location)],
      ['담당', r.owner ?? <span className="muted">미지정</span>],
      ['필요일', date(r.targetDate, r.overdue)],
      ['요청 → 수령', `${r.requestedDate ?? '-'} → ${r.receivedDate ?? '-'}`],
    ],
    links: (r) => (
      <>
        <KV label="필요 세션(⑤)" wide>
          {r.requiredBySessions.length === 0 && <span className="muted">이 산출물을 요구하는 세션이 없습니다.</span>}
          {r.requiredBySessions.map((c) => (
            <ItemChip key={c} peek={{ kind: 'handover', code: c }} label={`인수인계 ${c}`} />
          ))}
        </KV>
      </>
    ),
  },
  access: {
    label: '접근권한',
    base: '/api/access-items',
    route: '/access-items',
    enumKey: 'accessStatus',
    statuses: ['미신청', '신청', '승인', '거부', '보류'],
    titleOf: (r) => r.target,
    statusOf: (r) => r.status,
    warnings: (r) => r.warnings,
    facts: (r) => [
      { label: '상태', value: r.status, tone: r.ready ? 'ok' : r.overdue ? 'danger' : undefined },
      { label: '필요일', value: r.targetDate ?? '미정', tone: r.overdue ? 'danger' : r.targetDate ? undefined : 'muted' },
      { label: '신청 → 승인', value: `${md(r.requestedDate)} → ${md(r.approvedDate)}` },
      { label: '담당', value: r.owner ?? '미지정', tone: r.owner ? undefined : 'warn' },
    ],
    fields: (r) => [
      ['구분', r.category],
      ['용도', orDash(r.usage)],
      ['환경 범위', orDash(r.envScope)],
      ['신청 절차', orDash(r.procedure)],
      ['담당', r.owner ?? <span className="muted">미지정</span>],
      ['필요일', date(r.targetDate, r.overdue)],
      ['신청 → 승인', `${r.requestedDate ?? '-'} → ${r.approvedDate ?? '-'}${r.leadTimeDays !== null ? ` (${r.leadTimeDays}일)` : ''}`],
    ],
    links: (r) => (
      <>
        <KV label="필요 세션(⑤)" wide>
          {r.requiredBySessions.length === 0 && <span className="muted">이 접근권한을 요구하는 세션이 없습니다.</span>}
          {r.requiredBySessions.map((c) => (
            <ItemChip key={c} peek={{ kind: 'handover', code: c }} label={`인수인계 ${c}`} />
          ))}
        </KV>
      </>
    ),
  },
  handover: {
    label: '인수인계',
    base: '/api/handover-sessions',
    route: '/handover-sessions',
    enumKey: 'handoverStatus',
    statuses: ['미착수', '예정', '진행중', '완료', '보류'],
    titleOf: (r) => r.topic,
    statusOf: (r) => r.status,
    confirm: (r, next) =>
      (next === '진행중' || next === '완료') && r.blockers.length > 0
        ? `시작 전 미충족 항목이 있습니다:\n- ${r.blockers.join('\n- ')}\n\n그래도 "${next}"로 변경할까요?`
        : null,
    warnings: (r) => r.warnings,
    facts: (r) => [
      { label: '상태', value: r.status, tone: r.status === '완료' ? 'ok' : r.overdue ? 'danger' : undefined },
      { label: '예정일', value: r.plannedDate ?? '미정', tone: r.overdue ? 'danger' : r.plannedDate ? undefined : 'muted' },
      { label: '인계 → 인수', value: r.handoverFrom || r.handoverTo ? `${r.handoverFrom ?? '?'} → ${r.handoverTo ?? '?'}` : '미지정', tone: r.handoverFrom || r.handoverTo ? undefined : 'warn' },
      { label: '시작 전 미충족', value: r.blockers.length > 0 ? `${r.blockers.length}건` : '없음', tone: r.blockers.length > 0 ? 'warn' : 'ok' },
    ],
    fields: (r) => [
      ['주차 · 구분', `${r.week} · ${r.category}`],
      ['도메인', orDash(r.domain)],
      ['KT 단계', orDash(r.ktStage)],
      ['인계 → 인수', r.handoverFrom || r.handoverTo ? `${r.handoverFrom ?? '?'} → ${r.handoverTo ?? '?'}` : <span className="muted">미지정</span>],
      ['진행 방식', orDash(r.method)],
      ['산출물', orDash(r.output)],
      ['예정일', date(r.plannedDate, r.overdue)],
      ['완료일', date(r.completedDate)],
    ],
    links: (r) => (
      <>
        <KV label="마일스톤" wide>
          {r.milestoneCode ? (
            <ItemChip peek={{ kind: 'milestone', code: r.milestoneCode }} label={`${r.milestoneCode} ${r.milestoneName ?? ''}`} />
          ) : (
            <span className="muted">연결 없음</span>
          )}
        </KV>
        <KV label="선행 수급(②)" wide>
          {r.supplyDocs.length === 0 && <span className="muted">없음</span>}
          {r.supplyDocs.map((d) => (
            <ItemChip key={d.code} peek={{ kind: 'supply', code: d.code }} ready={d.ready} label={`${d.code} ${d.name} · ${d.status}`} />
          ))}
        </KV>
        <KV label="선행 접근(③)" wide>
          {r.accessItems.length === 0 && <span className="muted">없음</span>}
          {r.accessItems.map((a) => (
            <ItemChip key={a.code} peek={{ kind: 'access', code: a.code }} ready={a.ready} label={`${a.code} ${a.target} · ${a.status}`} />
          ))}
        </KV>
      </>
    ),
  },
};

type Row = { id: number; code: string };

/**
 * 다른 메뉴의 항목을 페이지 이동 없이 확인하는 팝업.
 * 상태 변경까지만 허용하고(필드 수정·삭제·순서 변경은 해당 화면에서), 변경 시 뒤쪽 화면이 다시 불러온다.
 */
export default function PeekDialog({
  kind,
  code,
  depth,
  onBack,
  onCloseAll,
}: {
  kind: PeekKind;
  code: string;
  depth: number;
  onBack: () => void;
  onCloseAll: () => void;
}) {
  const spec = SPECS[kind] as unknown as Spec<Row>;
  const peek = usePeek();
  const enums = useEnums();
  const statuses = enums?.[spec.enumKey] ?? spec.statuses;
  const list = useAsync(() => api.get<Page<Row>>(`${spec.base}?size=500&sort=id,asc`), [kind]);
  const row = list.data?.content.find((r) => r.code === code);
  const [busy, setBusy] = useState(false);
  const [msg, setMsg] = useState<string>();
  const closeRef = useRef<HTMLButtonElement>(null);

  // 열릴 때 포커스를 팝업으로 옮기고, 닫히면 원래 자리로 돌려준다
  useEffect(() => {
    const before = document.activeElement as HTMLElement | null;
    closeRef.current?.focus();
    return () => before?.focus?.();
  }, []);

  // Esc 는 이 팝업만 닫는다(뒤쪽 팝업까지 닫히지 않도록 캡처 단계에서 가로챈다)
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.key !== 'Escape') return;
      e.stopPropagation();
      onBack();
    };
    window.addEventListener('keydown', onKey, true);
    return () => window.removeEventListener('keydown', onKey, true);
  }, [onBack]);

  async function changeStatus(next: string) {
    if (!row) return;
    const m = spec.confirm?.(row, next);
    if (m && !window.confirm(m)) return;
    setBusy(true);
    setMsg(undefined);
    try {
      await api.patchStatus(`${spec.base}/${row.id}`, next);
      list.reload();
      peek.notifyChanged();
    } catch (e) {
      setMsg(`상태 변경 실패: ${(e as Error).message}`);
    } finally {
      setBusy(false);
    }
  }

  const warnings = row && spec.warnings ? spec.warnings(row) : [];
  const blockers = kind === 'handover' && row ? (row as unknown as HandoverSession).blockers : [];
  const blockedBy = kind === 'milestone' && row ? (row as unknown as Milestone).blockedBy : [];

  const fieldRows = row ? spec.fields(row) : [];
  const files = kind === 'supply' && row ? (row as unknown as SupplyDoc).files : [];

  return (
    <DialogFrame
      label={`${spec.label} ${code} 확인`}
      eyebrow={spec.label}
      code={code}
      title={row ? spec.titleOf(row) : spec.label}
      className="peek"
      backdropClass="peek-backdrop"
      meta={row ? <StatusBadge status={spec.statusOf(row)} /> : undefined}
      onClose={onBack}
      closeLabel={depth > 1 ? '이전 팝업으로' : '닫기'}
      closeText={depth > 1 ? '← 뒤로' : '✕'}
      closeRef={closeRef}
      footer={
        row ? (
          <FootBar
            left={
              <label className="foot-status">
                상태
                <StatusSelect value={spec.statusOf(row)} options={statuses} disabled={busy} onChange={changeStatus} />
              </label>
            }
            right={
              <Link className="chip" to={`${spec.route}?code=${encodeURIComponent(code)}`} onClick={onCloseAll}>
                {spec.label}에서 열기 →
              </Link>
            }
          />
        ) : undefined
      }
    >
      {list.loading && !row && <p className="muted">불러오는 중…</p>}
      {list.error && <Banner tone="danger">오류: {list.error}</Banner>}
      {!list.loading && !list.error && !row && (
        <Banner tone="danger">
          {spec.label} {code} 항목을 찾을 수 없습니다.
        </Banner>
      )}
      {msg && <Banner tone="danger">{msg}</Banner>}
      {row && (
        <>
          {blockers.length > 0 && (
            <Banner tone="warn" title={`시작 전 미충족 ${blockers.length}건`}>
              <ul>
                {blockers.map((b) => (
                  <li key={b}>{b}</li>
                ))}
              </ul>
            </Banner>
          )}
          {blockedBy.length > 0 && <Banner tone="warn">선행 마일스톤 미완료: {blockedBy.join(', ')}</Banner>}
          {warnings.length > 0 && (
            <Banner tone="warn">
              <ul>
                {warnings.map((w) => (
                  <li key={w}>{w}</li>
                ))}
              </ul>
            </Banner>
          )}
          <Facts items={spec.facts(row)} />
          <Section title="기본 정보">
            <KVGrid>
              {fieldRows.map(([k, v]) => (
                <KV key={k} label={k}>
                  {v}
                </KV>
              ))}
            </KVGrid>
          </Section>
          <Section title="연결">
            <KVGrid>
              {spec.links(row)}
              {kind === 'milestone' && (row as unknown as Milestone).links.length > 0 && (
                <KV label="연결 현황" wide>
                  {(row as unknown as Milestone).links.map((l) => (
                    <Link key={l.sheet} className="chip" to={l.route} onClick={onCloseAll} title="해당 메뉴로 이동">
                      {l.sheet} {l.label} {l.metric} {l.done}/{l.total}
                    </Link>
                  ))}
                </KV>
              )}
            </KVGrid>
          </Section>
          {kind === 'supply' && files.length > 0 && <FilesSection doc={row as unknown as SupplyDoc} readOnly onChanged={() => undefined} />}
          <p className="muted small">수정·삭제·순서 변경과 파일 업로드는 해당 화면에서 합니다.</p>
        </>
      )}
    </DialogFrame>
  );
}

