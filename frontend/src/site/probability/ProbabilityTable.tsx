import { useEffect, useState } from 'react';
import { DEFAULT_SIZE, FULL_SIZE, type Section } from './useProbabilityPage';
import { formatCell, formatParentRate, kindLabel, pageWindow, visibleColumns } from './format';
import { leafTitle } from './tree';
import type { TableIndex } from './types';

/**
 * 확률표 한 구역: 머리말(제목·종류·행 수·상위 표 확률·표 안 검색) + 설명 + 표 + 페이징("전체 보기").
 * 선택한 표 아래의 하위 표도 같은 구역으로 쌓이며, 구역마다 검색·페이지·전체 보기를 따로 갖는다.
 */
export default function ProbabilityTable({
  section,
  index,
  tables,
  isRoot,
  onSearch,
  onPage,
  onSize,
  onSelectTable,
}: {
  section: Section;
  /** 이 구역 표의 LNB 항목(아직 불러오는 중이어도 제목을 보여주기 위해) */
  index: TableIndex | undefined;
  tables: TableIndex[];
  /** 선택한 표(첫 구역)면 제목은 페이지 제목(H1)이 대신하므로 구역 제목을 그리지 않는다 */
  isRoot: boolean;
  onSearch: (keyword: string) => void;
  onPage: (page: number) => void;
  onSize: (size: number) => void;
  onSelectTable: (tableKey: string) => void;
}) {
  const { table, keyword, size, loading, error } = section;
  const [draft, setDraft] = useState(keyword);
  useEffect(() => setDraft(keyword), [keyword, section.tableKey]);

  const title = index ? leafTitle(index) : section.tableKey;
  const heading = isRoot ? null : (
    <h2 className="pi-section-title" id={`pi-sec-${index?.id ?? section.tableKey}`}>
      {title}
    </h2>
  );

  if (!table) {
    return (
      <section className={`pi-section${isRoot ? '' : ' is-child'}`} aria-busy={loading}>
        {heading}
        {error ? (
          <p className="pi-inline-error" role="alert">
            {error}
          </p>
        ) : (
          <p className="pi-muted" role="status">
            불러오는 중…
          </p>
        )}
      </section>
    );
  }

  const columns = visibleColumns(table.columns, table.rows);
  const parent = table.parentKey ? tables.find((t) => t.tableKey === table.parentKey) : undefined;
  const first = table.totalElements === 0 ? 0 : table.page * table.size + 1;
  const last = table.page * table.size + table.rows.length;
  const lastPage = table.page + 1 >= table.totalPages;
  const full = size === FULL_SIZE;

  return (
    <section className={`pi-section${isRoot ? '' : ' is-child'}`} aria-busy={loading}>
      {heading}
      <div className="pi-section-head">
        <span className="pi-chip">{kindLabel(table.kind)}</span>
        <span className="pi-count">
          {table.totalElements.toLocaleString('ko-KR')}행{keyword ? ' (검색 결과)' : ''}
        </span>
        {parent && table.parentRate !== null && (
          <span className="pi-parent">
            상위 표{' '}
            <button type="button" className="pi-link" onClick={() => onSelectTable(parent.tableKey)}>
              {leafTitle(parent)}
            </button>
            에서 {formatParentRate(table.parentRate)} 확률로 선택됩니다
          </span>
        )}
        <form
          className="pi-inline-search"
          role="search"
          onSubmit={(e) => {
            e.preventDefault();
            onSearch(draft);
          }}
        >
          <input value={draft} onChange={(e) => setDraft(e.target.value)} placeholder="이 표에서 검색" aria-label={`${title}에서 검색`} />
          {keyword && (
            <button
              type="button"
              className="pi-btn"
              onClick={() => {
                setDraft('');
                onSearch('');
              }}
            >
              지우기
            </button>
          )}
        </form>
      </div>

      {table.description && <p className="pi-section-desc">{table.description}</p>}
      {error && (
        <p className="pi-inline-error" role="alert">
          {error}
        </p>
      )}

      <div className={`pi-table-wrap${loading ? ' is-loading' : ''}`}>
        <table className="pi-table">
          <thead>
            <tr>
              {columns.map((c) => (
                <th key={c.key} scope="col" className={c.type === 'text' ? '' : 'num'}>
                  {c.label}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {table.rows.map((row, i) => (
              <tr key={i}>
                {columns.map((c) => {
                  const text = formatCell(row, c);
                  return (
                    <td key={c.key} className={c.type === 'text' ? '' : c.type === 'percent' ? 'num pct' : 'num'}>
                      {text === '--' ? <span className="pi-empty">--</span> : text}
                    </td>
                  );
                })}
              </tr>
            ))}
            {table.rows.length === 0 && (
              <tr>
                <td colSpan={Math.max(columns.length, 1)} className="pi-none">
                  조건에 맞는 행이 없습니다.
                </td>
              </tr>
            )}
          </tbody>
          {lastPage && !keyword && table.declaredTotalText && (
            <tfoot>
              <tr>
                <td colSpan={Math.max(columns.length - 1, 1)}>합계</td>
                <td className="num pct">{table.declaredTotalText}</td>
              </tr>
            </tfoot>
          )}
        </table>
      </div>

      <div className="pi-pager">
        {table.totalPages > 1 && (
          <div className="pi-pages" role="navigation" aria-label={`${title} 페이지`}>
            <button type="button" className="pi-page" disabled={table.page === 0 || loading} onClick={() => onPage(table.page - 1)} aria-label="이전 페이지">
              ‹
            </button>
            {pageWindow(table.page, table.totalPages).map((p, i) =>
              p === null ? (
                <span key={`gap${i}`} className="pi-gap" aria-hidden="true">
                  …
                </span>
              ) : (
                <button
                  key={p}
                  type="button"
                  className={`pi-page${p === table.page ? ' is-active' : ''}`}
                  aria-current={p === table.page ? 'page' : undefined}
                  disabled={loading}
                  onClick={() => onPage(p)}
                >
                  {p + 1}
                </button>
              ),
            )}
            <button type="button" className="pi-page" disabled={lastPage || loading} onClick={() => onPage(table.page + 1)} aria-label="다음 페이지">
              ›
            </button>
          </div>
        )}
        <span className="pi-range">
          {first.toLocaleString('ko-KR')}–{last.toLocaleString('ko-KR')} / {table.totalElements.toLocaleString('ko-KR')}행
        </span>
        {table.totalElements > DEFAULT_SIZE && (
          <button type="button" className="pi-btn pi-full" disabled={loading} onClick={() => onSize(full ? DEFAULT_SIZE : FULL_SIZE)}>
            {full ? `${DEFAULT_SIZE}행씩 보기` : '전체 보기'}
          </button>
        )}
      </div>
    </section>
  );
}
