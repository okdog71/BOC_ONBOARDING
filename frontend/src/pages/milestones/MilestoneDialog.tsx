import { useEffect, useState } from 'react';
import { api } from '../../api';
import { LinkChip, SheetChips } from '../../links';
import { nextCodePreview } from '../../phase';
import { StatusSelect } from '../../components';
import type { Milestone, MilestoneInput } from '../../types';

export const emptyInput = (phase = ''): MilestoneInput => ({
  phase,
  code: '',
  name: '',
  goal: '',
  checkpoint: '',
  output: '',
  precondition: '',
  owner: '',
  durationWeeks: '',
  status: '미착수',
  doneCriteria: '',
  note: '',
  targetDate: '',
});

const toInput = (m: Milestone): MilestoneInput => ({
  phase: m.phase,
  code: m.code,
  name: m.name,
  goal: m.goal ?? '',
  checkpoint: m.checkpoint ?? '',
  output: m.output ?? '',
  precondition: m.precondition ?? '',
  owner: m.owner ?? '',
  durationWeeks: m.durationWeeks ?? '',
  status: m.status,
  doneCriteria: m.doneCriteria ?? '',
  note: m.note ?? '',
  targetDate: m.targetDate ?? '',
});

/** 빈 문자열은 null 로 보내 서버에서 "미입력"으로 저장 */
const toPayload = (i: MilestoneInput) =>
  Object.fromEntries(Object.entries(i).filter(([k]) => k !== 'code').map(([k, v]) => [k, typeof v === 'string' && v.trim() === '' ? null : v]));

interface Props {
  /** 보기/수정 대상. 생성 모드에서는 undefined */
  milestone?: Milestone;
  all: Milestone[];
  phases: string[];
  owners: string[];
  statuses: string[];
  /** 생성 모드 초기 단계 */
  defaultPhase?: string;
  onClose: () => void;
  /** 저장·삭제 등으로 서버 데이터가 바뀜 → 목록 재조회. code 가 있으면 그 항목을 선택 */
  onChanged: (selectCode?: string | null) => void;
  onOpenCode: (code: string) => void;
}

