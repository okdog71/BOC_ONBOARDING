import { useMemo } from 'react';
import { api, type Page } from '../api';
import { useEnums } from '../hooks';
import { SheetChips } from '../links';
import { DateCell, ItemChip, itemLink, orDash } from '../resource/chips';
import ManagedPage from '../resource/ManagedPage';
import RelatedMilestones from '../resource/RelatedMilestones';
import type { Draft, ResourceConfig } from '../resource/types';
import type { AccessItem, HandoverSession, Milestone, SupplyDoc } from '../types';

const STATUSES = ['미착수', '예정', '진행중', '완료', '보류'];
const WEEKS = ['W1', 'W2', 'W3', 'W4'];
const CATEGORIES = ['공통', 'KEC', 'BOC'];

interface Extra {
  milestones?: Milestone[];
  supply?: SupplyDoc[];
  access?: AccessItem[];
}

/** 여러 항목 중 고르는 체크 목록(수급·접근 선행 선택용) */
function CheckPick({
  options,
  value,
  onChange,
}: {
  options: { code: string; label: string; ready: boolean }[];
  value: string[];
  onChange: (v: string[]) => void;
}) {
  if (options.length === 0) return <p className="muted small">선택할 항목이 없습니다.</p>;
  return (
    <ul className="pick-list">
      {options.map((o) => (
        <li key={o.code}>
          <label>
            <input
              type="checkbox"
              checked={value.includes(o.code)}
              onChange={(e) => onChange(e.target.checked ? [...value, o.code] : value.filter((c) => c !== o.code))}
            />
            <code>{o.code}</code> {o.label}
            <span className={o.ready ? 'badge s-완료' : 'badge s-보류'}>{o.ready ? '충족' : '미충족'}</span>
          </label>
        </li>
      ))}
    </ul>
  );
}

