import { usePeek, type PeekKind } from '../peek/context';

/**
 * 다른 메뉴의 항목을 가리키는 칩. 누르면 페이지 이동 없이 그 항목의 확인 팝업이 현재 화면 위에 열린다.
 * ready=false 면 선행 미충족을 색으로 알린다.
 */
export function ItemChip({ peek, label, ready, title }: { peek: { kind: PeekKind; code: string }; label: string; ready?: boolean; title?: string }) {
  const { open } = usePeek();
  const cls = ready === undefined ? 'chip' : ready ? 'chip chip-ok' : 'chip chip-wait';
  return (
    <button
      type="button"
      className={cls}
      title={title ? `${title} — 눌러서 확인` : '눌러서 확인'}
      onClick={(e) => {
        e.stopPropagation();
        open(peek.kind, peek.code);
      }}
    >
      {label}
    </button>
  );
}

/** 코드 칩: /route?code=CODE 로 이동해 상세 팝업을 연다 */
export const itemLink = (route: string, code: string) => `${route}?code=${encodeURIComponent(code)}`;

/** 날짜 셀: 지연이면 강조 */
export function DateCell({ date, overdue }: { date: string | null; overdue?: boolean }) {
  if (!date) return <span className="muted">-</span>;
  return (
    <span className={overdue ? 'overdue' : ''}>
      {date}
      {overdue && ' ⚠'}
    </span>
  );
}

/** 값이 없으면 흐리게 "-" */
export const orDash = (v: string | null | undefined) => (v ? v : <span className="muted">-</span>);

/** YYYY-MM-DD → MM-DD (좁은 칸용). 없으면 '-' */
export const md = (d: string | null | undefined) => (d ? d.slice(5) : '-');
