import { useEffect, useState } from 'react';
import { api } from './api';
import type { AppUser } from './types';

/** 사용자 목록(담당자 후보·등록자 이름 표시용). 화면 간에 한 번만 불러와 공유한다. */
let cache: AppUser[] | null = null;
let inflight: Promise<void> | null = null;
const listeners = new Set<() => void>();

function load(): Promise<void> {
  inflight ??= api
    .get<AppUser[]>('/api/users')
    .then((list) => {
      cache = list;
    })
    .catch(() => {
      /* 로그인 전 등: 빈 목록으로 둔다 */
    })
    .finally(() => {
      inflight = null;
      listeners.forEach((l) => l());
    });
  return inflight;
}

/** 사용자 관리에서 바뀐 뒤, 또는 로그아웃 시 캐시를 버린다 */
export function invalidateUsers(refetch = true) {
  cache = null;
  listeners.forEach((l) => l());
  if (refetch) void load();
}

export function useUsers(): AppUser[] {
  const [, force] = useState(0);
  useEffect(() => {
    const l = () => force((n) => n + 1);
    listeners.add(l);
    if (cache === null) void load();
    return () => {
      listeners.delete(l);
    };
  }, []);
  return cache ?? [];
}

/** 로그인 ID → 이름(없으면 ID 그대로, system 은 "시스템") */
export function displayName(users: AppUser[], loginId: string | null | undefined): string {
  if (!loginId) return '-';
  if (loginId === 'system') return '시스템';
  return users.find((u) => u.loginId === loginId)?.name ?? loginId;
}
