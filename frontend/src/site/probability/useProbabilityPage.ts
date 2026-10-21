import { useCallback, useEffect, useRef, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { probabilityApi, ProbabilityApiError } from './api';
import { descendantsOf } from './tree';
import type { SearchHit, SearchResult, SiteTable, TableIndex, VersionItem, VersionMeta } from './types';

// 확률 정보 센터 화면의 상태와 API 호출 흐름(사용자 여정 J1~J9).
//  - 최초 접속·버전 변경·링크 진입: GET /view (이력 + 버전 + 표 목록 + 선택한 표)
//  - 선택한 표의 모든 하위 표: GET /versions/{v}/table 을 하위 표마다 병렬 호출해 구역(section)으로 쌓는다
//  - 구역별 페이징·표 안 검색·전체 보기: 그 구역의 GET /versions/{v}/table 만 다시 호출
//  - 전체 검색: GET /search (내보낸 최신 버전 대상)
// 화면 상태는 주소(?category&version&table&page&q)에도 남겨 새로고침·링크 공유로 복원한다(replace 로만 갱신).
// 주소의 page·q 는 선택한 표(첫 구역)의 것이다. 하위 표 구역의 페이지는 주소에 남기지 않는다.

export const CATEGORIES = [
  { code: 'CONSOLE', label: '콘솔' },
  { code: 'PC', label: 'PC' },
];
const DEFAULT_CATEGORY = 'CONSOLE';
const DEFAULT_LANG = 'ko';
// 표 한 번에 20행씩(샘플 화면과 같음), "전체 보기"는 서버가 허용하는 최대 200행씩
export const DEFAULT_SIZE = 20;
export const FULL_SIZE = 200;
export const SEARCH_MIN = 2;
const FALLBACK_NOTICE = '선택하신 표가 이 적용일에는 없어 첫 번째 표를 보여드립니다.';

export interface PageData {
  versions: VersionItem[];
  version: VersionMeta;
  tables: TableIndex[];
  /** 지금 선택한 표(첫 구역). 아래 하위 표는 이 표의 자손이다 */
  rootKey: string;
}

/** 화면에 쌓이는 표 한 개. 선택한 표가 첫 구역이고 그 아래로 하위 표가 이어진다 */
export interface Section {
  tableKey: string;
  /** 아직 불러오는 중이면 null */
  table: SiteTable | null;
  /** 이 구역에 적용된 표 안 검색어 */
  keyword: string;
  size: number;
  loading: boolean;
  error: string;
}

export type PageStatus = 'loading' | 'ready' | 'empty' | 'error';

export interface SearchState {
  keyword: string;
  result: SearchResult | null;
  loading: boolean;
  error: string;
}

const NO_SEARCH: SearchState = { keyword: '', result: null, loading: false, error: '' };

function isAbort(e: unknown): boolean {
  return e instanceof DOMException && e.name === 'AbortError';
}

function message(e: unknown): string {
  return e instanceof ProbabilityApiError ? e.message : '확률 정보를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.';
}

function skeleton(tableKey: string): Section {
  return { tableKey, table: null, keyword: '', size: DEFAULT_SIZE, loading: true, error: '' };
}

interface ViewRequest {
  category: string;
  version?: number;
  tableKey?: string;
  page?: number;
  /** 표 안 검색어. view 는 검색을 지원하지 않아 table 을 한 번 더 호출한다(새로고침 복원용) */
  keyword?: string;
  withVersions?: boolean;
}

interface FetchOpts {
  page: number;
  keyword: string;
  size: number;
}

export function useProbabilityPage() {
  const [params, setParams] = useSearchParams();
  const category = (params.get('category') ?? DEFAULT_CATEGORY).toUpperCase();
  const lang = params.get('lang') ?? DEFAULT_LANG;

  const [status, setStatus] = useState<PageStatus>('loading');
  const [error, setError] = useState('');
  const [data, setData] = useState<PageData | null>(null);
  const [sections, setSections] = useState<Section[]>([]);
  const [notice, setNotice] = useState('');
  const [search, setSearch] = useState<SearchState>(NO_SEARCH);

  const dataRef = useRef<PageData | null>(null);
  dataRef.current = data;
  const sectionsRef = useRef<Section[]>([]);
  sectionsRef.current = sections;
  const categoryRef = useRef(category);
  categoryRef.current = category;
  const nav = useRef({ gen: 0, ctrl: new AbortController() });
  const sectionCtrls = useRef(new Map<string, AbortController>());
  const searchCtrl = useRef<AbortController | null>(null);

  /** 새 화면 이동을 시작한다. 진행 중이던 모든 요청을 취소해 늦게 온 응답이 화면을 덮어쓰지 못하게 한다 */
  const beginNav = () => {
    nav.current.ctrl.abort();
    sectionCtrls.current.forEach((c) => c.abort());
    sectionCtrls.current.clear();
    nav.current = { gen: nav.current.gen + 1, ctrl: new AbortController() };
    return nav.current;
  };

  const patchSection = useCallback((tableKey: string, patch: Partial<Section>) => {
    setSections((prev) => prev.map((s) => (s.tableKey === tableKey ? { ...s, ...patch } : s)));
  }, []);

  const writeUrl = useCallback(
    (cat: string, d: PageData, page: number, kw: string) => {
      const next = new URLSearchParams();
      next.set('category', cat);
      if (lang !== DEFAULT_LANG) next.set('lang', lang);
      if (!d.version.latest) next.set('version', String(d.version.version));
      if (d.tables.length > 0 && d.rootKey !== d.tables[0].tableKey) next.set('table', d.rootKey);
      if (page > 0) next.set('page', String(page));
      if (kw) next.set('q', kw);
      setParams(next, { replace: true });
    },
    [lang, setParams],
  );

  /** 표 한 개(구역)를 불러온다. 같은 구역의 이전 요청은 취소한다. 새 화면 이동이 시작되면 결과를 버린다 */
  const fetchSection = useCallback(
    async (gen: number, version: number, tableKey: string, o: FetchOpts): Promise<{ table: SiteTable | null; status: number }> => {
      sectionCtrls.current.get(tableKey)?.abort();
      const ctrl = new AbortController();
      sectionCtrls.current.set(tableKey, ctrl);
      patchSection(tableKey, { loading: true, error: '' });
      try {
        const table = await probabilityApi.table(
          { category: categoryRef.current, lang, version, tableKey, keyword: o.keyword, page: o.page, size: o.size },
          ctrl.signal,
        );
        if (gen !== nav.current.gen) return { table: null, status: 0 };
        patchSection(tableKey, { table, keyword: o.keyword, size: o.size, loading: false });
        return { table, status: 200 };
      } catch (e) {
        if (isAbort(e) || gen !== nav.current.gen) return { table: null, status: 0 };
        patchSection(tableKey, { loading: false, error: message(e) });
        return { table: null, status: e instanceof ProbabilityApiError ? e.status : 500 };
      }
    },
    [lang, patchSection],
  );

  /** J1·J5·J6·J7: 화면 전체(이력·LNB·선택한 표)를 다시 만들고, 선택한 표의 하위 표를 이어서 불러온다 */
  const loadView = useCallback(
    async (req: ViewRequest, recovered = false): Promise<void> => {
      const { gen, ctrl } = beginNav();
      if (!dataRef.current) setStatus('loading');
      try {
        const view = await probabilityApi.view(
          {
            category: req.category,
            lang,
            version: req.version,
            tableKey: req.tableKey,
            page: req.keyword ? 0 : req.page,
            size: DEFAULT_SIZE,
            withVersions: req.withVersions,
          },
          ctrl.signal,
        );
        if (gen !== nav.current.gen) return;
        if (view.tableResolved === 'NONE' || !view.version || !view.table) {
          setData(null);
          setSections([]);
          setStatus('empty');
          setNotice('');
          setParams(new URLSearchParams({ category: req.category }), { replace: true });
          return;
        }
        const rootKey = view.table.tableKey;
        const version = view.version.version;
        const next: PageData = {
          versions: view.versions ?? dataRef.current?.versions ?? [],
          version: view.version,
          tables: view.tables,
          rootKey,
        };
        const children = descendantsOf(view.tables, rootKey);
        setData(next);
        setSections([
          { tableKey: rootKey, table: view.table, keyword: '', size: DEFAULT_SIZE, loading: false, error: '' },
          ...children.map((c) => skeleton(c.tableKey)),
        ]);
        setStatus('ready');
        setNotice(view.tableResolved === 'FALLBACK_FIRST' ? FALLBACK_NOTICE : '');
        writeUrl(req.category, next, view.table.page, '');

        const jobs: Promise<unknown>[] = children.map((c) => fetchSection(gen, version, c.tableKey, { page: 0, keyword: '', size: DEFAULT_SIZE }));
        if (req.keyword) {
          // 새로고침 복원: 주소의 표 안 검색어를 선택한 표에 다시 적용한다
          jobs.push(
            fetchSection(gen, version, rootKey, { page: req.page ?? 0, keyword: req.keyword, size: DEFAULT_SIZE }).then((r) => {
              if (r.table) writeUrl(req.category, next, r.table.page, req.keyword ?? '');
            }),
          );
        }
        await Promise.all(jobs);
      } catch (e) {
        if (isAbort(e) || gen !== nav.current.gen) return;
        // 없는 버전(링크가 오래됨·내보내기 해제됨)은 버전/표를 빼고 최신으로 한 번 복구한다(J9)
        if (e instanceof ProbabilityApiError && e.status === 404 && !recovered && (req.version !== undefined || req.tableKey)) {
          await loadView({ category: req.category, withVersions: true }, true);
          return;
        }
        setError(message(e));
        setStatus(dataRef.current ? 'ready' : 'error');
      }
    },
    [fetchSection, lang, setParams, writeUrl],
  );

  /** J2: LNB 에서 표를 고른다 → 그 표와 모든 하위 표를 구역으로 쌓는다(LNB·적용일은 그대로) */
  const selectTable = useCallback(
    async (tableKey: string): Promise<void> => {
      const d = dataRef.current;
      if (!d) return;
      const { gen } = beginNav();
      const children = descendantsOf(d.tables, tableKey);
      const next: PageData = { ...d, rootKey: tableKey };
      setData(next);
      setNotice('');
      setSections([skeleton(tableKey), ...children.map((c) => skeleton(c.tableKey))]);
      const version = d.version.version;
      const [root] = await Promise.all([
        fetchSection(gen, version, tableKey, { page: 0, keyword: '', size: DEFAULT_SIZE }),
        ...children.map((c) => fetchSection(gen, version, c.tableKey, { page: 0, keyword: '', size: DEFAULT_SIZE })),
      ]);
      if (gen !== nav.current.gen) return;
      if (root.table) writeUrl(categoryRef.current, next, 0, '');
      else if (root.status === 404) await loadView({ category: categoryRef.current, withVersions: true }, true);
    },
    [fetchSection, loadView, writeUrl],
  );

  // J1·J7: 처음 열릴 때 주소의 값(없으면 기본값)으로 한 번 조회한다
  useEffect(() => {
    const v = Number(params.get('version'));
    void loadView({
      category,
      version: Number.isInteger(v) && v > 0 ? v : undefined,
      tableKey: params.get('table') ?? undefined,
      page: Number(params.get('page')) || 0,
      keyword: params.get('q') ?? undefined,
      withVersions: true,
    });
    return () => {
      nav.current.ctrl.abort();
      sectionCtrls.current.forEach((c) => c.abort());
      searchCtrl.current?.abort();
    };
    // 최초 1회만. 이후 주소는 화면 상태를 따라 갱신될 뿐이다
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  /** J3·J4: 한 구역만 다시 불러온다(페이지 이동·표 안 검색·전체 보기). 선택한 표(첫 구역)면 주소도 갱신한다 */
  const reloadSection = useCallback(
    async (tableKey: string, o: FetchOpts): Promise<void> => {
      const d = dataRef.current;
      if (!d) return;
      const r = await fetchSection(nav.current.gen, d.version.version, tableKey, o);
      if (r.table && tableKey === d.rootKey) writeUrl(categoryRef.current, d, r.table.page, o.keyword);
      if (r.status === 404) await loadView({ category: categoryRef.current, withVersions: true }, true);
    },
    [fetchSection, loadView, writeUrl],
  );
  const sectionOf = (tableKey: string) => sectionsRef.current.find((s) => s.tableKey === tableKey);
  const changePage = (tableKey: string, page: number) => {
    const s = sectionOf(tableKey);
    if (s) void reloadSection(tableKey, { page, keyword: s.keyword, size: s.size });
  };
  const searchInTable = (tableKey: string, keyword: string) => {
    const s = sectionOf(tableKey);
    if (s) void reloadSection(tableKey, { page: 0, keyword: keyword.trim(), size: s.size });
  };
  /** 20행씩 ↔ 전체 보기(200행씩) */
  const changeSize = (tableKey: string, size: number) => {
    const s = sectionOf(tableKey);
    if (s) void reloadSection(tableKey, { page: 0, keyword: s.keyword, size });
  };

  /** J5: 같은 표를 유지한 채 다른 적용일 보기(없으면 서버가 첫 표로 대체) */
  const changeVersion = (version: number) =>
    void loadView({ category: categoryRef.current, version, tableKey: dataRef.current?.rootKey, withVersions: false });
  const changeCategory = (next: string) => {
    if (next === categoryRef.current) return;
    categoryRef.current = next;
    setData(null);
    setSections([]);
    setSearch(NO_SEARCH);
    void loadView({ category: next, withVersions: true });
  };

  /** J6: 전체 검색 */
  const runSearch = useCallback(
    async (kw: string, page = 0): Promise<void> => {
      const trimmed = kw.trim();
      searchCtrl.current?.abort();
      if (trimmed.length < SEARCH_MIN) {
        setSearch({ keyword: kw, result: null, loading: false, error: '' });
        return;
      }
      const ctrl = new AbortController();
      searchCtrl.current = ctrl;
      setSearch((s) => ({ ...s, keyword: kw, loading: true, error: '' }));
      try {
        const result = await probabilityApi.search({ category: categoryRef.current, lang, keyword: trimmed, page, size: 20 }, ctrl.signal);
        setSearch({ keyword: kw, result, loading: false, error: '' });
      } catch (e) {
        if (isAbort(e)) return;
        setSearch({ keyword: kw, result: null, loading: false, error: message(e) });
      }
    },
    [lang],
  );
  const clearSearch = () => {
    searchCtrl.current?.abort();
    setSearch(NO_SEARCH);
  };
  /** 검색 결과 클릭: 최신 버전의 해당 표로 이동(적용일도 최신으로 바뀐다) */
  const openHit = (hit: SearchHit) => {
    setSearch((s) => ({ ...s, result: null }));
    void loadView({ category: categoryRef.current, tableKey: hit.tableKey, withVersions: true });
  };

  return {
    category,
    status,
    error,
    data,
    sections,
    notice,
    search,
    retry: () => void loadView({ category: categoryRef.current, withVersions: true }),
    selectTable: (tableKey: string) => void selectTable(tableKey),
    changePage,
    searchInTable,
    changeSize,
    changeVersion,
    changeCategory,
    runSearch,
    clearSearch,
    openHit,
  };
}
