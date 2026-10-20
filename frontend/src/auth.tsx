import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { api } from './api';
import type { AppUser } from './types';

interface AuthState {
  user: AppUser | null;
  /** 최초 로그인 상태 확인 중 */
  loading: boolean;
  login: (loginId: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  /** 내 정보를 다시 읽는다(비밀번호 변경·프로필 수정 뒤) */
  refresh: () => Promise<void>;
}

const Ctx = createContext<AuthState | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AppUser | null>(null);
  const [loading, setLoading] = useState(true);

  const refresh = useCallback(async () => {
    try {
      setUser(await api.get<AppUser>('/api/auth/me'));
    } catch {
      setUser(null);
    }
  }, []);

  useEffect(() => {
    refresh().finally(() => setLoading(false));
  }, [refresh]);

  // 어느 요청이든 401 이 오면(세션 만료·서버 재시작) 로그인 화면으로
  useEffect(() => {
    const on = () => setUser(null);
    window.addEventListener('boc:unauthorized', on);
    return () => window.removeEventListener('boc:unauthorized', on);
  }, []);

  const value = useMemo<AuthState>(
    () => ({
      user,
      loading,
      refresh,
      login: async (loginId, password) => {
        setUser(await api.send<AppUser>('POST', '/api/auth/login', { loginId, password }));
      },
      logout: async () => {
        try {
          await api.send('POST', '/api/auth/logout');
        } finally {
          setUser(null);
        }
      },
    }),
    [user, loading, refresh],
  );
  return <Ctx.Provider value={value}>{children}</Ctx.Provider>;
}

export function useAuth(): AuthState {
  const v = useContext(Ctx);
  if (!v) throw new Error('AuthProvider 안에서 사용하세요');
  return v;
}
