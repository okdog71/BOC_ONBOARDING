export interface FieldError {
  field: string;
  message: string;
}
export interface ApiErrorBody {
  code: string;
  message: string;
  fieldErrors?: FieldError[];
}
interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: ApiErrorBody;
}
export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export class ApiError extends Error {
  constructor(public body: ApiErrorBody) {
    super(
      body.fieldErrors?.length
        ? `${body.message} (${body.fieldErrors.map((f) => `${f.field}: ${f.message}`).join(', ')})`
        : body.message,
    );
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  // FormData(파일 업로드)는 브라우저가 boundary 를 포함한 Content-Type 을 직접 붙이도록 둔다
  const isForm = init?.body instanceof FormData;
  const res = await fetch(path, {
    ...init,
    headers: isForm ? init?.headers : { 'Content-Type': 'application/json', ...init?.headers },
  });
  if (res.status === 204) return undefined as T;
  let json: ApiResponse<T> | undefined;
  try {
    json = (await res.json()) as ApiResponse<T>;
  } catch {
    /* 본문 없음/비JSON */
  }
  if (!res.ok || !json?.success) {
    throw new ApiError(json?.error ?? { code: 'HTTP_' + res.status, message: `요청 실패 (${res.status})` });
  }
  return json.data as T;
}

export function query(params: Record<string, string | number | undefined>): string {
  const q = new URLSearchParams();
  Object.entries(params).forEach(([k, v]) => {
    if (v !== undefined && v !== '') q.set(k, String(v));
  });
  const s = q.toString();
  return s ? `?${s}` : '';
}

export const api = {
  get: <T>(path: string) => request<T>(path),
  send: <T>(method: 'POST' | 'PUT' | 'PATCH' | 'DELETE', path: string, body?: unknown) =>
    request<T>(path, { method, body: body === undefined ? undefined : JSON.stringify(body) }),
  /** multipart 업로드(file 파트 1개) */
  upload: <T>(path: string, file: File) => {
    const body = new FormData();
    body.append('file', file);
    return request<T>(path, { method: 'POST', body });
  },
  patchStatus: <T>(path: string, status: string) =>
    request<T>(`${path}/status`, { method: 'PATCH', body: JSON.stringify({ status }) }),
};
