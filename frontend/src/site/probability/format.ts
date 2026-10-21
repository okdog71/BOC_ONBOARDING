import type { Column, Row, SearchHit } from './types';

const EMPTY = '--';

/** 확률 셀: 원천이 준 원문 표기("0.6500%")가 있으면 그대로(자릿수 유지), 없으면 0~1 값을 %로 환산 */
function percentCell(row: Row, column: Column): string {
  const text = row[`${column.key}Text`];
  if (typeof text === 'string' && text) return text;
  const value = row[column.key];
  if (typeof value !== 'number') return EMPTY;
  const digits = Math.min(8, Math.max(2, column.precision ?? 4));
  return `${(value * 100).toFixed(digits)}%`;
}

export function formatCell(row: Row, column: Column): string {
  if (column.type === 'percent') return percentCell(row, column);
  const value = row[column.key];
  if (value === null || value === undefined || value === '') return EMPTY;
  return typeof value === 'number' ? value.toLocaleString('ko-KR') : String(value);
}

/** 현재 페이지의 모든 행에서 값이 없는 열(주로 대체 아이템 열)은 숨긴다. percent 열은 항상 보인다 */
export function visibleColumns(columns: Column[], rows: Row[]): Column[] {
  return columns.filter(
    (c) => c.type === 'percent' || rows.some((r) => r[c.key] !== null && r[c.key] !== undefined && r[c.key] !== ''),
  );
}

/** 상위 표에서 선택될 확률(0~1) → '30%' 처럼 불필요한 0 없이 */
export function formatParentRate(rate: number): string {
  return `${Number((rate * 100).toFixed(6))}%`;
}

export function kindLabel(kind: string): string {
  if (kind === 'BOX') return '상자 구성';
  if (kind === 'REPURPOSE') return '가공 확률';
  return '확률표';
}

/** 전체 검색 결과 한 줄 요약: 첫 번째 값이 있는 문자열 열(아이템명)과 확률 */
export function summarizeHit(hit: SearchHit): { name: string; rate: string } {
  const text = hit.columns.find((c) => c.type === 'text' && typeof hit.row[c.key] === 'string' && hit.row[c.key] !== '');
  const percent = [...hit.columns].reverse().find((c) => c.type === 'percent');
  return {
    name: text ? String(hit.row[text.key]) : EMPTY,
    rate: percent ? percentCell(hit.row, percent) : EMPTY,
  };
}

/** 현재 페이지 주변만 보여주는 페이지 번호 목록(0부터). 생략은 null */
export function pageWindow(page: number, totalPages: number): (number | null)[] {
  const pages = new Set<number>([0, totalPages - 1]);
  for (let p = page - 2; p <= page + 2; p++) if (p >= 0 && p < totalPages) pages.add(p);
  const sorted = [...pages].sort((a, b) => a - b);
  const out: (number | null)[] = [];
  sorted.forEach((p, i) => {
    if (i > 0 && p - sorted[i - 1] > 1) out.push(null);
    out.push(p);
  });
  return out;
}
