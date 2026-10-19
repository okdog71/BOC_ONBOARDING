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
  /** 산출물이 가리키는 메뉴(②③⑤)의 실제 진행 현황 */
  links: LinkedProgress[];
}

export interface LinkedProgress {
  sheet: string;
  label: string;
  route: string;
  metric: string;
  done: number;
  total: number;
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

export interface SupplyDoc {
  id: number;
  code: string;
  no: number;
  category: string;
  name: string;
  expectedContent: string | null;
  usagePurpose: string | null;
  existence: string | null;
  location: string | null;
  freshness: string | null;
  owner: string | null;
  supplyStatus: string;
  targetDate: string | null;
  requestedDate: string | null;
  receivedDate: string | null;
  ready: boolean;
  overdue: boolean;
  warnings: string[];
  requiredBySessions: string[];
  note: string | null;
}

export interface AccessItem {
  id: number;
  code: string;
  no: number;
  category: string;
  target: string;
  usage: string | null;
  envScope: string | null;
  procedure: string | null;
  status: string;
  requestedDate: string | null;
  approvedDate: string | null;
  targetDate: string | null;
  leadTimeDays: number | null;
  ready: boolean;
  overdue: boolean;
  prerequisite: boolean;
  warnings: string[];
  requiredBySessions: string[];
  owner: string | null;
  note: string | null;
}

export interface HandoverSession {
  id: number;
  code: string;
  no: number;
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
  completedDate: string | null;
  status: string;
  overdue: boolean;
  milestoneCode: string | null;
  milestoneName: string | null;
  supplyDocs: { code: string; name: string; status: string; ready: boolean }[];
  accessItems: { code: string; target: string; status: string; ready: boolean }[];
  blockers: string[];
  warnings: string[];
  note: string | null;
}

export interface LinkReport {
  checks: { name: string; checked: number; problems: number }[];
  issues: { severity: string; source: string; message: string }[];
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