export default function MilestonePanel({
  milestone,
  all,
  phases,
  owners,
  statuses,
  defaultPhase,
  onClose,
  onChanged,
  onOpenCode,
}: Props) {
  const creating = !milestone;
  const [editing, setEditing] = useState(creating);
  const [draft, setDraft] = useState<MilestoneInput>(() => (milestone ? toInput(milestone) : emptyInput(defaultPhase)));
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string>();
  const [newItem, setNewItem] = useState('');

  // 다른 마일스톤을 열거나 서버 데이터가 갱신되면(저장 후 등) 초안을 맞춘다. 편집 중에는 덮어쓰지 않는다.
  useEffect(() => {
    if (!editing) setDraft(milestone ? toInput(milestone) : emptyInput(defaultPhase));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [milestone, defaultPhase]);

  useEffect(() => {
    setEditing(!milestone);
    setError(undefined);
    setNewItem('');
    // 선택 대상(id)이 바뀔 때만 모드 초기화
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [milestone?.id]);

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && !editing) onClose();
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [editing, onClose]);

  const set = <K extends keyof MilestoneInput>(k: K, v: MilestoneInput[K]) => setDraft((d) => ({ ...d, [k]: v }));

  async function run(fn: () => Promise<unknown>, after?: () => void) {
    setBusy(true);
    setError(undefined);
    try {
      await fn();
      after?.();
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setBusy(false);
    }
  }

  function save() {
    if (!draft.phase.trim() || !draft.name.trim()) {
      setError('단계와 이름은 필수입니다.');
      return;
    }
    run(
      async () => {
        const saved = creating
          ? await api.send<Milestone>('POST', '/api/milestones', toPayload(draft))
          : await api.send<Milestone>('PUT', `/api/milestones/${milestone!.id}`, toPayload(draft));
        setEditing(false);
        onChanged(saved.code);
      },
    );
  }

  function remove() {
    if (!milestone || !window.confirm(`${milestone.code} ${milestone.name}\n정말 삭제할까요? 되돌릴 수 없습니다.`)) return;
    run(async () => {
      await api.send('DELETE', `/api/milestones/${milestone.id}`);
      onChanged(null);
      onClose();
    });
  }

  function changeStatus(next: string) {
    if (!milestone) return;
    if ((next === '진행중' || next === '완료') && milestone.blockedBy.length > 0) {
      const ok = window.confirm(`선행 마일스톤이 아직 완료되지 않았습니다: ${milestone.blockedBy.join(', ')}\n그래도 "${next}"로 변경할까요?`);
      if (!ok) return;
    }
    if (next === '완료' && milestone.progress.total > milestone.progress.done) {
      const ok = window.confirm(`체크리스트 ${milestone.progress.total - milestone.progress.done}개가 미완료입니다.\n그래도 "완료"로 변경할까요?`);
      if (!ok) return;
    }
    run(async () => {
      await api.patchStatus<Milestone>(`/api/milestones/${milestone.id}`, next);
      onChanged(milestone.code);
    });
  }

  // ---- 체크리스트 ----
  const itemsPath = milestone ? `/api/milestones/${milestone.id}/check-items` : '';
  const saveItems = (items: { id?: number; title: string; done: boolean }[]) =>
    run(async () => {
      await api.send('PUT', itemsPath, { items });
      onChanged(milestone!.code);
    });
  const current = milestone?.checkItems ?? [];

  const otherCodes = all.filter((m) => m.id !== milestone?.id).map((m) => m.code);
  const appendPrecondition = (code: string) => {
    if (!code) return;
    const parts = draft.precondition.split(/[,\s]+/).filter(Boolean);
    if (!parts.includes(code)) set('precondition', [...parts, code].join(','));
  };

  return (
    <div
      className="modal-backdrop"
      onMouseDown={(e) => {
        // 편집 중에는 바깥 클릭으로 닫지 않는다(입력 보호)
        if (e.target === e.currentTarget && !editing) onClose();
      }}
    >
    <div className="modal" role="dialog" aria-modal="true" aria-label="마일스톤">
      <div className="panel-head">
        <h2>
          {creating ? '새 마일스톤' : milestone.name}
          {!creating && <span className="code-tag">{milestone.code}</span>}
        </h2>
        <button onClick={onClose} aria-label="닫기">
          ✕
        </button>
      </div>

      {error && <p className="error">{error}</p>}

      {!creating && !editing && (
        <div className="panel-actions">
          <StatusSelect value={milestone.status} options={statuses} disabled={busy} onChange={changeStatus} />
          <button onClick={() => setEditing(true)}>편집</button>
          <button className="danger" onClick={remove} disabled={busy}>
            삭제
          </button>
        </div>
      )}

      {editing ? (
        <form
          className="form"
          onSubmit={(e) => {
            e.preventDefault();
            save();
          }}
        >
          <label>
            단계 *
            <input list="phase-list" value={draft.phase} onChange={(e) => set('phase', e.target.value)} placeholder="기존 단계 선택 또는 새 단계 입력" />
            <datalist id="phase-list">
              {phases.map((p) => (
                <option key={p} value={p} />
              ))}
            </datalist>
          </label>
          <div className="row2">
            <label>
              코드 (자동 부여·변경 불가)
              <input
                readOnly
                className="readonly"
                value={creating ? `${nextCodePreview(draft.phase, all.map((m) => m.code))} (예상)` : draft.code}
              />
            </label>
            <label>
              상태
              <select value={draft.status} onChange={(e) => set('status', e.target.value)}>
                {statuses.map((s) => (
                  <option key={s}>{s}</option>
                ))}
              </select>
            </label>
          </div>
          <label>
            이름 *
            <input value={draft.name} onChange={(e) => set('name', e.target.value)} />
          </label>
          <label>
            목표
            <textarea rows={2} value={draft.goal} onChange={(e) => set('goal', e.target.value)} />
          </label>
          <label>
            체크포인트
            <textarea rows={2} value={draft.checkpoint} onChange={(e) => set('checkpoint', e.target.value)} placeholder="항목은 · 로 구분하면 체크리스트로 만들 수 있습니다" />
          </label>
          <label>
            산출물
            <input value={draft.output} onChange={(e) => set('output', e.target.value)} placeholder="예: 메뉴 인벤토리(⑦) — 시트 기호는 링크로 표시됩니다" />
          </label>
          <label>
            완료 기준
            <textarea rows={2} value={draft.doneCriteria} onChange={(e) => set('doneCriteria', e.target.value)} />
          </label>
          <label>
            선행 조건
            <input value={draft.precondition} onChange={(e) => set('precondition', e.target.value)} placeholder="P0.1,P0.2 또는 P0.1~P0.4 (코드는 링크로 연결)" />
            <select value="" onChange={(e) => appendPrecondition(e.target.value)}>
              <option value="">+ 선행 코드 추가…</option>
              {otherCodes.map((c) => (
                <option key={c}>{c}</option>
              ))}
            </select>
          </label>
          <div className="row2">
            <label>
              담당
              <input list="owner-list" value={draft.owner} onChange={(e) => set('owner', e.target.value)} />
              <datalist id="owner-list">
                {owners.map((o) => (
                  <option key={o} value={o} />
                ))}
              </datalist>
            </label>
            <label>
              목표일
              <input type="date" value={draft.targetDate} onChange={(e) => set('targetDate', e.target.value)} />
            </label>
          </div>
          <label>
            소요 기간(주)
            <input value={draft.durationWeeks} onChange={(e) => set('durationWeeks', e.target.value)} />
          </label>
          <label>
            비고
            <textarea rows={2} value={draft.note} onChange={(e) => set('note', e.target.value)} />
          </label>
          <div className="panel-actions">
            <button type="submit" className="primary" disabled={busy}>
              {creating ? '추가' : '저장'}
            </button>
            <button
              type="button"
              disabled={busy}
              onClick={() => {
                if (creating) onClose();
                else {
                  setDraft(toInput(milestone));
                  setEditing(false);
                  setError(undefined);
                }
              }}
            >
              취소
            </button>
          </div>
        </form>
      ) : (
        milestone && (
          <dl className="detail">
            <dt>단계</dt>
            <dd>
              {milestone.phase} <span className="muted">· No. {milestone.no}</span>
            </dd>
            <dt>목표</dt>
            <dd>{milestone.goal ?? '-'}</dd>
            <dt>체크포인트</dt>
            <dd>{milestone.checkpoint ?? '-'}</dd>
            <dt>산출물</dt>
            <dd>
              {milestone.output ?? '-'} <SheetChips text={milestone.output} links={milestone.links} />
            </dd>
            <dt>연결 현황</dt>
            <dd>
              {milestone.links.length === 0 && <span className="muted">산출물이 가리키는 메뉴 연결 없음</span>}
              {milestone.links.map((l) => (
                <div key={l.sheet} className="link-progress">
                  <LinkChip to={l.route} label={`${l.sheet} ${l.label}`} />
                  <span className="bar-track mini">
                    <span className="bar-fill" style={{ width: `${l.total ? (l.done / l.total) * 100 : 0}%` }} />
                  </span>
                  <span className="small">
                    {l.metric} {l.done}/{l.total}
                  </span>
                </div>
              ))}
            </dd>
            <dt>완료 기준</dt>
            <dd>{milestone.doneCriteria ?? '-'}</dd>
            <dt>선행</dt>
            <dd>
              {milestone.predecessorCodes.map((c) => (
                <button key={c} className="chip" onClick={() => onOpenCode(c)} title="해당 마일스톤 열기">
                  {c}
                  {milestone.blockedBy.includes(c) && ' ⏳'}
                </button>
              ))}
              {milestone.precondition && <span className="muted"> {milestone.precondition}</span>}
              {!milestone.precondition && milestone.predecessorCodes.length === 0 && '-'}
            </dd>
            <dt>담당</dt>
            <dd>{milestone.owner ?? '-'}</dd>
            <dt>일정</dt>
            <dd>
              목표 {milestone.targetDate ?? '-'}
              {milestone.overdue && <span className="overdue"> · 지연</span>}
              {milestone.completedDate && ` · 완료 ${milestone.completedDate}`}
              {milestone.durationWeeks && ` · 기간 ${milestone.durationWeeks}주`}
            </dd>
            <dt>비고</dt>
            <dd>{milestone.note ?? '-'}</dd>
          </dl>
        )
      )}

      {milestone && (
        <section className="checklist">
          <h3>
            체크리스트
            {milestone.progress.percent !== null && (
              <small>
                {' '}
                {milestone.progress.done}/{milestone.progress.total} · {milestone.progress.percent}%
              </small>
            )}
          </h3>
          {current.length === 0 && <p className="muted">항목이 없습니다.</p>}
          <ul>
            {current.map((it) => (
              <li key={it.id}>
                <label>
                  <input
                    type="checkbox"
                    checked={it.done}
                    disabled={busy}
                    onChange={(e) =>
                      run(async () => {
                        await api.send('PATCH', `${itemsPath}/${it.id}`, { done: e.target.checked });
                        onChanged(milestone.code);
                      })
                    }
                  />
                  <span className={it.done ? 'done-text' : ''}>{it.title}</span>
                </label>
                <button
                  className="icon"
                  title="삭제"
                  disabled={busy}
                  onClick={() => saveItems(current.filter((c) => c.id !== it.id))}
                >
                  ✕
                </button>
              </li>
            ))}
          </ul>
          <form
            className="add-item"
            onSubmit={(e) => {
              e.preventDefault();
              const title = newItem.trim();
              if (!title) return;
              setNewItem('');
              saveItems([...current, { title, done: false }]);
            }}
          >
            <input value={newItem} onChange={(e) => setNewItem(e.target.value)} placeholder="항목 추가 후 Enter" maxLength={500} />
          </form>
          {current.length === 0 && milestone.checkpoint && (
            <button
              disabled={busy}
              onClick={() =>
                run(async () => {
                  await api.send('POST', `${itemsPath}/generate`);
                  onChanged(milestone.code);
                })
              }
            >
              체크포인트로 항목 생성
            </button>
          )}
        </section>
      )}

    </div>
    </div>
  );
}
