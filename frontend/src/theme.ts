import { useCallback, useState } from 'react';

export type Theme = 'system' | 'light' | 'dark';
const KEY = 'boc.theme';
const ORDER: Theme[] = ['system', 'light', 'dark'];
const LABEL: Record<Theme, string> = { system: '시스템', light: '라이트', dark: '다크' };
const ICON: Record<Theme, string> = { system: '🖥', light: '☀', dark: '🌙' };

function read(): Theme {
  try {
    const v = localStorage.getItem(KEY);
    if (v === 'light' || v === 'dark') return v;
  } catch {
    /* 저장소 접근 불가 시 시스템 */
  }
  return 'system';
}

function apply(t: Theme) {
  if (t === 'system') delete document.documentElement.dataset.theme;
  else document.documentElement.dataset.theme = t;
  try {
    if (t === 'system') localStorage.removeItem(KEY);
    else localStorage.setItem(KEY, t);
  } catch {
    /* 무시 */
  }
}

/** 시스템/라이트/다크 순환 토글. 선택은 localStorage 에 저장되고 index.html 의 인라인 스크립트가 첫 렌더 전에 복원한다. */
export function useTheme() {
  const [theme, setTheme] = useState<Theme>(read);
  const cycle = useCallback(() => {
    const next = ORDER[(ORDER.indexOf(theme) + 1) % ORDER.length];
    apply(next);
    setTheme(next);
  }, [theme]);
  const next = ORDER[(ORDER.indexOf(theme) + 1) % ORDER.length];
  return { theme, cycle, label: LABEL[theme], nextLabel: LABEL[next], icon: ICON[theme] };
}
