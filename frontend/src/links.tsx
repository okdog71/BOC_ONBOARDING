import { Link } from 'react-router-dom';

/**
 * 엑셀 시트 기호(②~⑬) → 화면 라우트 매핑. 단일 출처.
 * ready=false 인 화면은 아직 프런트가 없어 "준비 중" 칩으로 표시하고, 화면이 생기면 true 로 바꾼다.
 * (안정화되면 백엔드 /api/meta/links 로 이관 예정)
 */
export const SHEET_LINKS: Record<string, { label: string; to: string; ready: boolean }> = {
  '②': { label: '수급 산출물', to: '/supply-docs', ready: false },
  '③': { label: '접근권한', to: '/access-items', ready: false },
  '④': { label: '문서 신뢰도', to: '/doc-reliabilities', ready: false },
  '⑤': { label: '인수인계 일정', to: '/handover-sessions', ready: true },
  '⑥': { label: '캐치업', to: '/catchup-actions', ready: false },
  '⑦': { label: '메뉴 인벤토리', to: '/menu-inventories', ready: false },
  '⑧': { label: '메가프로세스', to: '/mega-processes', ready: false },
  '⑨': { label: '연동 인터페이스', to: '/interface-links', ready: false },
  '⑩': { label: '분석 카드', to: '/analysis-cards', ready: false },
  '⑪': { label: '이슈 큐', to: '/issue-queues', ready: false },
  '⑫': { label: 'RACI', to: '/raci-assignments', ready: false },
  '⑬': { label: '회고 로그', to: '/review-logs', ready: false },
};

/** 문자열에서 시트 기호를 중복 없이 순서대로 추출 */
export function sheetSymbols(text: string | null | undefined): string[] {
  const found = (text ?? '').match(/[②-⑬]/g) ?? [];
  return [...new Set(found)];
}

export function LinkChip({ to, ready = true, label, hint }: { to: string; ready?: boolean; label: string; hint?: string }) {
  if (!ready) {
    return (
      <span className="chip chip-off" title={hint ?? '준비 중인 화면입니다'}>
        {label}
      </span>
    );
  }
  return (
    <Link className="chip" to={to} title={hint} onClick={(e) => e.stopPropagation()}>
      {label}
    </Link>
  );
}

/** 산출물 문자열에서 시트 기호를 찾아 이동 칩으로 렌더 */
export function SheetChips({ text }: { text: string | null | undefined }) {
  return (
    <>
      {sheetSymbols(text).map((sym) => {
        const l = SHEET_LINKS[sym];
        return <LinkChip key={sym} to={l.to} ready={l.ready} label={`${sym} ${l.label}`} />;
      })}
    </>
  );
}
