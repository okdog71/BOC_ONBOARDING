import type { ReactNode } from 'react';

export function StatusBadge({ status }: { status: string }) {
  return <span className={`badge s-${status}`}>{status}</span>;
}

/** 상태 변경 드롭다운. 변경 시 onChange 로 새 값을 알린다. */
export function StatusSelect({
  value,
  options,
  disabled,
  onChange,
}: {
  value: string;
  options: string[];
  disabled?: boolean;
  onChange: (v: string) => void;
}) {
  return (
    <select className={`s-${value}`} value={value} disabled={disabled} onChange={(e) => onChange(e.target.value)}>
      {options.map((o) => (
        <option key={o} value={o}>
          {o}
        </option>
      ))}
    </select>
  );
}

export function FilterSelect({
  label,
  value,
  options,
  onChange,
}: {
  label: string;
  value: string;
  options: string[];
  onChange: (v: string) => void;
}) {
  return (
    <label className="filter">
      {label}
      <select value={value} onChange={(e) => onChange(e.target.value)}>
        <option value="">전체</option>
        {options.map((o) => (
          <option key={o} value={o}>
            {o}
          </option>
        ))}
      </select>
    </label>
  );
}

export function Pager({
  page,
  totalPages,
  totalElements,
  onPage,
}: {
  page: number;
  totalPages: number;
  totalElements: number;
  onPage: (p: number) => void;
}) {
  return (
    <div className="pager">
      <button disabled={page <= 0} onClick={() => onPage(page - 1)}>
        이전
      </button>
      <span>
        {totalPages === 0 ? 0 : page + 1} / {totalPages} 페이지 · 총 {totalElements}건
      </span>
      <button disabled={page + 1 >= totalPages} onClick={() => onPage(page + 1)}>
        다음
      </button>
    </div>
  );
}

export function State({ loading, error, children }: { loading: boolean; error?: string; children: ReactNode }) {
  if (error) return <p className="error">오류: {error}</p>;
  return <>{loading ? <p className="muted">불러오는 중…</p> : null}{children}</>;
}
