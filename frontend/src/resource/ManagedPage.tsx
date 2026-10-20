import { Fragment, useEffect, useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { api, type Page } from '../api';
import { FilterSelect, State, StatusSelect } from '../components';
import { useAsync } from '../hooks';
import { usePeek } from '../peek/context';
import { phaseHue } from '../phase';

const EXPORT_ICON = '⬇';
import ResourceDialog from './ResourceDialog';
import { ModifiedCell } from '../ui/AuditLine';
import type { BaseRow, Ctx, ResourceConfig } from './types';

/**
 * 마일스톤과 같은 수준의 관리 화면을 설정(ResourceConfig)만으로 만든다:
 * 그룹 색상 띠 · No./코드 열 · 드래그앤드롭(+▲▼) 순서 변경 · 요약 칩 · 검색/필터 · 팝업 등록·수정·삭제 · 딥링크(?code=).
 * 21~30건 규모라 전체를 한 번에 받아 필터·정렬·그룹을 클라이언트에서 처리한다.
 */
export default function ManagedPage<T extends BaseRow>({ config }: { config: ResourceConfig<T> }) {
  const { version } = usePeek(); // 확인 팝업에서 상태가 바뀌면 다시 불러온다
  const list = useAsync(() => api.get<Page<T>>(`${config.base}?size=500&sort=id,asc`), [config.base, version]);
  const extraRes = useAsync(() => (config.loadExtra ? config.loadExtra() : Promise.resolve({})), [config.base, version]);
  const reload = () => {
    list.reload();
    extraRes.reload();
  };

  const [params, setParams] = useSearchParams();
  const selectedCode = params.get('code');
  const creating = params.get('new') === '1';
  const newGroup = params.get('group') ?? undefined;

  const [q, setQ] = useState('');
  const [status, setStatus] = useState('');
  const [group, setGroup] = useState('');
  const [filterValues, setFilterValues] = useState<string[]>(() => config.filters.map(() => ''));
  const [overdueOnly, setOverdueOnly] = useState(false);
  const [msg, setMsg] = useState<string>();
  const [busyId, setBusyId] = useState<number>();
  const [pending, setPending] = useState<{ group: string; ids: number[] } | null>(null);
  const [dragId, setDragId] = useState<number>();
  const [overId, setOverId] = useState<number>();

  const all = useMemo(() => list.data?.content ?? [], [list.data]);
  useEffect(() => setPending(null), [list.data]);
  const selected = all.find((r) => r.code === selectedCode);
  const groupOf = (r: T) => String(r[config.groupField]);
  const statusOf = (r: T) => String(r[config.statusField]);

  // 그룹 순서: 고정 그룹이 있으면 그대로, 없으면 첫 등장(id) 순
  const hueOf = (g: string) => config.groupHue?.(g) ?? phaseHue(g);
  const groups = useMemo(() => {
    if (config.fixedGroups) {
      // 고정 목록에 없는 그룹(예: 이름 변경 전 값)도 잃지 않도록 뒤에 덧붙인다
      const extra = [...new Set(all.map(groupOf))].filter((g) => !config.fixedGroups!.includes(g));
      return [...config.fixedGroups, ...extra];
    }
    const seen: string[] = [];
    all.forEach((r) => {
      if (!seen.includes(groupOf(r))) seen.push(groupOf(r));
    });
    return seen;
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [all, config.fixedGroups]);

  const ctx: Ctx<T> = {
    rows: all,
    open: (code) => setParams({ code }),
    extra: (extraRes.data ?? {}) as Record<string, unknown>,
    reload,
  };

  const counts = useMemo(() => {
    const c: Record<string, number> = {};
    all.forEach((r) => (c[statusOf(r)] = (c[statusOf(r)] ?? 0) + 1));
    return c;
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [all]);
  const doneCount = all.filter(config.isDone).length;
  const overdueCount = all.filter((r) => r.overdue).length;
  const warnCount = all.filter((r) => r.warnings.length > 0).length;
  const filtering = Boolean(status || group || overdueOnly || q.trim() || filterValues.some(Boolean));

  const filtered = useMemo(() => {
    const needle = q.trim().toLowerCase();
    return all.filter(
      (r) =>
        (!status || statusOf(r) === status) &&
        (!group || groupOf(r) === group) &&
        (!overdueOnly || r.overdue) &&
        config.filters.every((f, i) => !filterValues[i] || f.valueOf(r) === filterValues[i]) &&
        (!needle || [r.code, ...config.searchOf(r)].some((v) => v?.toLowerCase().includes(needle))),
    );
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [all, q, status, group, overdueOnly, filterValues]);

  const rowsOf = (g: string, source: T[]) => {
    const rows = source.filter((r) => groupOf(r) === g).sort((a, b) => a.no - b.no || a.id - b.id);
    if (pending && pending.group === g) {
      const rank = new Map(pending.ids.map((id, i) => [id, i]));
      rows.sort((a, b) => (rank.get(a.id) ?? 0) - (rank.get(b.id) ?? 0));
    }
    return rows;
  };

  const blocks = useMemo(
    () =>
      groups
        .map((g) => ({
          name: g,
          rows: rowsOf(g, filtered),
          total: all.filter((r) => groupOf(r) === g).length,
          done: all.filter((r) => groupOf(r) === g && config.isDone(r)).length,
        }))
        .filter((b) => b.rows.length > 0 || !filtering),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [groups, filtered, all, filtering, pending],
  );

  async function commitOrder(g: string, ids: number[]) {
    setPending({ group: g, ids });
    setMsg(undefined);
    try {
      await api.send('PUT', `${config.base}/order`, { group: g, ids });
      reload();
    } catch (e) {
      setPending(null);
      setMsg(`순서 변경 실패: ${(e as Error).message}`);
      reload();
    }
  }

  function moveTo(fromId: number, toId: number) {
    const from = all.find((r) => r.id === fromId);
    const to = all.find((r) => r.id === toId);
    if (!from || !to || groupOf(from) !== groupOf(to) || fromId === toId) return;
    const ids = rowsOf(groupOf(from), all).map((r) => r.id);
    const toIdx = ids.indexOf(toId);
    ids.splice(ids.indexOf(fromId), 1);
    ids.splice(toIdx, 0, fromId);
    commitOrder(groupOf(from), ids);
  }

  function step(r: T, delta: -1 | 1) {
    const ids = rowsOf(groupOf(r), all).map((x) => x.id);
    const i = ids.indexOf(r.id);
    const j = i + delta;
    if (j < 0 || j >= ids.length) return;
    [ids[i], ids[j]] = [ids[j], ids[i]];
    commitOrder(groupOf(r), ids);
  }

  async function changeStatus(r: T, next: string) {
    const confirmMsg = config.confirmStatus?.(r, next);
    if (confirmMsg && !window.confirm(confirmMsg)) return;
    setBusyId(r.id);
    setMsg(undefined);
    try {
      await api.patchStatus<T>(`${config.base}/${r.id}`, next);
      reload();
    } catch (e) {
      setMsg(`상태 변경 실패: ${(e as Error).message}`);
    } finally {
      setBusyId(undefined);
    }
  }

  async function renameGroup(from: string) {
    const to = window.prompt(`"${from}" ${config.groupLabel}의 새 이름\n(이미 있는 이름이면 병합됩니다. 기존 코드는 바뀌지 않습니다)`, from)?.trim();
    if (!to || to === from) return;
    setMsg(undefined);
    try {
      await api.send('PATCH', `${config.base}/groups`, { from, to });
      if (group === from) setGroup(to);
      reload();
    } catch (e) {
      setMsg(`${config.groupLabel} 이름 변경 실패: ${(e as Error).message}`);
    }
  }

  const clearFilters = () => {
    setQ('');
    setStatus('');
    setGroup('');
    setOverdueOnly(false);
    setFilterValues(config.filters.map(() => ''));
  };

  /** 현재 화면의 상태·그룹·검색·필터를 서버 내보내기 파라미터로 옮긴다 */
  function exportUrl(): string {
    const p = new URLSearchParams();
    if (status) p.set('status', status);
    if (group && config.export) p.set(config.export.groupParam, group);
    if (q.trim()) p.set('q', q.trim());
    if (overdueOnly) p.set('overdue', 'true');
    config.filters.forEach((_, i) => {
      const name = config.export?.filterParams[i];
      if (name && filterValues[i]) p.set(name, filterValues[i]);
    });
    const qs = p.toString();
    return `${config.base}/export${qs ? `?${qs}` : ''}`;
  }

  const dragEnabled = !filtering;
  const COLS = 5 + config.columns.length;
  const donePct = all.length ? Math.round((doneCount / all.length) * 100) : 0;

  return (
    <>
      <h2 className="page-title">
        {[config.sheet, config.title].filter(Boolean).join(' ')}
      </h2>
      {config.header}

      <div className="summary-bar">
        <span className="chip-stat total" title={`${config.doneLabel} 기준`}>
          전체 {all.length}건 · {config.doneLabel} {donePct}%
        </span>
        {config.statuses.map((s) => (
          <button key={s} className={`chip-stat s-${s}${status === s ? ' active' : ''}`} onClick={() => setStatus(status === s ? '' : s)}>
            {s} {counts[s] ?? 0}
          </button>
        ))}
        {!config.hideOverdue && (
          <button className={`chip-stat danger${overdueOnly ? ' active' : ''}`} onClick={() => setOverdueOnly(!overdueOnly)}>
            지연 {overdueCount}
          </button>
        )}
        {warnCount > 0 && (
          <span className="chip-stat warn" title="상세에서 경고 내용을 확인하세요">
            ⚠ 경고 {warnCount}
          </span>
        )}
      </div>

      <div className="toolbar">
        <input type="search" className="search" placeholder="코드·이름·내용 검색" value={q} onChange={(e) => setQ(e.target.value)} />
        <FilterSelect label={config.groupLabel} value={group} options={groups} onChange={setGroup} />
        {config.filters.map((f, i) => (
          <FilterSelect
            key={f.label}
            label={f.label}
            value={filterValues[i]}
            options={[...new Set(all.map(f.valueOf).filter((v): v is string => Boolean(v)))]}
            onChange={(v) => setFilterValues((vs) => vs.map((x, j) => (j === i ? v : x)))}
          />
        ))}
        {filtering && <button onClick={clearFilters}>필터 초기화</button>}
        <span className="spacer" />
        {config.export && (
          <a className="btn" href={exportUrl()} download title="현재 필터를 적용해 엑셀(.xlsx)로 내려받습니다">
            {EXPORT_ICON} 엑셀 내보내기
          </a>
        )}
        {filtering && <span className="muted small">필터 적용 중에는 순서를 바꿀 수 없습니다</span>}
        <button className="primary" onClick={() => setParams({ new: '1', ...(group ? { group } : {}) })}>
          + {config.itemLabel} 추가
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
              {config.columns.map((c) => (
                <th key={c.header}>{c.header}</th>
              ))}
              <th>상태</th>
              <th>수정자 · 수정일시</th>
            </tr>
          </thead>
          <tbody>
            {blocks.map(({ name, rows, total, done }) => (
              <Fragment key={name}>
                <tr className="phase-row" style={{ '--hue': hueOf(name) } as React.CSSProperties}>
                  <td colSpan={COLS}>
                    <span className="phase-name">{name}</span>
                    {config.groupHint?.(name) && <span className="phase-hint">{config.groupHint(name)}</span>}
                    <span className="phase-meta">
                      {done}/{total} {config.doneLabel}
                    </span>
                    <span className="bar-track phase-bar">
                      <span className="bar-fill" style={{ width: `${total ? (done / total) * 100 : 0}%` }} />
                    </span>
                    {config.canRenameGroup && (
                      <button className="icon" title={`${config.groupLabel} 이름 변경`} onClick={() => renameGroup(name)}>
                        ✎
                      </button>
                    )}
                    <button className="icon" title={`이 ${config.groupLabel}에 추가`} onClick={() => setParams({ new: '1', group: name })}>
                      ＋
                    </button>
                  </td>
                </tr>
                {rows.map((r, idx) => {
                  const dropping = overId === r.id && dragId !== undefined && dragId !== r.id;
                  const fromIdx = rows.findIndex((x) => x.id === dragId);
                  return (
                    <tr
                      key={r.id}
                      className={[
                        'ms-row',
                        r.code === selectedCode ? 'selected' : '',
                        dragId === r.id ? 'dragging' : '',
                        dropping ? (fromIdx >= 0 && fromIdx < idx ? 'drop-after' : 'drop-before') : '',
                      ].join(' ')}
                      style={{ '--hue': hueOf(name) } as React.CSSProperties}
                      draggable={dragEnabled}
                      onDragStart={(e) => {
                        setDragId(r.id);
                        e.dataTransfer.effectAllowed = 'move';
                        e.dataTransfer.setData('text/plain', String(r.id));
                      }}
                      onDragOver={(e) => {
                        const dragged = all.find((x) => x.id === dragId);
                        if (dragged && groupOf(dragged) === groupOf(r)) {
                          e.preventDefault();
                          setOverId(r.id);
                        }
                      }}
                      onDrop={(e) => {
                        e.preventDefault();
                        if (dragId !== undefined) moveTo(dragId, r.id);
                        setDragId(undefined);
                        setOverId(undefined);
                      }}
                      onDragEnd={() => {
                        setDragId(undefined);
                        setOverId(undefined);
                      }}
                      onClick={() => ctx.open(r.code)}
                    >
                      <td className={`handle${dragEnabled ? '' : ' off'}`} title={dragEnabled ? '끌어서 순서 변경' : '필터 적용 중에는 이동 불가'}>
                        ⠿
                      </td>
                      <td className="no nowrap" onClick={(e) => e.stopPropagation()}>
                        <span className="no-num">{r.no}</span>
                        {dragEnabled && (
                          <span className="steps">
                            <button className="icon" title="위로" disabled={idx === 0} onClick={() => step(r, -1)}>
                              ▲
                            </button>
                            <button className="icon" title="아래로" disabled={idx === rows.length - 1} onClick={() => step(r, 1)}>
                              ▼
                            </button>
                          </span>
                        )}
                      </td>
                      <td className="nowrap code">
                        {r.code}
                        {(r.warnings.length > 0 || r.overdue) && (
                          <span className={r.overdue ? 'overdue' : 'warn-mark'} title={[r.overdue ? '일정 지연' : '', ...r.warnings].filter(Boolean).join('\n')}>
                            {' '}
                            ⚠
                          </span>
                        )}
                      </td>
                      {config.columns.map((c) => (
                        <td key={c.header} className={c.className}>
                          {c.render(r, ctx)}
                        </td>
                      ))}
                      <td onClick={(e) => e.stopPropagation()}>
                        <StatusSelect
                          value={statusOf(r)}
                          options={config.statuses}
                          disabled={busyId === r.id}
                          onChange={(v) => changeStatus(r, v)}
                        />
                      </td>
                      <td className="nowrap small">
                        <ModifiedCell row={r} />
                      </td>
                    </tr>
                  );
                })}
              </Fragment>
            ))}
            {blocks.length === 0 && (
              <tr>
                <td colSpan={COLS} className="muted empty">
                  조건에 맞는 항목이 없습니다.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </State>

      {(selected || creating) && (
        <ResourceDialog
          key={creating ? 'new' : selected!.id}
          config={config}
          row={creating ? undefined : selected}
          ctx={ctx}
          groups={groups}
          defaultGroup={newGroup}
          onClose={() => setParams({})}
          onChanged={(code) => {
            reload();
            if (code) ctx.open(code);
          }}
        />
      )}
    </>
  );
}
