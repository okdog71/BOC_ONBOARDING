import { useCallback, useEffect, useState } from 'react';
import { ThemeIcon } from '../../navIcons';
import GlobalSearch from './GlobalSearch';
import ProbabilityTable from './ProbabilityTable';
import TableMenu from './TableMenu';
import VersionSelect from './VersionSelect';
import { CATEGORIES, useProbabilityPage } from './useProbabilityPage';
import './probability.css';

const NARROW = '(max-width: 900px)';
const THEME_KEY = 'boc.theme';

/** 이 화면은 처음 열면 다크(샘플 화면과 같은 톤). 한 번 바꾸면 그 선택을 관리 화면과 같은 키로 저장한다 */
function usePublicTheme() {
  const [dark, setDark] = useState(() => document.documentElement.dataset.theme !== 'light');
  useEffect(() => {
    if (!document.documentElement.dataset.theme) document.documentElement.dataset.theme = 'dark';
    setDark(document.documentElement.dataset.theme === 'dark');
  }, []);
  const toggle = () => {
    const next = dark ? 'light' : 'dark';
    document.documentElement.dataset.theme = next;
    try {
      localStorage.setItem(THEME_KEY, next);
    } catch {
      /* 저장소를 쓸 수 없으면 이번 화면에서만 적용 */
    }
    setDark(!dark);
  };
  return { dark, toggle };
}

function useNarrow(): boolean {
  const [narrow, setNarrow] = useState(() => window.matchMedia(NARROW).matches);
  useEffect(() => {
    const mq = window.matchMedia(NARROW);
    const on = () => setNarrow(mq.matches);
    mq.addEventListener('change', on);
    return () => mq.removeEventListener('change', on);
  }, []);
  return narrow;
}

/** 안내문은 문장 단위로 끊어 글머리 기호로 보여준다 */
function sentences(text: string): string[] {
  return text
    .split(/\n+|(?<=[.!?])\s+/)
    .map((s) => s.trim())
    .filter(Boolean);
}

/** 이용자용 확률 정보 센터(로그인 없음). 좌측 확률표 목록 · 상단 전체 검색 · 제목 옆 적용일 선택 · 본문 확률표 */
export default function ProbabilityPage() {
  const p = useProbabilityPage();
  const theme = usePublicTheme();
  const narrow = useNarrow();
  const [menuOpen, setMenuOpen] = useState(() => !window.matchMedia(NARROW).matches);

  useEffect(() => {
    document.title = '확률 정보 센터';
  }, []);
  // 좁은 화면에서는 표를 고르면 목록(드로어)을 닫는다
  const select = useCallback(
    (tableKey: string) => {
      p.selectTable(tableKey);
      if (narrow) setMenuOpen(false);
    },
    [p, narrow],
  );
  useEffect(() => {
    if (narrow) setMenuOpen(false);
  }, [narrow]);

  const d = p.data;
  // 선택한 표(첫 구역)의 LNB 항목 - 구역이 아직 불러오는 중이어도 제목·경로를 그릴 수 있다
  const rootIndex = d?.tables.find((t) => t.tableKey === d.rootKey);
  const busy = p.sections.some((s) => s.loading);

  return (
    <div className={`pi-root${menuOpen ? ' menu-open' : ''}`}>
      <a className="pi-skip" href="#pi-main">
        본문으로 건너뛰기
      </a>
      <header className="pi-header">
        <button type="button" className="pi-icon-btn" aria-label={menuOpen ? '확률표 목록 접기' : '확률표 목록 펼치기'} aria-expanded={menuOpen} aria-controls="pi-lnb" onClick={() => setMenuOpen((v) => !v)}>
          <span aria-hidden="true">☰</span>
        </button>
        <div className="pi-brand">
          <strong>
            <span className="pi-accent">확률</span> 정보 센터
          </strong>
          <span className="pi-brand-sub">KRAFTON</span>
        </div>
        <GlobalSearch state={p.search} onSearch={(k, page) => void p.runSearch(k, page)} onClear={p.clearSearch} onOpen={p.openHit} />
        <div className="pi-header-right">
          <div className="pi-seg" role="group" aria-label="플랫폼">
            {CATEGORIES.map((c) => (
              <button key={c.code} type="button" className={p.category === c.code ? 'is-active' : ''} aria-pressed={p.category === c.code} onClick={() => p.changeCategory(c.code)}>
                {c.label}
              </button>
            ))}
          </div>
          <button type="button" className="pi-icon-btn" onClick={theme.toggle} aria-label={theme.dark ? '라이트 모드로 전환' : '다크 모드로 전환'} title={theme.dark ? '라이트 모드로 전환' : '다크 모드로 전환'}>
            <ThemeIcon dark={theme.dark} />
          </button>
        </div>
      </header>

      <div className="pi-body">
        <aside id="pi-lnb" className="pi-lnb" aria-hidden={!menuOpen}>
          {d && <TableMenu tables={d.tables} selectedKey={d.rootKey} onSelect={select} />}
        </aside>
        {narrow && menuOpen && <div className="pi-backdrop" onClick={() => setMenuOpen(false)} aria-hidden="true" />}

        <div id="pi-main" className="pi-main" role="main" tabIndex={-1}>
          {p.status === 'loading' && (
            <p className="pi-state" role="status">
              불러오는 중…
            </p>
          )}
          {p.status === 'empty' && (
            <div className="pi-state" role="status">
              <strong>등록된 확률 정보가 없습니다</strong>
              <p>공개 중인 확률 정보가 준비되면 이곳에 표시됩니다.</p>
            </div>
          )}
          {p.status === 'error' && !d && (
            <div className="pi-state" role="alert">
              <strong>확률 정보를 불러오지 못했습니다</strong>
              <p>{p.error}</p>
              <button type="button" className="pi-btn" onClick={p.retry}>
                다시 시도
              </button>
            </div>
          )}

          {d && rootIndex && (
            <>
              <nav className="pi-crumb" aria-label="현재 위치">
                {rootIndex.path.map((seg, i) => (
                  <span key={i}>{seg}</span>
                ))}
              </nav>
              <div className="pi-title-row">
                <h1>{rootIndex.path[rootIndex.path.length - 1] ?? rootIndex.title}</h1>
                <VersionSelect versions={d.versions} value={d.version.version} disabled={busy} onChange={p.changeVersion} />
              </div>
              {p.notice && (
                <p className="pi-notice-bar" role="status">
                  {p.notice}
                </p>
              )}
              {d.version.description && (
                <ul className="pi-notice-box">
                  {sentences(d.version.description).map((s, i) => (
                    <li key={i}>{s}</li>
                  ))}
                </ul>
              )}
              {p.sections.map((s, i) => (
                <ProbabilityTable
                  key={s.tableKey}
                  section={s}
                  index={d.tables.find((t) => t.tableKey === s.tableKey)}
                  tables={d.tables}
                  isRoot={i === 0}
                  onSearch={(kw) => p.searchInTable(s.tableKey, kw)}
                  onPage={(page) => p.changePage(s.tableKey, page)}
                  onSize={(size) => p.changeSize(s.tableKey, size)}
                  onSelectTable={select}
                />
              ))}
            </>
          )}
        </div>
      </div>
    </div>
  );
}