/** ⑤ 인수인계 일정(KT Plan) */
export default function Handovers() {
  const enums = useEnums();
  const statuses = enums?.handoverStatus ?? STATUSES;

  const config = useMemo<ResourceConfig<HandoverSession>>(
    () => ({
      base: '/api/handover-sessions',
      route: '/handover-sessions',
      title: '인수인계 일정 (KT Plan)',
      sheet: '⑤',
      itemLabel: '세션',
      titleField: 'topic',
      groupField: 'week',
      groupLabel: '주차',
      fixedGroups: WEEKS,
      canRenameGroup: false,
      statusField: 'status',
      statuses,
      doneLabel: '완료',
      isDone: (r) => r.status === '완료',
      header: <RelatedMilestones sheet="⑤" label="인수인계 일정" />,
      confirmStatus: (r, next) =>
        (next === '진행중' || next === '완료') && r.blockers.length > 0
          ? `시작 전 미충족 항목이 있습니다:\n- ${r.blockers.join('\n- ')}\n\n그래도 "${next}"로 변경할까요?`
          : null,
      searchOf: (r) => [r.domain, r.topic, r.output, r.handoverFrom, r.handoverTo, r.method],
      filters: [
        { label: '구분', valueOf: (r) => r.category },
        { label: '인수자', valueOf: (r) => r.handoverTo },
      ],
      columns: [
        { header: '구분', className: 'nowrap', render: (r) => r.category },
        {
          header: '도메인 · 주제',
          className: 'name',
          render: (r) => (
            <>
              <div>{r.domain ?? r.topic}</div>
              <div className="sub">{r.topic}</div>
            </>
          ),
        },
        { header: 'KT 단계', className: 'nowrap', render: (r) => orDash(r.ktStage) },
        {
          header: '인계 → 인수',
          className: 'small',
          render: (r) => (r.handoverFrom || r.handoverTo ? `${r.handoverFrom ?? '?'} → ${r.handoverTo ?? '?'}` : <span className="muted">미지정</span>),
        },
        {
          header: '마일스톤 · 선행',
          className: 'chips',
          render: (r) => (
            <>
              {r.milestoneCode ? (
                <ItemChip to={`/milestones?code=${encodeURIComponent(r.milestoneCode)}`} label={r.milestoneCode} title={r.milestoneName ?? ''} />
              ) : (
                <span className="chip chip-wait" title="연결된 마일스톤 없음">마일스톤 없음</span>
              )}
              {r.supplyDocs.map((d) => (
                <ItemChip key={d.code} to={itemLink('/supply-docs', d.code)} ready={d.ready} label={`② ${d.code}`} title={`${d.name} (${d.status})`} />
              ))}
              {r.accessItems.map((a) => (
                <ItemChip key={a.code} to={itemLink('/access-items', a.code)} ready={a.ready} label={`③ ${a.code}`} title={`${a.target} (${a.status})`} />
              ))}
              <SheetChips text={r.precondition} skip={['②', '③']} />
            </>
          ),
        },
        {
          header: '예정 · 완료',
          className: 'nowrap small',
          render: (r) => (
            <>
              <DateCell date={r.plannedDate} overdue={r.overdue} />
              {r.completedDate && <div className="muted">완료 {r.completedDate}</div>}
            </>
          ),
        },
      ],
      fields: [
        { key: 'week', label: '주차', kind: 'select', options: WEEKS, allowEmpty: false, required: true },
        { key: 'category', label: '구분', kind: 'select', options: CATEGORIES, allowEmpty: false, required: true },
        { key: 'domain', label: '도메인', kind: 'text' },
        { key: 'topic', label: '주제', kind: 'textarea', required: true },
        { key: 'ktStage', label: 'KT 단계', kind: 'text', hint: '설명/관찰/병행/독립 (복합: 설명·병행)' },
        { key: 'method', label: '진행 방식', kind: 'text', placeholder: '세션+실습' },
        { key: 'output', label: '산출물', kind: 'text' },
        { key: 'handoverFrom', label: '인계자', kind: 'text' },
        { key: 'handoverTo', label: '인수자', kind: 'text' },
        { key: 'status', label: '상태', kind: 'select', options: statuses, allowEmpty: false, hint: '완료일은 완료 전환 시 자동 기록' },
        { key: 'plannedDate', label: '예정일', kind: 'date' },
        {
          key: 'milestoneCode',
          label: '연결 마일스톤',
          kind: 'custom',
          hint: '이 세션이 속한 인수인계 마일스톤(⑤)',
          render: (d, set, ctx) => {
            const ms = ((ctx.extra as Extra).milestones ?? []).filter((m) => m.output?.includes('⑤'));
            return (
              <select value={String(d.milestoneCode ?? '')} onChange={(e) => set('milestoneCode', e.target.value)}>
                <option value="">(연결 안 함)</option>
                {ms.map((m) => (
                  <option key={m.code} value={m.code}>
                    {m.code} {m.name}
                  </option>
                ))}
              </select>
            );
          },
        },
        {
          key: 'supplyDocCodes',
          label: '선행 수급 산출물(②)',
          kind: 'custom',
          hint: '시작 전에 수령되어 있어야 하는 것',
          render: (d, set, ctx) => (
            <CheckPick
              options={((ctx.extra as Extra).supply ?? []).map((s) => ({ code: s.code, label: s.name, ready: s.ready }))}
              value={(d.supplyDocCodes as string[]) ?? []}
              onChange={(v) => set('supplyDocCodes', v)}
            />
          ),
        },
        {
          key: 'accessItemCodes',
          label: '선행 접근권한(③)',
          kind: 'custom',
          hint: '시작 전에 승인되어 있어야 하는 것',
          render: (d, set, ctx) => (
            <CheckPick
              options={((ctx.extra as Extra).access ?? []).map((a) => ({ code: a.code, label: a.target, ready: a.ready }))}
              value={(d.accessItemCodes as string[]) ?? []}
              onChange={(v) => set('accessItemCodes', v)}
            />
          ),
        },
        { key: 'precondition', label: '선행 조건 메모', kind: 'textarea', hint: "'전 세션 완료'를 적으면 마무리 세션으로 취급" },
        { key: 'note', label: '비고', kind: 'textarea' },
      ],
      detail: (r) => (
        <>
          {r.blockers.length > 0 && (
            <div className="blockers">
              <b>시작 전 미충족 {r.blockers.length}건</b>
              <ul>
                {r.blockers.map((b) => (
                  <li key={b}>{b}</li>
                ))}
              </ul>
            </div>
          )}
          <dl className="detail">
            <dt>마일스톤</dt>
            <dd>
              {r.milestoneCode ? (
                <ItemChip to={`/milestones?code=${encodeURIComponent(r.milestoneCode)}`} label={`${r.milestoneCode} ${r.milestoneName ?? ''}`} />
              ) : (
                <span className="muted">연결 없음</span>
              )}
            </dd>
            <dt>선행 수급(②)</dt>
            <dd>
              {r.supplyDocs.length === 0 && <span className="muted">없음</span>}
              {r.supplyDocs.map((d) => (
                <ItemChip key={d.code} to={itemLink('/supply-docs', d.code)} ready={d.ready} label={`${d.code} ${d.name} · ${d.status}`} />
              ))}
            </dd>
            <dt>선행 접근(③)</dt>
            <dd>
              {r.accessItems.length === 0 && <span className="muted">없음</span>}
              {r.accessItems.map((a) => (
                <ItemChip key={a.code} to={itemLink('/access-items', a.code)} ready={a.ready} label={`${a.code} ${a.target} · ${a.status}`} />
              ))}
            </dd>
            <dt>선행 메모</dt>
            <dd>
              {r.precondition ?? '-'} <SheetChips text={r.precondition} skip={['②', '③']} />
            </dd>
            <dt>완료일</dt>
            <dd>{r.completedDate ?? '-'}</dd>
          </dl>
        </>
      ),
      emptyDraft: (group) => ({
        week: group && WEEKS.includes(group) ? group : 'W1',
        category: '공통',
        domain: '',
        topic: '',
        ktStage: '',
        method: '',
        output: '',
        handoverFrom: '',
        handoverTo: '',
        status: '미착수',
        plannedDate: '',
        milestoneCode: '',
        supplyDocCodes: [],
        accessItemCodes: [],
        precondition: '',
        note: '',
      }),
      toDraft: (r): Draft => ({
        week: r.week,
        category: r.category,
        domain: r.domain ?? '',
        topic: r.topic,
        ktStage: r.ktStage ?? '',
        method: r.method ?? '',
        output: r.output ?? '',
        handoverFrom: r.handoverFrom ?? '',
        handoverTo: r.handoverTo ?? '',
        status: r.status,
        plannedDate: r.plannedDate ?? '',
        milestoneCode: r.milestoneCode ?? '',
        supplyDocCodes: r.supplyDocs.map((d) => d.code),
        accessItemCodes: r.accessItems.map((a) => a.code),
        precondition: r.precondition ?? '',
        note: r.note ?? '',
      }),
      loadExtra: async () => {
        const [m, s, a] = await Promise.all([
          api.get<Page<Milestone>>('/api/milestones?size=500&sort=id,asc'),
          api.get<Page<SupplyDoc>>('/api/supply-docs?size=500&sort=id,asc'),
          api.get<Page<AccessItem>>('/api/access-items?size=500&sort=id,asc'),
        ]);
        return { milestones: m.content, supply: s.content, access: a.content } satisfies Extra;
      },
    }),
    [statuses],
  );

  return <ManagedPage config={config} />;
}
