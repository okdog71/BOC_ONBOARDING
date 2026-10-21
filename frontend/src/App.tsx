import { Suspense, lazy, useEffect, useState } from 'react';
import { Link, NavLink, Navigate, Route, Routes, useLocation, useSearchParams } from 'react-router-dom';
import { useAuth } from './auth';
import Login from './pages/Login';
import PeekHost from './peek/PeekHost';
import UserMenu from './ui/UserMenu';
import { invalidateUsers } from './users';
import { useTheme } from './theme';
import { NavIcon, ThemeIcon } from './navIcons';
import { HOME_PATH, NAV, READY_NAV, findNav } from './nav';

// 이용자(대고객)용 확률 정보 화면 - 로그인 없이 열리는 공개 화면이라 인증 게이트·관리 셸 밖에서 렌더한다.
// 관리 메뉴(nav.tsx)에는 넣지 않고, 상단바의 "확률형아이템" 링크(새 탭)로만 연결한다.
const PUBLIC_PROBABILITY_PATH = '/probability';
const ProbabilityPage = lazy(() => import('./site/probability/ProbabilityPage'));

const NARROW = '(max-width: 900px)';
const STORAGE_KEY = 'boc.nav.open';

function readStoredOpen(): boolean {
  try {
    const v = localStorage.getItem(STORAGE_KEY);
    if (v !== null) return v === '1';
  } catch {
    /* 저장소 접근 불가 시 기본값 */
  }
  return true;
}

function useIsNarrow(): boolean {
  const [narrow, setNarrow] = useState(() => window.matchMedia(NARROW).matches);
  useEffect(() => {
    const mq = window.matchMedia(NARROW);
    const on = () => setNarrow(mq.matches);
    mq.addEventListener('change', on);
    return () => mq.removeEventListener('change', on);
  }, []);
  return narrow;
}

/** 현재 위치: 홈 › 메뉴 › (선택된 항목 코드) */
function Breadcrumb() {
  const { pathname } = useLocation();
  const [params] = useSearchParams();
  const item = findNav(pathname);
  if (!item) return null;
  const code = params.get('code');
  const crumbs: { label: string; to?: string }[] = [];
  if (item.path !== HOME_PATH) crumbs.push({ label: '홈', to: HOME_PATH });
  crumbs.push({ label: item.label, to: code ? item.path : undefined });
  if (code) crumbs.push({ label: code });
  return (
    <nav className="breadcrumb" aria-label="현재 위치">
      <ol>
        {crumbs.map((c, i) => (
          <li key={i} aria-current={i === crumbs.length - 1 ? 'page' : undefined}>
            {c.to ? <Link to={c.to}>{c.label}</Link> : c.label}
          </li>
        ))}
      </ol>
    </nav>
  );
}

/** 로그인하지 않았으면 로그인 화면, 로그인했으면 앱 본체 */
export default function App() {
  const { user, loading } = useAuth();
  const { pathname } = useLocation();
  // 로그아웃·세션 만료 때 사용자 캐시를 비운다(다른 사람으로 로그인해도 이전 목록이 남지 않게)
  useEffect(() => {
    if (!user) invalidateUsers(false);
  }, [user]);
  if (pathname === PUBLIC_PROBABILITY_PATH || pathname.startsWith(`${PUBLIC_PROBABILITY_PATH}/`)) {
    return (
      <Suspense fallback={<p className="muted boot-msg" role="status">불러오는 중…</p>}>
        <ProbabilityPage />
      </Suspense>
    );
  }
  if (loading) return <p className="muted boot-msg" role="status">불러오는 중…</p>;
  if (!user) return <Login />;
  return <Shell />;
}

function Shell() {
  const narrow = useIsNarrow();
  const theme = useTheme();
  const [open, setOpen] = useState<boolean>(() => (window.matchMedia(NARROW).matches ? false : readStoredOpen()));
  const { pathname } = useLocation();

  // 좁은 화면에서는 이동하면 드로어를 닫는다
  useEffect(() => {
    if (narrow) setOpen(false);
  }, [pathname, narrow]);

  useEffect(() => {
    if (!narrow) setOpen(readStoredOpen());
  }, [narrow]);

  useEffect(() => {
    if (!(narrow && open)) return;
    const onKey = (e: KeyboardEvent) => e.key === 'Escape' && setOpen(false);
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [narrow, open]);

  const toggle = () => {
    const next = !open;
    setOpen(next);
    if (!narrow) {
      try {
        localStorage.setItem(STORAGE_KEY, next ? '1' : '0');
      } catch {
        /* 무시 */
      }
    }
  };

  const current = findNav(pathname);
  useEffect(() => {
    document.title = current ? `${current.label} · BOC 온보딩 트래커` : 'BOC 온보딩 트래커';
  }, [current]);

  return (
    <PeekHost>
    <div className={`shell${open ? ' nav-open' : ''}`}>
      <a className="skip-link" href="#main">
        본문으로 건너뛰기
      </a>
      <header className="topbar">
        <button
          type="button"
          className="menu-toggle"
          aria-label={open ? '메뉴 접기' : '메뉴 펼치기'}
          aria-expanded={open}
          aria-controls="lnb"
          onClick={toggle}
        >
          ☰
        </button>
        <Link className="brand" to={HOME_PATH}>
          <span className="brand-mark" aria-hidden="true">B</span>
          <span className="brand-text">
            <strong>BOC</strong>
            <span>온보딩·인수인계 트래커</span>
          </span>
        </Link>
        <span className="topbar-sep" aria-hidden="true" />
        <Breadcrumb />
        <a
          className="topbar-link"
          href={PUBLIC_PROBABILITY_PATH}
          target="_blank"
          rel="noopener noreferrer"
          title="이용자용 확률 정보 화면 (새 탭)"
        >
          확률형아이템
        </a>
        <button
          type="button"
          className="theme-toggle"
          onClick={theme.toggle}
          title={`${theme.nextLabel} 모드로 전환`}
          aria-label={`${theme.nextLabel} 모드로 전환`}
        >
          <ThemeIcon dark={theme.theme === 'dark'} />
        </button>
        <UserMenu />
      </header>
      <aside id="lnb" className="sidebar" aria-hidden={!open}>
        <nav aria-label="주 메뉴">
          <p className="nav-title">메뉴</p>
          <ul>
            {READY_NAV.map((n) => (
              <li key={n.path}>
                <NavLink to={n.path} title={n.sheet ? `${n.sheet} 시트` : undefined} tabIndex={open ? 0 : -1}>
                  <NavIcon path={n.path} />
                  <span>{n.label}</span>
                </NavLink>
              </li>
            ))}
          </ul>
        </nav>
      </aside>
      {narrow && open && <div className="nav-backdrop" onClick={() => setOpen(false)} aria-hidden="true" />}
      <main id="main" tabIndex={-1}>
        <Suspense fallback={<p className="muted" role="status">불러오는 중…</p>}>
          <Routes>
            <Route path="/" element={<Navigate to={HOME_PATH} replace />} />
            {NAV.filter((n) => n.component).flatMap((n) => {
              const Page = n.component!;
              return [
                <Route key={n.path} path={n.path} element={<Page />} />,
                ...(n.aliases ?? []).map((a) => <Route key={a} path={a} element={<Navigate to={n.path} replace />} />),
              ];
            })}
            <Route path="*" element={<Navigate to={HOME_PATH} replace />} />
          </Routes>
        </Suspense>
      </main>
    </div>
    </PeekHost>
  );
}
