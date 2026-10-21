import type { SearchResult, SiteTable, View } from './types';

// 대고객 확률 정보 API 클라이언트. 로그인 없는 공개 API 라서 ../../api.ts 의 request()를 쓰지 않는다
// (그쪽은 {success,data,error} 응답과 401 → 로그아웃 이벤트를 가정한다).
// 개발 중에는 Vite 프록시(/api/site/probability → BOC Gateway :8090)를 거친다.
const BASE = '/api/site/probability';

export class ProbabilityApiError extends Error {
  constructor(
    public status: number,
    public code: string,
    message: string,
  ) {
    super(message);
  }
}

type Param = string | number | boolean | undefined | null;

async function get<T>(path: string, params: Record<string, Param>, signal?: AbortSignal): Promise<T> {
  // tableKey 처럼 한글·공백·'>' 가 들어간 값도 URLSearchParams 가 인코딩해 준다
  const qs = new URLSearchParams();
  for (const [k, v] of Object.entries(params)) {
    if (v !== undefined && v !== null && v !== '') qs.set(k, String(v));
  }
  const res = await fetch(`${BASE}${path}?${qs.toString()}`, { signal, headers: { Accept: 'application/json' } });
  let body: { data?: T; code?: string; message?: string } | null = null;
  try {
    body = await res.json();
  } catch {
    /* 본문이 JSON 이 아니면 아래에서 일반 오류로 처리 */
  }
  if (!res.ok || !body || body.data === undefined) {
    throw new ProbabilityApiError(res.status, body?.code ?? 'HTTP', body?.message ?? `요청에 실패했습니다 (${res.status})`);
  }
  return body.data;
}

export interface ViewParams {
  category: string;
  lang: string;
  version?: number;
  tableKey?: string;
  withVersions?: boolean;
  page?: number;
  size?: number;
}

export interface TableParams {
  category: string;
  lang: string;
  version: number;
  tableKey: string;
  keyword?: string;
  page?: number;
  size?: number;
}

export const probabilityApi = {
  /** 최초 접속·버전 변경·딥링크: 이력 + 버전 정보 + 표 목록 + 본문 표(첫 페이지)를 한 번에 */
  view(p: ViewParams, signal?: AbortSignal) {
    return get<View>(
      '/view',
      {
        category: p.category,
        lang: p.lang,
        version: p.version,
        tableKey: p.tableKey,
        withVersions: p.withVersions === false ? false : undefined,
        page: p.page,
        size: p.size,
      },
      signal,
    );
  },

  /** 표 선택·표 안 검색·페이징: 본문 표만 */
  table(p: TableParams, signal?: AbortSignal) {
    return get<SiteTable>(
      `/versions/${p.version}/table`,
      { category: p.category, lang: p.lang, tableKey: p.tableKey, keyword: p.keyword, page: p.page, size: p.size },
      signal,
    );
  },

  /** 전체 검색(내보낸 최신 버전 대상, 2글자 이상) */
  search(p: { category: string; lang: string; keyword: string; page?: number; size?: number }, signal?: AbortSignal) {
    return get<SearchResult>('/search', { ...p }, signal);
  },
};
