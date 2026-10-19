import { Link } from 'react-router-dom';

/** 다른 메뉴의 항목으로 가는 칩. ready=false 면 선행 미충족을 색으로 알린다. */
export function ItemChip({ to, label, ready, title }: { to: string; label: string; ready?: boolean; title?: string }) {
  const cls = ready === undefined ? 'chip' : ready ? 'chip chip-ok' : 'chip chip-wait';
  return (
    <Link className={cls} to={to} title={title} onClick={(e) => e.stopPropagation()}>
      {label}
    </Link>
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
