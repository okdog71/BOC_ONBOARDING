import { useMemo } from 'react';
import { useEnums } from '../hooks';
import { SheetChips } from '../links';
import { DateCell, ItemChip, itemLink, orDash } from '../resource/chips';
import ManagedPage from '../resource/ManagedPage';
import RelatedMilestones from '../resource/RelatedMilestones';
import type { ResourceConfig } from '../resource/types';
import type { SupplyDoc } from '../types';

const STATUSES = ['미요청', '요청', '수령', '해당없음'];
const EXISTENCE = ['있음', '없음', '부분', '미확인'];
const FRESHNESS = ['현행', '레거시', '혼재', '불명', '미확인'];

/** ② 수급 산출물 확인표 */
export default function SupplyDocs() {
  const enums = useEnums();
  const statuses = enums?.supplyStatus ?? STATUSES;

  const config = useMemo<ResourceConfig<SupplyDoc>>(
    () => ({
      base: '/api/supply-docs',
      route: '/supply-docs',
      title: '수급 산출물 확인표',
      sheet: '②',
      itemLabel: '산출물',
      titleField: 'name',
      groupField: 'category',
      groupLabel: '구분',
      canRenameGroup: true,
      statusField: 'supplyStatus',
      statuses,
      doneLabel: '수령',
      isDone: (r) => r.ready,
      header: <RelatedMilestones sheet="②" label="수급 산출물" />,
      searchOf: (r) => [r.name, r.expectedContent, r.usagePurpose, r.owner, r.location],
      filters: [{ label: '담당', valueOf: (r) => r.owner }],
      columns: [
        {
          header: '산출물',
          className: 'name',
          render: (r) => (
            <>
              <div>{r.name}</div>
              {r.expectedContent && <div className="sub">{r.expectedContent}</div>}
            </>
          ),
        },
        {
          header: '활용 목적',
          className: 'chips',
          render: (r) => (
            <>
              <span className="small">{r.usagePurpose}</span> <SheetChips text={r.usagePurpose} />
            </>
          ),
        },
        {
          header: '존재·최신성',
          className: 'nowrap',
          render: (r) => (
            <span className="small">
              {r.existence ?? '-'} · {r.freshness ?? '-'}
            </span>
          ),
        },
        { header: '위치', className: 'small', render: (r) => orDash(r.location) },
        { header: '담당', className: 'nowrap', render: (r) => r.owner ?? <span className="muted">미지정</span> },
        { header: '필요일', className: 'nowrap', render: (r) => <DateCell date={r.targetDate} overdue={r.overdue} /> },
        {
          header: '필요 세션(⑤)',
          className: 'chips',
          render: (r) =>
            r.requiredBySessions.length === 0 ? (
              <span className="muted">-</span>
            ) : (
              r.requiredBySessions.map((c) => <ItemChip key={c} to={itemLink('/handover-sessions', c)} label={c} title="이 산출물을 선행으로 요구하는 인수인계 세션" />)
            ),
        },
      ],
      fields: [
        { key: 'category', label: '구분', kind: 'text', required: true },
        { key: 'name', label: '산출물명', kind: 'text', required: true },
        { key: 'expectedContent', label: '기대 내용', kind: 'textarea' },
        { key: 'usagePurpose', label: '활용 목적', kind: 'textarea', hint: '⑦⑧⑨ 같은 시트 기호는 링크로 표시됩니다' },
        { key: 'supplyStatus', label: '수급 상태', kind: 'select', options: statuses, allowEmpty: false, hint: '요청일·수령일은 상태 변경 시 자동 기록' },
        { key: 'existence', label: '존재 여부', kind: 'select', options: EXISTENCE },
        { key: 'freshness', label: '최신성', kind: 'select', options: FRESHNESS },
        { key: 'location', label: '위치', kind: 'text', placeholder: 'Confluence/Git/공유드라이브 경로' },
        { key: 'owner', label: '담당', kind: 'combo', options: (rows) => [...new Set(rows.map((r) => r.owner).filter((v): v is string => Boolean(v)))] },
        { key: 'targetDate', label: '필요일', kind: 'date', hint: '이 날까지 수령' },
        { key: 'note', label: '비고', kind: 'textarea' },
      ],
      detail: (r) => (
        <>
          <dl className="detail">
            <dt>일정</dt>
            <dd>
              요청 {r.requestedDate ?? '-'} · 수령 {r.receivedDate ?? '-'}
            </dd>
            <dt>사용처</dt>
            <dd>
              <SheetChips text={r.usagePurpose} />
              {r.requiredBySessions.length === 0 && <span className="muted">이 산출물을 선행으로 요구하는 인수인계 세션이 없습니다.</span>}
              {r.requiredBySessions.map((c) => (
                <ItemChip key={c} to={itemLink('/handover-sessions', c)} label={`인수인계 ${c}`} />
              ))}
            </dd>
          </dl>
        </>
      ),
      emptyDraft: (group) => ({
        category: group ?? '',
        name: '',
        expectedContent: '',
        usagePurpose: '',
        supplyStatus: '미요청',
        existence: '',
        freshness: '',
        location: '',
        owner: '',
        targetDate: '',
        note: '',
      }),
      toDraft: (r) => ({
        category: r.category,
        name: r.name,
        expectedContent: r.expectedContent ?? '',
        usagePurpose: r.usagePurpose ?? '',
        supplyStatus: r.supplyStatus,
        existence: r.existence ?? '',
        freshness: r.freshness ?? '',
        location: r.location ?? '',
        owner: r.owner ?? '',
        targetDate: r.targetDate ?? '',
        note: r.note ?? '',
      }),
    }),
    [statuses],
  );

  return <ManagedPage config={config} />;
}
