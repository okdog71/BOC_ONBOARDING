import type { ReactNode } from 'react';

/** 서버 응답 공통 필드(코드·No.·지연·경고) */
export interface BaseRow {
  id: number;
  code: string;
  no: number;
  overdue: boolean;
  warnings: string[];
}

export type Draft = Record<string, unknown>;

/** 페이지가 화면 렌더에 쓰는 외부 데이터·동작 */
export interface Ctx<T extends BaseRow> {
  rows: T[];
  /** 선택/새로 만들기 등에서 다른 항목을 열 때 */
  open: (code: string) => void;
  /** 페이지가 따로 불러온 참조 데이터(드롭다운 후보 등) */
  extra: Record<string, unknown>;
  reload: () => void;
}

export interface ColumnDef<T extends BaseRow> {
  header: string;
  className?: string;
  render: (row: T, ctx: Ctx<T>) => ReactNode;
}

/** section: 상세·편집 팝업에서 묶을 구역 이름(없으면 '기본'). 구역 순서는 처음 등장한 순서 */
type FieldBase = { key: string; label: string; required?: boolean; hint?: string; wide?: boolean; section?: string };

export type FieldDef<T extends BaseRow> =
  | (FieldBase & { kind: 'text'; placeholder?: string })
  | (FieldBase & { kind: 'textarea'; placeholder?: string })
  | (FieldBase & { kind: 'date' })
  /** 고정 목록 중 선택(빈 값 허용) */
  | (FieldBase & { kind: 'select'; options: string[]; allowEmpty?: boolean })
  /** 기존 값 목록에서 고르거나 직접 입력 */
  | (FieldBase & { kind: 'combo'; options: (rows: T[]) => string[]; placeholder?: string })
  /** 직접 그리는 입력(다대다 선택 등) */
  | (FieldBase & { kind: 'custom'; render: (draft: Draft, set: (k: string, v: unknown) => void, ctx: Ctx<T>) => ReactNode });

export interface ResourceConfig<T extends BaseRow> {
  /** API 경로, 예: /api/supply-docs */
  base: string;
  /** 화면 경로, 예: /supply-docs (딥링크 ?code=) */
  route: string;
  title: string;
  /** 엑셀 시트 기호(②③⑤) — 연관 마일스톤 조회에 사용 */
  sheet: string;
  itemLabel: string;
  /** 팝업 제목으로 쓸 필드(이름/대상/주제) */
  titleField: string;
  groupField: keyof T & string;
  groupLabel: string;
  /** 비어 있어도 항상 보여 줄 그룹(예: W1~W4) */
  fixedGroups?: string[];
  canRenameGroup: boolean;
  statusField: keyof T & string;
  statuses: string[];
  /** 상태 변경 전에 확인이 필요하면 메시지를 돌려준다 */
  confirmStatus?: (row: T, next: string) => string | null;
  isDone: (row: T) => boolean;
  /** 상태 칩의 "완료율" 기준 이름 */
  doneLabel: string;
  columns: ColumnDef<T>[];
  fields: FieldDef<T>[];
  /** 상세 맨 위에 띄울 경고 배너(예: 시작 전 미충족 항목) */
  alerts?: (row: T) => ReactNode;
  /** 상세 머리 아래에 한눈에 보여 줄 핵심 수치 */
  facts?: (row: T) => { label: string; value: ReactNode; tone?: 'ok' | 'warn' | 'danger' | 'muted' }[];
  /** 상세(읽기)에서 필드 아래에 추가로 보여 줄 연결·경고 영역 */
  detail?: (row: T, ctx: Ctx<T>) => ReactNode;
  searchOf: (row: T) => (string | null | undefined)[];
  /** 담당 등 값 목록으로 거르는 필터 */
  filters: { label: string; valueOf: (row: T) => string | null | undefined }[];
  emptyDraft: (group?: string) => Draft;
  toDraft: (row: T) => Draft;
  /** 안내 문구(민감정보 입력 금지 등) */
  notice?: string;
  /** 목록 상단 영역(연관 마일스톤 등) */
  header?: ReactNode;
  /** 페이지가 추가로 필요로 하는 참조 데이터 */
  loadExtra?: () => Promise<Record<string, unknown>>;
}
