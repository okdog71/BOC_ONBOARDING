import { useEffect, useState } from 'react';
import { api } from '../../api';
import { LinkChip, SheetChips } from '../../links';
import { nextCodePreview } from '../../phase';
import { StatusBadge, StatusSelect } from '../../components';
import { phaseHue } from '../../phase';
import AuditLine from '../../ui/AuditLine';
import { useUsers } from '../../users';
import { Banner, DialogFrame, Facts, FootBar, FormField, FormGrid, KV, KVGrid, Section } from '../../ui/detail';
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
  statuses,
  defaultPhase,
  onClose,
  onChanged,
  onOpenCode,
}: Props) {
  const creating = !milestone;
  const userNames = useUsers().map((u) => u.name);
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

  const pct = milestone?.progress.percent ?? null;

  // ---------- 읽기 ----------
  const readBody = milestone && (
    <>
      {(milestone.overdue || milestone.blockedBy.length > 0) && (
        <Banner tone={milestone.overdue ? 'danger' : 'warn'}>
          <ul>
            {milestone.overdue && <li>목표일이 지났습니다.</li>}
            {milestone.blockedBy.length > 0 && <li>선행 마일스톤이 아직 완료되지 않았습니다: {milestone.blockedBy.join(', ')}</li>}
          </ul>
        </Banner>
      )}
      <Facts
        items={[
          { label: '상태', value: milestone.status, tone: milestone.status === '완료' ? 'ok' : undefined },
          { label: '목표일', value: milestone.targetDate ?? '미정', tone: milestone.overdue ? 'danger' : milestone.targetDate ? undefined : 'muted' },
          { label: '완료일', value: milestone.completedDate ?? '-', tone: milestone.completedDate ? 'ok' : 'muted' },
          { label: '체크리스트', value: milestone.progress.total ? `${milestone.progress.done}/${milestone.progress.total}${pct !== null ? ` · ${pct}%` : ''}` : '없음', tone: milestone.progress.total ? undefined : 'muted' },
          { label: '담당', value: milestone.owner ?? '미지정', tone: milestone.owner ? undefined : 'warn' },
        ]}
      />
      <Section title="기본">
        <KVGrid>
          <KV label="단계">
            {milestone.phase} <span className="muted">· No. {milestone.no}</span>
          </KV>
          <KV label="소요 기간">{milestone.durationWeeks && `${milestone.durationWeeks}주`}</KV>
          <KV label="목표" wide>
            <span className="pre">{milestone.goal}</span>
          </KV>
          <KV label="체크포인트" wide>
            <span className="pre">{milestone.checkpoint}</span>
          </KV>
          <KV label="완료 기준" wide>
            <span className="pre">{milestone.doneCriteria}</span>
          </KV>
        </KVGrid>
      </Section>
      <Section title="산출물 · 연결">
        <KVGrid>
          <KV label="산출물" wide>
            {milestone.output && (
              <>
                {milestone.output} <SheetChips text={milestone.output} links={milestone.links} />
              </>
            )}
          </KV>
          {milestone.links.length > 0 && (
            <KV label="연결 현황" wide>
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
            </KV>
          )}
          <KV label="선행" wide>
            {(milestone.predecessorCodes.length > 0 || milestone.precondition) && (
              <>
                {milestone.predecessorCodes.map((c) => (
                  <button key={c} type="button" className="chip" onClick={() => onOpenCode(c)} title="해당 마일스톤 열기">
                    {c}
                    {milestone.blockedBy.includes(c) && ' ⏳'}
                  </button>
                ))}
                {milestone.precondition && <span className="muted"> {milestone.precondition}</span>}
              </>
            )}
          </KV>
        </KVGrid>
      </Section>
      {milestone.note && (
        <Section title="메모">
          <KVGrid>
            <KV label="비고" wide>
              <span className="pre">{milestone.note}</span>
            </KV>
          </KVGrid>
        </Section>
      )}
      <AuditLine row={milestone} />
    </>
  );

  // ---------- 편집 ----------
  const editBody = (
    <form
      id="milestone-form"
      onSubmit={(e) => {
        e.preventDefault();
        save();
      }}
    >
      <Banner tone="edit">입력한 뒤 아래 "{creating ? '추가' : '저장'}"을 눌러야 반영됩니다. 취소하면 변경 내용이 사라집니다.</Banner>
      <Section title="기본">
        <FormGrid>
          <FormField label="단계" required>
            <input list="phase-list" value={draft.phase} onChange={(e) => set('phase', e.target.value)} placeholder="기존 단계 선택 또는 새 단계 입력" />
            <datalist id="phase-list">
              {phases.map((p) => (
                <option key={p} value={p} />
              ))}
            </datalist>
          </FormField>
          <FormField label="코드" hint="자동 부여 · 변경 불가">
            <input
              readOnly
              className="readonly"
              value={creating ? `${nextCodePreview(draft.phase, all.map((m) => m.code))} (예상)` : draft.code}
            />
          </FormField>
          <FormField label="이름" required wide>
            <input value={draft.name} onChange={(e) => set('name', e.target.value)} />
          </FormField>
          <FormField label="상태">
            <select value={draft.status} onChange={(e) => set('status', e.target.value)}>
              {statuses.map((st) => (
                <option key={st}>{st}</option>
              ))}
            </select>
          </FormField>
          <FormField label="소요 기간(주)">
            <input value={draft.durationWeeks} onChange={(e) => set('durationWeeks', e.target.value)} />
          </FormField>
        </FormGrid>
      </Section>
      <Section title="내용">
        <FormGrid>
          <FormField label="목표" wide>
            <textarea rows={2} value={draft.goal} onChange={(e) => set('goal', e.target.value)} />
          </FormField>
          <FormField label="체크포인트" wide hint="항목은 · 로 구분하면 체크리스트로 만들 수 있습니다">
            <textarea rows={2} value={draft.checkpoint} onChange={(e) => set('checkpoint', e.target.value)} />
          </FormField>
          <FormField label="산출물" wide hint="예: 메뉴 인벤토리(⑦) — 시트 기호는 링크로 표시됩니다">
            <input value={draft.output} onChange={(e) => set('output', e.target.value)} />
          </FormField>
          <FormField label="완료 기준" wide>
            <textarea rows={2} value={draft.doneCriteria} onChange={(e) => set('doneCriteria', e.target.value)} />
          </FormField>
        </FormGrid>
      </Section>
      <Section title="선행 · 일정">
        <FormGrid>
          <FormField label="선행 조건" wide hint="P0.1,P0.2 또는 P0.1~P0.4 (코드는 링크로 연결)">
            <input value={draft.precondition} onChange={(e) => set('precondition', e.target.value)} />
            <select value="" onChange={(e) => appendPrecondition(e.target.value)}>
              <option value="">+ 선행 코드 추가…</option>
              {otherCodes.map((c) => (
                <option key={c}>{c}</option>
              ))}
            </select>
          </FormField>
          <FormField label="담당">
            <select value={draft.owner} onChange={(e) => set('owner', e.target.value)}>
              <option value="">(미지정)</option>
              {userNames.map((n) => (
                <option key={n}>{n}</option>
              ))}
              {draft.owner && !userNames.includes(draft.owner) && <option value={draft.owner}>{draft.owner} (사용자 목록에 없음)</option>}
            </select>
          </FormField>
          <FormField label="목표일">
            <input type="date" value={draft.targetDate} onChange={(e) => set('targetDate', e.target.value)} />
          </FormField>
        </FormGrid>
      </Section>
      <Section title="메모">
        <FormGrid>
          <FormField label="비고" wide>
            <textarea rows={2} value={draft.note} onChange={(e) => set('note', e.target.value)} />
          </FormField>
        </FormGrid>
      </Section>
    </form>
  );

  const checklist = milestone && (
    <Section
      title="체크리스트"
      aside={milestone.progress.percent !== null ? `${milestone.progress.done}/${milestone.progress.total} · ${milestone.progress.percent}%` : undefined}
    >
      {milestone.progress.total > 0 && (
        <span className="bar-track checklist-bar">
          <span className="bar-fill" style={{ width: `${milestone.progress.percent ?? 0}%` }} />
        </span>
      )}
      {current.length === 0 && <p className="muted small">항목이 없습니다.</p>}
      <ul className="check-list">
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
              type="button"
              className="icon"
              title="삭제"
              aria-label={`${it.title} 삭제`}
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
          type="button"
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
    </Section>
  );

  return (
    <DialogFrame
      label="마일스톤"
      eyebrow="마일스톤"
      code={creating ? undefined : milestone.code}
      title={creating ? '새 마일스톤' : milestone.name}
      hue={draft.phase ? phaseHue(draft.phase) : undefined}
      locked={editing}
      mode={editing ? 'edit' : 'view'}
      onClose={onClose}
      meta={
        creating ? (
          <span className="flag flag-edit">✎ 새로 만드는 중</span>
        ) : (
          <>
            {editing && <span className="flag flag-edit">✎ 편집 중</span>}
            <StatusBadge status={milestone.status} />
            {milestone.overdue && <span className="flag flag-danger">지연</span>}
            <span className="muted">
              {milestone.phase} · No. {milestone.no}
            </span>
          </>
        )
      }
      footer={
        editing ? (
          <FootBar
            key="foot-edit"
            right={
              <>
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
                <button key="btn-save" type="submit" form="milestone-form" className="primary" disabled={busy}>
                  {creating ? '추가' : '저장'}
                </button>
              </>
            }
          />
        ) : (
          <FootBar
            key="foot-view"
            left={
              <label className="foot-status">
                상태
                <StatusSelect value={milestone!.status} options={statuses} disabled={busy} onChange={changeStatus} />
              </label>
            }
            right={
              <>
                <button key="btn-delete" type="button" className="danger" onClick={remove} disabled={busy}>
                  삭제
                </button>
                <button key="btn-edit" type="button" className="primary" onClick={() => setEditing(true)} disabled={busy}>
                  편집
                </button>
              </>
            }
          />
        )
      }
    >
      {error && (
        <Banner tone="danger">
          <span>{error}</span>
        </Banner>
      )}
      {editing ? editBody : readBody}
      {checklist}
    </DialogFrame>
  );
}
