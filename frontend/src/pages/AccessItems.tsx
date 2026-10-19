import { useMemo } from 'react';
import { useEnums } from '../hooks';
import { DateCell, ItemChip, md, orDash } from '../resource/chips';
import ManagedPage from '../resource/ManagedPage';
import RelatedMilestones from '../resource/RelatedMilestones';
import type { ResourceConfig } from '../resource/types';
import { KV, KVGrid, Section } from '../ui/detail';
import type { AccessItem } from '../types';

const STATUSES = ['미신청', '신청', '승인', '거부', '보류'];

/** ③ 접근권한 체크리스트 */
export default function AccessItems() {
  const enums = useEnums();
  const statuses = enums?.accessStatus ?? STATUSES;

  const config = useMemo<ResourceConfig<AccessItem>>(
    () => ({
      base: '/api/access-items',
      route: '/access-items',
      title: '접근권한 체크리스트',
      sheet: '③',
      itemLabel: '접근권한',
      titleField: 'target',
      groupField: 'category',
      groupLabel: '구분',
      canRenameGroup: true,
      statusField: 'status',
      statuses,
      doneLabel: '승인',
      isDone: (r) => r.ready,
      notice: '계정 비밀번호·토큰·개인정보 등 민감정보는 입력하지 마세요(신청 절차와 상태만 관리합니다).',
      header: <RelatedMilestones sheet="③" label="접근권한" />,
      searchOf: (r) => [r.target, r.usage, r.envScope, r.procedure, r.owner],
      filters: [{ label: '담당', valueOf: (r) => r.owner }],
      columns: [
        {
          header: '대상',
          className: 'name',
          render: (r) => (
            <>
              <div>
                {r.target}
                {r.prerequisite && <span className="tag" title="다른 접근의 전제가 되는 항목">전제</span>}
              </div>
              {r.usage && <div className="sub">{r.usage}</div>}
            </>
          ),
        },
        { header: '환경 범위', className: 'small', render: (r) => orDash(r.envScope) },
        { header: '신청 절차', className: 'small', render: (r) => orDash(r.procedure) },
        { header: '담당', className: 'nowrap', render: (r) => r.owner ?? <span className="muted">미지정</span> },
        {
          header: '신청 → 승인',
          className: 'nowrap small',
          render: (r) => (
            <>
              {r.requestedDate ?? '-'} → {r.approvedDate ?? '-'}
              {r.leadTimeDays !== null && <span className="muted"> ({r.leadTimeDays}일)</span>}
            </>
          ),
        },
        { header: '필요일', className: 'nowrap', render: (r) => <DateCell date={r.targetDate} overdue={r.overdue} /> },
        {
          header: '필요 세션(⑤)',
          className: 'chips',
          render: (r) =>
            r.requiredBySessions.length === 0 ? (
              <span className="muted">-</span>
            ) : (
              r.requiredBySessions.map((c) => <ItemChip key={c} peek={{ kind: 'handover', code: c }} label={c} title="이 접근권한을 선행으로 요구하는 인수인계 세션" />)
            ),
        },
      ],
      fields: [
        { key: 'target', label: '대상', kind: 'text', required: true, wide: true },
        { key: 'category', label: '구분', kind: 'text', required: true },
        { key: 'usage', label: '용도', kind: 'textarea', section: '용도 · 절차', hint: "'접근 전제'를 적으면 다른 접근의 선행(전제) 항목으로 취급" },
        { key: 'envScope', label: '환경 범위', kind: 'text', section: '용도 · 절차', placeholder: 'dev / read 등' },
        { key: 'procedure', label: '신청 절차', kind: 'textarea', section: '용도 · 절차' },
        { key: 'status', label: '상태', kind: 'select', options: statuses, allowEmpty: false, section: '신청 현황' },
        { key: 'requestedDate', label: '신청일', kind: 'date', section: '신청 현황', hint: '비워 두면 상태 변경 시 자동 기록' },
        { key: 'approvedDate', label: '승인일', kind: 'date', section: '신청 현황', hint: '승인 상태에서만' },
        { key: 'targetDate', label: '필요일', kind: 'date', section: '신청 현황', hint: '투입 전까지 승인' },
        { key: 'owner', label: '담당', kind: 'combo', section: '담당 · 메모', options: (rows) => [...new Set(rows.map((r) => r.owner).filter((v): v is string => Boolean(v)))] },
        { key: 'note', label: '비고', kind: 'textarea', section: '담당 · 메모' },
      ],
      facts: (r) => [
        { label: '상태', value: r.status, tone: r.ready ? 'ok' : r.overdue ? 'danger' : undefined },
        { label: '필요일', value: r.targetDate ?? '미정', tone: r.overdue ? 'danger' : r.targetDate ? undefined : 'muted' },
        { label: '신청 → 승인', value: `${md(r.requestedDate)} → ${md(r.approvedDate)}` },
        { label: '소요', value: r.leadTimeDays !== null ? `${r.leadTimeDays}일` : '-', tone: r.leadTimeDays === null ? 'muted' : undefined },
        { label: '담당', value: r.owner ?? '미지정', tone: r.owner ? undefined : 'warn' },
      ],
      detail: (r) => (
        <Section title="연결">
          <KVGrid>
            <KV label="필요 세션 (⑤ 인수인계)" wide>
              {r.requiredBySessions.length > 0 &&
                r.requiredBySessions.map((c) => <ItemChip key={c} peek={{ kind: 'handover', code: c }} label={`인수인계 ${c}`} />)}
            </KV>
          </KVGrid>
        </Section>
      ),
      emptyDraft: (group) => ({
        category: group ?? '',
        target: '',
        usage: '',
        envScope: '',
        procedure: '',
        status: '미신청',
        requestedDate: '',
        approvedDate: '',
        targetDate: '',
        owner: '',
        note: '',
      }),
      toDraft: (r) => ({
        category: r.category,
        target: r.target,
        usage: r.usage ?? '',
        envScope: r.envScope ?? '',
        procedure: r.procedure ?? '',
        status: r.status,
        requestedDate: r.requestedDate ?? '',
        approvedDate: r.approvedDate ?? '',
        targetDate: r.targetDate ?? '',
        owner: r.owner ?? '',
        note: r.note ?? '',
      }),
    }),
    [statuses],
  );

  return <ManagedPage config={config} />;
}
