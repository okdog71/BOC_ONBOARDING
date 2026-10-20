import { useMemo } from 'react';
import { useEnums } from '../hooks';
import { md, orDash } from '../resource/chips';
import FilesSection from '../resource/FilesSection';
import ManagedPage from '../resource/ManagedPage';
import type { ResourceConfig } from '../resource/types';
import type { HandoverQuestion } from '../types';

const PRIORITIES = ['높음', '중', '낮음'];
const STATUSES = ['미확인', '진행중', '확인완료', '보류'];
const PRIORITY_HINT: Record<string, string> = {
  높음: '실지급·개인정보·보안·장애 리스크',
  중: '호출 경로·사양',
  낮음: '문서화·명칭',
};
const PRIORITY_HUE: Record<string, number> = { 높음: 4, 중: 28, 낮음: 222 };

const uniq = (values: (string | null)[]) => [...new Set(values.filter((v): v is string => Boolean(v)))];

/** 인수인계 질의(KEC·BOC 인수인계 체크 질문지) */
export default function HandoverQuestions() {
  const enums = useEnums();
  const statuses = enums?.questionStatus ?? STATUSES;
  const priorities = enums?.questionPriority ?? PRIORITIES;

  const config = useMemo<ResourceConfig<HandoverQuestion>>(
    () => ({
      base: '/api/handover-questions',
      route: '/handover-questions',
      title: '인수인계 질의',
      sheet: '',
      itemLabel: '질문',
      titleField: 'question',
      groupField: 'priority',
      groupLabel: '우선순위',
      fixedGroups: priorities,
      groupHue: (g) => PRIORITY_HUE[g] ?? 222,
      groupHint: (g) => PRIORITY_HINT[g],
      canRenameGroup: false,
      statusField: 'status',
      statuses,
      doneLabel: '확인완료',
      isDone: (r) => r.done,
      hideOverdue: true,
      export: { groupParam: 'priority', filterParams: ['kind', 'askTo'] },
      notice: '확인 답변에는 계정·비밀번호·토큰 등 민감정보와 개인정보를 적지 마세요. 증빙은 첨부 파일로 보관합니다.',
      header: (
        <div className="legend-bar">
          <span>
            <b>사용법</b>질문 → 확인 대상에게 문의 → 「확인 답변」 기입 → 상태 변경 → 「구성도 반영 방법」 적용
          </span>
          <span className="warn-text">
            <b>가설</b>확인 전 추정입니다. 사실로 취급하지 말고 실제 답으로 대체하세요
          </span>
        </div>
      ),
      searchOf: (r) => [r.question, r.relatedNode, r.askTo, r.kind, r.answer, r.basis],
      filters: [
        { label: '구분', valueOf: (r) => r.kind },
        { label: '확인 대상', valueOf: (r) => r.askTo },
      ],
      columns: [
        {
          header: '질문',
          className: 'name',
          render: (r) => (
            <>
              <div>
                {r.question}
                {r.files.length > 0 && (
                  <span className="attach-count" title="첨부 파일">
                    📎 {r.files.length}
                  </span>
                )}
              </div>
              {r.relatedNode && <div className="sub">{r.relatedNode}</div>}
            </>
          ),
        },
        { header: '구분', className: 'nowrap small', render: (r) => orDash(r.kind) },
        { header: '확인 대상', className: 'small', render: (r) => orDash(r.askTo) },
        { header: '확인 답변', className: 'small', render: (r) => (r.answer ? <span className="clip">{r.answer}</span> : <span className="muted">-</span>) },
        {
          header: '확인자 · 확인일',
          className: 'nowrap small',
          render: (r) => (r.confirmedBy || r.confirmedDate ? `${r.confirmedBy ?? '-'} · ${md(r.confirmedDate)}` : <span className="muted">-</span>),
        },
      ],
      fields: [
        { key: 'question', label: '질문', kind: 'textarea', required: true, wide: true },
        { key: 'priority', label: '우선순위', kind: 'select', options: priorities, allowEmpty: false, required: true, hint: '바꾸면 그 우선순위의 맨 뒤로 이동(코드는 그대로)' },
        { key: 'kind', label: '구분', kind: 'combo', options: (rows) => uniq(rows.map((r) => r.kind)), placeholder: '예: 추정 I/F, 확인필요 #1' },
        { key: 'relatedNode', label: '관련 I/F·노드', kind: 'text', placeholder: '예: IF-29 ec-admin → kecdata' },
        { key: 'askTo', label: '확인 대상', kind: 'combo', options: (rows) => uniq(rows.map((r) => r.askTo)), placeholder: '역할·조직(개인 이름 대신)' },
        { key: 'basis', label: '근거 (문서·스웨거)', kind: 'textarea', section: '근거 · 가설' },
        { key: 'hypothesis', label: '가설 (사실 아님)', kind: 'textarea', section: '근거 · 가설', hint: '확인 전의 추정입니다. 사실처럼 인용하지 마세요' },
        { key: 'reflectPlan', label: '구성도 반영 방법', kind: 'textarea', section: '근거 · 가설' },
        { key: 'status', label: '상태', kind: 'select', options: statuses, allowEmpty: false, section: '확인 결과', hint: '확인완료는 확인 답변·확인자가 필요합니다(확인일은 자동 기록)' },
        { key: 'answer', label: '확인 답변', kind: 'textarea', section: '확인 결과', placeholder: '실제로 확인한 내용(가설과 구분)' },
        { key: 'confirmedBy', label: '확인자', kind: 'user', section: '확인 결과' },
        { key: 'confirmedDate', label: '확인일', kind: 'date', section: '확인 결과', hint: '비워 두면 확인완료 시 오늘로 기록' },
        { key: 'note', label: '비고', kind: 'textarea', section: '메모' },
      ],
      facts: (r) => [
        { label: '상태', value: r.status, tone: r.done ? 'ok' : undefined },
        { label: '우선순위', value: r.priority, tone: r.priority === '높음' ? 'danger' : r.priority === '중' ? 'warn' : undefined },
        { label: '확인 대상', value: r.askTo ?? '미정', tone: r.askTo ? undefined : 'warn' },
        { label: '확인자 · 확인일', value: r.confirmedBy || r.confirmedDate ? `${r.confirmedBy ?? '-'} · ${md(r.confirmedDate)}` : '-', tone: r.done ? 'ok' : 'muted' },
        { label: '첨부', value: r.files.length > 0 ? `${r.files.length}개` : '없음', tone: r.files.length > 0 ? 'ok' : 'muted' },
      ],
      detail: (r, ctx) => <FilesSection doc={r} base="/api/handover-questions" noun="질문" title="첨부 파일 (답변 증빙)" onChanged={ctx.reload} />,
      emptyDraft: (group) => ({
        priority: group ?? '중',
        kind: '',
        relatedNode: '',
        question: '',
        basis: '',
        askTo: '',
        hypothesis: '',
        reflectPlan: '',
        status: '미확인',
        answer: '',
        confirmedBy: '',
        confirmedDate: '',
        note: '',
      }),
      toDraft: (r) => ({
        priority: r.priority,
        kind: r.kind ?? '',
        relatedNode: r.relatedNode ?? '',
        question: r.question,
        basis: r.basis ?? '',
        askTo: r.askTo ?? '',
        hypothesis: r.hypothesis ?? '',
        reflectPlan: r.reflectPlan ?? '',
        status: r.status,
        answer: r.answer ?? '',
        confirmedBy: r.confirmedBy ?? '',
        confirmedDate: r.confirmedDate ?? '',
        note: r.note ?? '',
      }),
    }),
    [statuses, priorities],
  );

  return <ManagedPage config={config} />;
}
