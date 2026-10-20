import { lazy, type ComponentType, type LazyExoticComponent } from 'react';

/**
 * 메뉴 레지스트리 — 라우트·사이드 메뉴·브레드크럼·시트 기호(②~⑬) 링크의 단일 출처.
 * 새 화면은 여기에 한 줄을 추가/활성화한다(component 를 채우면 ready).
 * 순서는 엑셀 시트 순서이며, component 가 없는 항목은 메뉴·라우트에 나타나지 않는다(링크 칩만 "준비 중").
 */
export interface NavItem {
  path: string;
  /** 화면 이름(메뉴에는 이름만 표시) */
  label: string;
  /** 엑셀 시트 기호. 산출물 칩과 툴팁에 사용 */
  sheet?: string;
  /** 구현된 화면. 없으면 아직 준비 중 */
  component?: LazyExoticComponent<ComponentType>;
  /** 이전 경로 호환용 리다이렉트 */
  aliases?: string[];
  /** 향후 권한 제어용(현재 미사용) */
  roles?: string[];
}

export const NAV: NavItem[] = [
  { path: '/dashboard', label: '대시보드', component: lazy(() => import('./pages/Dashboard')) },
  { path: '/milestones', label: '마일스톤', component: lazy(() => import('./pages/Milestones')) },
  { path: '/supply-docs', label: '수급 산출물', sheet: '②', component: lazy(() => import('./pages/SupplyDocs')) },
  { path: '/access-items', label: '접근권한', sheet: '③', component: lazy(() => import('./pages/AccessItems')) },
  { path: '/doc-reliabilities', label: '문서 신뢰도', sheet: '④' },
  {
    path: '/handover-sessions',
    label: '인수인계',
    sheet: '⑤',
    aliases: ['/handovers'],
    component: lazy(() => import('./pages/Handovers')),
  },
  {
    path: '/handover-questions',
    label: '인수인계 질의',
    component: lazy(() => import('./pages/HandoverQuestions')),
  },
  { path: '/tech-debts', label: '기술부채', component: lazy(() => import('./pages/TechDebts')) },
  { path: '/catchup-actions', label: '캐치업', sheet: '⑥' },
  { path: '/menu-inventories', label: '메뉴 인벤토리', sheet: '⑦' },
  { path: '/mega-processes', label: '메가프로세스', sheet: '⑧' },
  { path: '/interface-links', label: '연동 인터페이스', sheet: '⑨' },
  { path: '/analysis-cards', label: '분석 카드', sheet: '⑩' },
  { path: '/issue-queues', label: '이슈 큐', sheet: '⑪' },
  { path: '/raci-assignments', label: 'RACI', sheet: '⑫' },
  { path: '/review-logs', label: '회고 로그', sheet: '⑬' },
  { path: '/users', label: '사용자 관리', component: lazy(() => import('./pages/Users')) },
];

export const READY_NAV = NAV.filter((n) => n.component);

export const HOME_PATH = '/dashboard';

export function findNav(pathname: string): NavItem | undefined {
  return READY_NAV.find((n) => n.path === pathname || n.aliases?.includes(pathname));
}
