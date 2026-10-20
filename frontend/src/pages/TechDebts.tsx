import { useMemo } from 'react';
import { useEnums } from '../hooks';
import { DateCell, orDash } from '../resource/chips';
import FilesSection from '../resource/FilesSection';
import ManagedPage from '../resource/ManagedPage';
import type { ResourceConfig } from '../resource/types';
import type { TechDebt } from '../types';

const PRIORITIES = ['P1', 'P2', 'P3', 'P4'];
const SYSTEMS = ['KEC', 'BOC', '공통'];
const STATUSES = ['미착수', '확인중', '진행중', '검증중', '완료', '보류'];

/** 우선순위 정의(관리대장 범례 시트) */
const PRIORITY_HINT: Record<string, string> = {
  P1: '즉시 대응',
  P2: '단기 대응',
  P3: '중기 대응',
  P4: '위생 항목',
};
const PRIORITY_DESC: Record<string, string> = {
  P1: 'LIVE 장애·중복지급·보안 노출 직결',
  P2: '데이터 정합성·미검증 리스크(SOW §11 Acceptance 직결)',
  P3: '부채 정리·회귀 안전망',
  P4: '유지보수성(backlog 소화)',
};
/** 위험할수록 붉게, 덜 급할수록 푸르게 */
const PRIORITY_HUE: Record<string, number> = { P1: 4, P2: 28, P3: 222, P4: 165 };

const uniq = (values: (string | null)[]) => [...new Set(values.filter((v): v is string => Boolean(v)))];

