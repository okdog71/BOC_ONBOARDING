import { useEffect, useRef, useState } from 'react';
import { SEARCH_MIN, type SearchState } from './useProbabilityPage';
import { summarizeHit } from './format';
import type { SearchHit } from './types';

/** 상단 전체 검색: 내보낸 최신 적용일의 모든 표를 검색한다(2글자 이상, 입력 후 300ms 또는 엔터) */
export default function GlobalSearch({
  state,
  onSearch,
  onClear,
  onOpen,
}: {
  state: SearchState;
  onSearch: (keyword: string, page?: number) => void;
  onClear: () => void;
  onOpen: (hit: SearchHit) => void;
}) {
  const [draft, setDraft] = useState('');
  const box = useRef<HTMLDivElement>(null);
  const [focused, setFocused] = useState(false);

  // 입력이 멈추면 검색(디바운스)
  useEffect(() => {
    if (draft.trim().length < SEARCH_MIN) {
      if (state.result || state.error) onClear();
      return;
    }
    const timer = window.setTimeout(() => onSearch(draft), 300);
    return () => window.clearTimeout(timer);
    // draft 가 바뀔 때만 다시 건다
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [draft]);

  // 바깥 클릭·Esc 로 결과를 닫는다
  useEffect(() => {
    const onDown = (e: MouseEvent) => {
      if (box.current && !box.current.contains(e.target as Node)) setFocused(false);
    };
    document.addEventListener('mousedown', onDown);
    return () => document.removeEventListener('mousedown', onDown);
  }, []);

  const trimmed = draft.trim();
  const tooShort = trimmed.length > 0 && trimmed.length < SEARCH_MIN;
  const result = state.result;
  const showPanel = focused && trimmed.length > 0;

  return (
    <div className="pi-search" ref={box} role="search">
      <svg className="pi-search-icon" viewBox="0 0 24 24" width="16" height="16" aria-hidden="true">
        <circle cx="11" cy="11" r="7" fill="none" stroke="currentColor" strokeWidth="2" />
        <path d="m20 20-4-4" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
      </svg>
      <input
        value={draft}
        placeholder="전체 확률 정보에서 검색 (아이템 이름, 등급, 종류)"
        aria-label="전체 확률 정보 검색"
        onChange={(e) => setDraft(e.target.value)}
        onFocus={() => setFocused(true)}
        onKeyDown={(e) => {
          if (e.key === 'Escape') {
            setFocused(false);
          } else if (e.key === 'Enter' && !tooShort) {
            onSearch(draft);
          }
        }}
      />
      {showPanel && (
        <div className="pi-search-panel" role="region" aria-label="검색 결과" aria-live="polite">
          {tooShort && <p className="pi-search-note">검색어는 {SEARCH_MIN}글자 이상 입력해 주세요.</p>}
          {!tooShort && state.loading && <p className="pi-search-note">검색 중…</p>}
          {!tooShort && state.error && <p className="pi-search-note is-error">{state.error}</p>}
          {!tooShort && !state.loading && !state.error && result && (
            <>
              <p className="pi-search-note">
                최신 적용일 기준 <strong>{result.hits.totalElements.toLocaleString('ko-KR')}</strong>건
              </p>
              <ul>
                {result.hits.content.map((hit, i) => {
                  const s = summarizeHit(hit);
                  return (
                    <li key={`${hit.tableKey}-${i}`}>
                      <button
                        type="button"
                        onClick={() => {
                          setFocused(false);
                          onOpen(hit);
                        }}
                      >
                        <span className="pi-hit-path">{hit.path.join(' / ')}</span>
                        <span className="pi-hit-main">
                          <span>{s.name}</span>
                          <span className="pi-hit-rate">{s.rate}</span>
                        </span>
                      </button>
                    </li>
                  );
                })}
              </ul>
              {result.hits.totalElements === 0 && <p className="pi-search-note">검색 결과가 없습니다.</p>}
              {result.hits.totalPages > 1 && (
                <div className="pi-search-pager">
                  <button type="button" className="pi-btn" disabled={result.hits.page === 0} onClick={() => onSearch(draft, result.hits.page - 1)}>
                    이전
                  </button>
                  <span>
                    {result.hits.page + 1} / {result.hits.totalPages}
                  </span>
                  <button
                    type="button"
                    className="pi-btn"
                    disabled={result.hits.page + 1 >= result.hits.totalPages}
                    onClick={() => onSearch(draft, result.hits.page + 1)}
                  >
                    다음
                  </button>
                </div>
              )}
            </>
          )}
        </div>
      )}
    </div>
  );
}
