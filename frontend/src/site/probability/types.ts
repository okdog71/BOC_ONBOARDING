// BOC Replica 대고객 API(/api/site/probability/*) 응답 타입.
// 원문 정의: BOC Replica docs/확률형아이템/확률정보-API-정의서.md 파트 A

export interface VersionItem {
  version: number;
  /** 'yyyy-MM-dd'. 수집일(KST)을 적용일로 쓴다(원천이 적용일을 주지 않음 - 가정) */
  effectiveDate: string;
  /** 적용일 셀렉트 표시 문구. 같은 날 두 번째 이후 버전은 '2026-10-21 (수정)' */
  label: string;
  /** 내보낸 버전 중 최신 */
  latest: boolean;
  dataVersion: string;
  generatedAt: string;
  tableCount: number;
  rowCount: number;
}

export interface VersionMeta {
  version: number;
  effectiveDate: string;
  label: string;
  latest: boolean;
  dataVersion: string;
  generatedAt: string;
  /** 게시물 설명(제목 아래 안내문). 없을 수 있다 */
  description: string | null;
  tableCount: number;
  rowCount: number;
}

/** LNB 한 칸 */
export interface TableIndex {
  /** 표의 버전 간 안정 식별자. 표 지정은 항상 이 값(표 id 는 버전마다 달라질 수 있다) */
  tableKey: string;
  id: string;
  /** 전체 경로를 포함한 제목 '상위 > 하위' */
  title: string;
  /** 제목 경로 구간. LNB 에는 마지막 구간을 보여준다 */
  path: string[];
  level: number;
  kind: 'BOX' | 'REPURPOSE' | 'TABLE' | string;
  parentKey: string | null;
  /** 상위 표에서 이 표가 선택될 확률(0~1) */
  parentRate: number | null;
  rowCount: number;
  description: string | null;
  declaredTotal: number | null;
  declaredTotalText: string | null;
}

export interface Column {
  key: string;
  label: string;
  type: 'text' | 'number' | 'percent';
  precision: number | null;
}

/** 값이 없는 셀은 키 자체가 없다. percent 열은 `${key}Text`(원문 표기)가 함께 올 수 있다 */
export type Row = Record<string, string | number | null | undefined>;

export interface SiteTable {
  tableKey: string;
  title: string;
  path: string[];
  level: number;
  kind: string;
  parentKey: string | null;
  parentRate: number | null;
  description: string | null;
  columns: Column[];
  rows: Row[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  declaredTotal: number | null;
  declaredTotalText: string | null;
}

export type TableResolved = 'REQUESTED' | 'FIRST' | 'FALLBACK_FIRST' | 'NONE';

export interface View {
  category: string;
  language: string;
  /** withVersions=false 면 null, 내보낸 이력이 없으면 [] */
  versions: VersionItem[] | null;
  version: VersionMeta | null;
  tables: TableIndex[];
  table: SiteTable | null;
  tableResolved: TableResolved;
}

export interface SearchHit {
  tableKey: string;
  tableTitle: string;
  path: string[];
  columns: Column[];
  row: Row;
}

export interface SearchResult {
  version: number | null;
  hits: { content: SearchHit[]; page: number; size: number; totalElements: number; totalPages: number };
}
