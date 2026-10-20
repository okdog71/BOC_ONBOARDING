import { Fragment, useEffect, useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { api, type Page } from '../api';
import { FilterSelect, State, StatusSelect } from '../components';
import { useAsync, useEnums } from '../hooks';
import { usePeek } from '../peek/context';
import { SheetChips } from '../links';
import { phaseHue } from '../phase';
import type { Milestone, PhaseInfo } from '../types';
import { ModifiedCell } from '../ui/AuditLine';
import MilestoneDialog from './milestones/MilestoneDialog';

const STATUS_ORDER = ['미착수', '진행중', '완료', '보류'];
const COLS = 11;

export default function Milestones() {
  const enums = useEnums();
  const statuses = enums?.milestoneStatus ?? STATUS_ORDER;

  // 21건 규모이므로 전체를 한 번에 받아 필터·정렬·그룹을 클라이언트에서 처리한다(서버 필터는 API 소비자용으로 별도 제공).
  const { version } = usePeek(); // 확인 팝업에서 상태가 바뀌면 다시 불러온다
  const list = useAsync(() => api.get<Page<Milestone>>('/api/milestones?size=500&sort=id,asc'), [version]);
  const phaseRes = useAsync(() => api.get<PhaseInfo[]>('/api/milestones/phases'), [version]);
  const ownerRes = useAsync(() => api.get<string[]>('/api/milestones/owners'), [version]);
  const reloadAll = () => {
    list.reload();
    phaseRes.reload();
    ownerRes.reload();
  };

  const [params, setParams] = useSearchParams();
  const selectedCode = params.get('code');
  const creating = params.get('new') === '1';
  const newPhase = params.get('phase') ?? undefined;

  const [q, setQ] = useState('');
  const [status, setStatus] = useState('');
  const [phase, setPhase] = useState('');
  const [owner, setOwner] = useState('');
  const [overdueOnly, setOverdueOnly] = useState(false);
  const [msg, setMsg] = useState<string>();
  const [busyId, setBusyId] = useState<number>();

  // 순서 변경: 저장 응답이 목록에 반영될 때까지 화면에서 먼저 보여 주는 낙관적 순서
  const [pending, setPending] = useState<{ phase: string; ids: number[] } | null>(null);
  const [dragId, setDragId] = useState<number>();
  const [overId, setOverId] = useState<number>();

  const all = useMemo(() => list.data?.content ?? [], [list.data]);
  useEffect(() => setPending(null), [list.data]);
  const phases = phaseRes.data ?? [];
  const selected = all.find((m) => m.code === selectedCode);

  const open = (code: string) => setParams({ code });
  const close = () => setParams({});

  const counts = useMemo(() => {
    const c: Record<string, number> = {};
    all.forEach((m) => (c[m.status] = (c[m.status] ?? 0) + 1));
    return c;
  }, [all]);
  const overdueCount = all.filter((m) => m.overdue).length;
  const donePct = all.length ? Math.round(((counts['완료'] ?? 0) / all.length) * 100) : 0;

  const filtering = Boolean(status || phase || owner || overdueOnly || q.trim());

  const filtered = useMemo(() => {
    const needle = q.trim().toLowerCase();
    return all.filter(
      (m) =>
        (!status || m.status === status) &&
        (!phase || m.phase === phase) &&
        (!owner || m.owner === owner) &&
        (!overdueOnly || m.overdue) &&
        (!needle || [m.code, m.name, m.goal, m.output].some((v) => v?.toLowerCase().includes(needle))),
    );
  }, [all, q, status, phase, owner, overdueOnly]);

  /** 단계별 행: 서버의 No. 순서(낙관적 순서가 있으면 그것) */
  const rowsOf = (phaseName: string, source: Milestone[]) => {
    const rows = source.filter((m) => m.phase === phaseName).sort((a, b) => a.no - b.no || a.id - b.id);
    if (pending && pending.phase === phaseName) {
      const rank = new Map(pending.ids.map((id, i) => [id, i]));
      rows.sort((a, b) => (rank.get(a.id) ?? 0) - (rank.get(b.id) ?? 0));
    }
    return rows;
  };

  const groups = useMemo(
    () =>
      phases
        .map((p) => ({ info: p, rows: rowsOf(p.phase, filtered) }))
        .filter((g) => g.rows.length > 0 || !filtering),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [phases, filtered, filtering, pending],
  );

  async function commitOrder(phaseName: string, ids: number[]) {
    setPending({ phase: phaseName, ids });
    setMsg(undefined);
    try {
      await api.send('PUT', '/api/milestones/order', { phase: phaseName, ids });
      reloadAll();
    } catch (e) {
      setPending(null);
      setMsg(`순서 변경 실패: ${(e as Error).message}`);
      reloadAll();
    }
  }

  /** 같은 단계 안에서 fromId 를 toId 자리로 옮긴다(아래로 끌면 뒤, 위로 끌면 앞에 놓임). */
  function moveTo(fromId: number, toId: number) {
    const from = all.find((m) => m.id === fromId);
    const to = all.find((m) => m.id === toId);
    if (!from || !to || from.phase !== to.phase || fromId === toId) return;
    const ids = rowsOf(from.phase, all).map((m) => m.id);
    const toIdx = ids.indexOf(toId);
    ids.splice(ids.indexOf(fromId), 1);
    ids.splice(toIdx, 0, fromId); // 원래 대상 위치에 삽입 → 아래로 끌면 대상 뒤, 위로 끌면 대상 앞
    commitOrder(from.phase, ids);
  }

  function step(m: Milestone, delta: -1 | 1) {
    const ids = rowsOf(m.phase, all).map((x) => x.id);
    const i = ids.indexOf(m.id);
    const j = i + delta;
    if (j < 0 || j >= ids.length) return;
    [ids[i], ids[j]] = [ids[j], ids[i]];
    commitOrder(m.phase, ids);
  }

  async function changeStatus(m: Milestone, next: string) {
    if ((next === '진행중' || next === '완료') && m.blockedBy.length > 0) {
      if (!window.confirm(`선행 마일스톤이 아직 완료되지 않았습니다: ${m.blockedBy.join(', ')}\n그래도 "${next}"로 변경할까요?`)) return;
    }
    if (next === '완료' && m.progress.total > m.progress.done) {
      if (!window.confirm(`체크리스트 ${m.progress.total - m.progress.done}개가 미완료입니다.\n그래도 "완료"로 변경할까요?`)) return;
    }
    setBusyId(m.id);
    setMsg(undefined);
    try {
      await api.patchStatus<Milestone>(`/api/milestones/${m.id}`, next);
      reloadAll();
    } catch (e) {
      setMsg(`상태 변경 실패: ${(e as Error).message}`);
    } finally {
      setBusyId(undefined);
    }
  }

  async function renamePhase(from: string) {
    const to = window.prompt(`"${from}" 단계의 새 이름\n(이미 있는 이름이면 두 단계가 병합됩니다. 기존 코드는 바뀌지 않습니다)`, from)?.trim();
    if (!to || to === from) return;
    setMsg(undefined);
    try {
      await api.send('PATCH', '/api/milestones/phases', { from, to });
      if (phase === from) setPhase(to);
      reloadAll();
    } catch (e) {
      setMsg(`단계 이름 변경 실패: ${(e as Error).message}`);
    }
  }

  const clearFilters = () => {
    setQ('');
    setStatus('');
    setPhase('');
    setOwner('');
    setOverdueOnly(false);
  };

  const dragEnabled = !filtering;

  return (
    <>
      <div className="summary-bar">
        <span className="chip-stat total" title="완료율">
          전체 {all.length}건 · 완료율 {donePct}%
        </span>
        {statuses.map((s) => (
          <button
            key={s}
            className={`chip-stat s-${s}${status === s ? ' active' : ''}`}
            onClick={() => setStatus(status === s ? '' : s)}
          >
            {s} {counts[s] ?? 0}
          </button>
        ))}
        <button className={`chip-stat danger${overdueOnly ? ' active' : ''}`} onClick={() => setOverdueOnly(!overdueOnly)}>
          지연 {overdueCount}
        </button>
      </div>

      <div className="toolbar">
        <input
          type="search"
          className="search"
          placeholder="코드·이름·목표·산출물 검색"
          value={q}
          onChange={(e) => setQ(e.target.value)}
        />
        <FilterSelect label="단계" value={phase} options={phases.map((p) => p.phase)} onChange={setPhase} />
        <FilterSelect label="담당" value={owner} options={ownerRes.data ?? []} onChange={setOwner} />
        {filtering && <button onClick={clearFilters}>필터 초기화</button>}
        <span className="spacer" />
        {filtering && <span className="muted small">필터 적용 중에는 순서를 바꿀 수 없습니다</span>}
        <button className="primary" onClick={() => setParams({ new: '1', ...(phase ? { phase } : {}) })}>
          + 마일스톤 추가
        </button>
      </div>
      {msg && <p className="error">{msg}</p>}

      <State loading={list.loading && !list.data} error={list.error}>
        <table className="ms-table">
          <thead>
            <tr>
              <th aria-label="이동" />
              <th>No.</th>
              <th>코드</th>
              <th>마일스톤</th>
              <th>산출물</th>
              <th>선행</th>
              <th>담당</th>
              <th>목표일</th>
              <th>진척</th>
              <th>상태</th>
              <th>수정자 · 수정일시</th>
            </tr>
          </thead>
          <tbody>
            {groups.map(({ info, rows }) => (
              <Fragment key={info.phase}>
                <tr className="phase-row" style={{ '--hue': phaseHue(info.phase) } as React.CSSProperties}>
                  <td colSpan={COLS}>
                    <span className="phase-name">{info.phase}</span>
                    <span className="phase-meta">
                      {info.done}/{info.total} 완료
                    </span>
                    <span className="bar-track phase-bar">
                      <span className="bar-fill" style={{ width: `${info.total ? (info.done / info.total) * 100 : 0}%` }} />
                    </span>
                    <button className="icon" title="단계 이름 변경" onClick={() => renamePhase(info.phase)}>
                      ✎
                    </button>
                    <button className="icon" title="이 단계에 마일스톤 추가" onClick={() => setParams({ new: '1', phase: info.phase })}>
                      ＋
                    </button>
                  </td>
                </tr>
                {rows.map((m, idx) => {
                  const dropping = overId === m.id && dragId !== undefined && dragId !== m.id;
                  const fromIdx = rows.findIndex((r) => r.id === dragId);
                  return (
                    <tr
                      key={m.id}
                      className={[
                        'ms-row',
                        m.code === selectedCode ? 'selected' : '',
                        dragId === m.id ? 'dragging' : '',
                        dropping ? (fromIdx >= 0 && fromIdx < idx ? 'drop-after' : 'drop-before') : '',
                      ].join(' ')}
                      style={{ '--hue': phaseHue(m.phase) } as React.CSSProperties}
                      draggable={dragEnabled}
                      onDragStart={(e) => {
                        setDragId(m.id);
                        e.dataTransfer.effectAllowed = 'move';
                        e.dataTransfer.setData('text/plain', String(m.id));
                      }}
                      onDragOver={(e) => {
                        const dragged = all.find((x) => x.id === dragId);
                        if (dragged && dragged.phase === m.phase) {
                          e.preventDefault(); // 같은 단계 안에서만 드롭 허용
                          setOverId(m.id);
                        }
                      }}
                      onDrop={(e) => {
                        e.preventDefault();
                        if (dragId !== undefined) moveTo(dragId, m.id);
                        setDragId(undefined);
                        setOverId(undefined);
                      }}
                      onDragEnd={() => {
                        setDragId(undefined);
                        setOverId(undefined);
                      }}
                      onClick={() => open(m.code)}
                    >
                      <td className={`handle${dragEnabled ? '' : ' off'}`} title={dragEnabled ? '끌어서 순서 변경' : '필터 적용 중에는 이동 불가'}>
                        ⠿
                      </td>
                      <td className="no nowrap" onClick={(e) => e.stopPropagation()}>
                        <span className="no-num">{m.no}</span>
                        {dragEnabled && (
                          <span className="steps">
                            <button className="icon" title="위로" disabled={idx === 0} onClick={() => step(m, -1)}>
                              ▲
                            </button>
                            <button className="icon" title="아래로" disabled={idx === rows.length - 1} onClick={() => step(m, 1)}>
                              ▼
                            </button>
                          </span>
                        )}
                      </td>
                      <td className="nowrap code">{m.code}</td>
                      <td className="name">
                        <div>{m.name}</div>
                        {m.goal && <div className="sub">{m.goal}</div>}
                      </td>
                      <td className="chips">
                        <SheetChips text={m.output} links={m.links} />
                        {m.output && !/[②-⑬]/.test(m.output) && <span className="muted small">{m.output}</span>}
                      </td>
                      <td className="chips" onClick={(e) => e.stopPropagation()}>
                        {m.predecessorCodes.map((c) => (
                          <button
                            key={c}
                            className={`chip${m.blockedBy.includes(c) ? ' chip-wait' : ''}`}
                            title={m.blockedBy.includes(c) ? '선행 미완료' : '선행 완료'}
                            onClick={() => open(c)}
                          >
                            {c}
                          </button>
                        ))}
                        {m.predecessorCodes.length === 0 && m.precondition && (
                          <span className="muted small" title="마일스톤 코드가 아닌 선행 조건">
                            {m.precondition}
                          </span>
                        )}
                      </td>
                      <td className="nowrap">{m.owner ?? <span className="muted">미지정</span>}</td>
                      <td className="nowrap">
                        {m.targetDate ? <span className={m.overdue ? 'overdue' : ''}>{m.targetDate}</span> : <span className="muted">-</span>}
                        {m.overdue && <span className="overdue"> ⚠</span>}
                      </td>
                      <td className="progress-cell">
                        {m.progress.percent === null ? (
                          <span className="muted">-</span>
                        ) : (
                          <span title={`${m.progress.done}/${m.progress.total}`}>
                            <span className="bar-track mini">
                              <span className="bar-fill" style={{ width: `${m.progress.percent}%` }} />
                            </span>
                            {m.progress.percent}%
                          </span>
                        )}
                      </td>
                      <td onClick={(e) => e.stopPropagation()}>
                        <StatusSelect
                          value={m.status}
                          options={statuses}
                          disabled={busyId === m.id}
                          onChange={(v) => changeStatus(m, v)}
                        />
                      </td>
                      <td className="nowrap small">
                        <ModifiedCell row={m} />
                      </td>
                    </tr>
                  );
                })}
              </Fragment>
            ))}
            {groups.length === 0 && (
              <tr>
                <td colSpan={COLS} className="muted empty">
                  조건에 맞는 마일스톤이 없습니다.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </State>

      {(selected || creating) && (
        <MilestoneDialog
          key={creating ? 'new' : selected!.id}
          milestone={creating ? undefined : selected}
          all={all}
          phases={phases.map((p) => p.phase)}
          statuses={statuses}
          defaultPhase={newPhase}
          onClose={close}
          onOpenCode={open}
          onChanged={(code) => {
            reloadAll();
            if (code) open(code);
          }}
        />
      )}
    </>
  );
}
