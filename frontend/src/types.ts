export interface Milestone {
  id: number;
  phase: string;
  code: string;
  /** 단계 내 순서(No.) — 코드와 별개, 드래그로 바뀜 */
  no: number;
  name: string;
  goal: string | null;
  checkpoint: string | null;
  output: string | null;
  precondition: string | null;
  owner: string | null;
  durationWeeks: string | null;
  status: string;
  doneCriteria: string | null;
  note: string | null;
  targetDate: string | null;
  completedDate: string | null;
  overdue: boolean;
  predecessorCodes: string[];
  blockedBy: string[];
  progress: { done: number; total: number; percent: number | null };
  checkItems: CheckItem[];
}

export interface CheckItem {
  id: number;
  title: string;
  done: boolean;
}

export interface PhaseInfo {
  phase: string;
  total: number;
  done: number;
}

/** 생성/수정(PUT) 요청 본문 */
export interface MilestoneInput {
  phase: string;
  code: string;
  name: string;
  goal: string;
  checkpoint: string;
  output: string;
  precondition: string;
  owner: string;
  durationWeeks: string;
  status: string;
  doneCriteria: string;
  note: string;
  targetDate: string;
}

export interface HandoverSession {
  id: number;
  week: string;
  category: string;
  domain: string | null;
  topic: string;
  ktStage: string | null;
  handoverFrom: string | null;
  handoverTo: string | null;
  method: string | null;
  output: string | null;
  precondition: string | null;
  plannedDate: string | null;
  status: string;
  note: string | null;
}

export interface Enums {
  milestoneStatus: string[];
  handoverStatus: string[];
  week: string[];
  category: string[];
  [key: string]: string[];
}

/** /api/summary 의 각 섹션: total + byStatus 등 카운트 맵 */
export type SummarySection = { total: number } & Record<string, number | Record<string, number>>;
export type Summary = Record<string, SummarySection>;
