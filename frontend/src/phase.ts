// 단계(Phase) 색상/코드 접두 유틸. 서버의 PhaseKey 와 같은 규칙을 쓴다.
const HUES: Record<string, number> = { P0: 215, HO: 270, P1: 150, P2: 30, P3: 350 };

/** "P1. IA" → "P1", "HO. 인수인계" → "HO", 접두가 없으면 "X" (새 마일스톤 코드의 접두) */
export function phaseKey(phase: string): string {
  const m = /^([A-Za-z][A-Za-z0-9]*)\s*\./.exec(phase.trim());
  return m ? m[1].toUpperCase() : 'X';
}

/** 마일스톤 단계가 아닌 그룹(수급 구분 A./B., 인수인계 주차 W1.. 등)의 공통 색 — 인수인계 화면의 파랑 */
export const GROUP_HUE = 222;

/** 그룹 띠 색상: 알려진 단계키(P0·HO·P1..)는 고정색, 나머지는 모두 공통 파랑 */
export function phaseHue(phase: string): number {
  const prefixed = /^([A-Za-z][A-Za-z0-9]*)\s*\./.test(phase.trim());
  const key = prefixed ? phaseKey(phase) : phase;
  return key in HUES ? HUES[key] : GROUP_HUE;
}

/** 새 마일스톤에 부여될 코드 미리보기(서버가 최종 결정): 같은 접두 코드의 최대 순번 + 1 */
export function nextCodePreview(phase: string, existingCodes: string[]): string {
  const key = phaseKey(phase);
  const max = existingCodes
    .filter((c) => c.startsWith(key + '.'))
    .map((c) => Number(c.slice(key.length + 1)))
    .filter((n) => Number.isInteger(n))
    .reduce((a, b) => Math.max(a, b), 0);
  return `${key}.${max + 1}`;
}