/** 기술부채 관리대장 */
export default function TechDebts() {
  const enums = useEnums();
  const statuses = enums?.techDebtStatus ?? STATUSES;
  const priorities = enums?.techDebtPriority ?? PRIORITIES;
  const systems = enums?.techDebtSystem ?? SYSTEMS;

  const config = useMemo<ResourceConfig<TechDebt>>(
    () => ({
      base: '/api/tech-debts',
      route: '/tech-debts',
      title: '기술부채 관리대장',
      sheet: '',
      itemLabel: '부채',
      titleField: 'name',
      groupField: 'priority',
      groupLabel: '우선순위',
      fixedGroups: priorities,
      groupHue: (g) => PRIORITY_HUE[g] ?? 222,
      groupHint: (g) => PRIORITY_HINT[g],
      canRenameGroup: false,
      statusField: 'status',
      statuses,
      doneLabel: '완료',
      isDone: (r) => r.done,
      export: { groupParam: 'priority', filterParams: ['system', 'owner'] },
      notice: '계정·비밀번호·토큰 등 민감정보와 개인정보는 입력하지 마세요. 근거 자료는 첨부 파일로 보관합니다.',
      header: (
        <div className="legend-bar" aria-label="우선순위 정의">
          {priorities.map((p) => (
            <span key={p}>
              <b>{p}</b>
              {PRIORITY_HINT[p]} — {PRIORITY_DESC[p]}
            </span>
          ))}
        </div>
      ),
      searchOf: (r) => [r.name, r.summary, r.impact, r.area, r.owner, r.confirmWith, r.system],
      filters: [
        { label: '시스템', valueOf: (r) => r.system },
        { label: '담당', valueOf: (r) => r.owner },
      ],
      columns: [
        {
          header: '항목',
          className: 'name',
          render: (r) => (
            <>
              <div>
                {r.name}
                {r.files.length > 0 && (
                  <span className="attach-count" title="첨부 파일">
                    📎 {r.files.length}
                  </span>
                )}
              </div>
              {r.summary && <div className="sub">{r.summary}</div>}
            </>
          ),
        },
        {
          header: '시스템 · 영역',
          className: 'nowrap small',
          render: (r) => (
            <>
              <b>{r.system}</b> · {orDash(r.area)}
            </>
          ),
        },
        { header: '리스크 · 영향', className: 'small', render: (r) => <span className="clip">{orDash(r.impact)}</span> },
        { header: '확인 필요처', className: 'small', render: (r) => orDash(r.confirmWith) },
        { header: '담당', className: 'nowrap', render: (r) => r.owner ?? <span className="muted">미지정</span> },
        { header: '목표 시점', className: 'nowrap', render: (r) => <DateCell date={r.targetDate} overdue={r.overdue} /> },
      ],
      fields: [
        { key: 'name', label: '항목명', kind: 'text', required: true, wide: true },
        { key: 'priority', label: '우선순위', kind: 'select', options: priorities, allowEmpty: false, required: true, hint: '바꾸면 그 우선순위의 맨 뒤로 이동(코드는 그대로)' },
        { key: 'system', label: '시스템', kind: 'select', options: systems, allowEmpty: false, required: true, hint: '코드 접두(KEC-NN)가 됩니다' },
        { key: 'area', label: '영역', kind: 'combo', options: (rows) => uniq(rows.map((r) => r.area)), placeholder: '예: 연동/장애, 보안' },
        { key: 'summary', label: '내용 요약', kind: 'textarea', section: '내용' },
        { key: 'impact', label: '리스크 · 영향', kind: 'textarea', section: '내용' },
        { key: 'basis', label: '근거 (문서·조항)', kind: 'text', section: '내용', placeholder: '예: KEC 가이드 §5-1' },
        { key: 'remedy', label: '대응(처리) 방안', kind: 'textarea', section: '내용' },
        { key: 'status', label: '상태', kind: 'select', options: statuses, allowEmpty: false, section: '진행', hint: '완료로 바꾸려면 담당이 필요합니다(완료일은 자동 기록)' },
        { key: 'owner', label: '담당', kind: 'user', section: '진행' },
        { key: 'targetDate', label: '목표 시점', kind: 'date', section: '진행' },
        { key: 'confirmWith', label: '확인 필요처', kind: 'text', section: '확인 · 검증', placeholder: '문의할 대상·창구' },
        { key: 'verifyCriteria', label: '검증 기준 (SOW §11 연계)', kind: 'textarea', section: '확인 · 검증' },
        { key: 'note', label: '비고', kind: 'textarea', section: '메모' },
      ],
      facts: (r) => [
        { label: '상태', value: r.status, tone: r.done ? 'ok' : r.overdue ? 'danger' : undefined },
        { label: '우선순위', value: `${r.priority} · ${PRIORITY_HINT[r.priority] ?? ''}`, tone: r.priority === 'P1' ? 'danger' : r.priority === 'P2' ? 'warn' : undefined },
        { label: '목표 시점', value: r.targetDate ?? '미정', tone: r.overdue ? 'danger' : r.targetDate ? undefined : 'muted' },
        { label: '담당', value: r.owner ?? '미지정', tone: r.owner ? undefined : 'warn' },
        { label: r.done ? '완료일' : '최종 업데이트', value: r.done ? (r.completedDate ?? '-') : r.updatedAt.slice(0, 10) },
        { label: '첨부', value: r.files.length > 0 ? `${r.files.length}개` : '없음', tone: r.files.length > 0 ? 'ok' : 'muted' },
      ],
      detail: (r, ctx) => <FilesSection doc={r} base="/api/tech-debts" noun="항목" title="첨부 파일 (근거 자료)" onChanged={ctx.reload} />,
      emptyDraft: (group) => ({
        priority: group ?? 'P3',
        system: 'KEC',
        area: '',
        name: '',
        summary: '',
        impact: '',
        basis: '',
        remedy: '',
        status: '미착수',
        owner: '',
        targetDate: '',
        confirmWith: '',
        verifyCriteria: '',
        note: '',
      }),
      toDraft: (r) => ({
        priority: r.priority,
        system: r.system,
        area: r.area ?? '',
        name: r.name,
        summary: r.summary ?? '',
        impact: r.impact ?? '',
        basis: r.basis ?? '',
        remedy: r.remedy ?? '',
        status: r.status,
        owner: r.owner ?? '',
        targetDate: r.targetDate ?? '',
        confirmWith: r.confirmWith ?? '',
        verifyCriteria: r.verifyCriteria ?? '',
        note: r.note ?? '',
      }),
    }),
    [statuses, priorities, systems],
  );

  return <ManagedPage config={config} />;
}
