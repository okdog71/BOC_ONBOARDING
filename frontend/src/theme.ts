import { useCallback, useEffect, useState } from 'react';

/** 화면 모드는 라이트/다크 두 가지. 선택하기 전에는 OS 설정을 따르고, 한 번 누르면 그 선택을 저장한다. */
export type Theme = 'light' | 'dark';
const KEY = 'boc.theme';
const DARK_QUERY = '(prefers-color-scheme: dark)';

function stored(): Theme | null {
  try {
    const v = localStorage.getItem(KEY);
    if (v === 'light' || v === 'dark') return v;
  } catch {
    /* 저장소 접근 불가 시 OS 설정 */
  }
  return null;
}

function effective(): Theme {
  return stored() ?? (window.matchMedia(DARK_QUERY).matches ? 'dark' : 'light');
}

export function useTheme() {
  const [theme, setTheme] = useState<Theme>(effective);

  // 선택 전에는 OS 설정이 바뀌면 따라간다
  useEffect(() => {
    const mq = window.matchMedia(DARK_QUERY);
    const on = () => {
      if (stored() === null) setTheme(mq.matches ? 'dark' : 'light');
    };
    mq.addEventListener('change', on);
    return () => mq.removeEventListener('change', on);
  }, []);

  const toggle = useCallback(() => {
    const next: Theme = theme === 'dark' ? 'light' : 'dark';
    document.documentElement.dataset.theme = next;
    try {
      localStorage.setItem(KEY, next);
    } catch {
      /* 무시 */
    }
    setTheme(next);
  }, [theme]);

  return { theme, toggle, nextLabel: theme === 'dark' ? '라이트' : '다크' };
}
