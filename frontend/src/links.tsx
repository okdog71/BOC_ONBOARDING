import { Link } from 'react-router-dom';
import { NAV } from './nav';
import type { LinkedProgress } from './types';

/**
 * 엑셀 시트 기호(②~⑬) → 화면 라우트 매핑. nav.tsx 레지스트리에서 파생한다.
 * 화면이 아직 없는(ready=false) 시트는 "준비 중" 칩으로 표시된다.
 * (안정화되면 백엔드 /api/meta/links 로 이관 예정)
 */
export const SHEET_LINKS: Record<string, { label: string; to: string; ready: boolean }> = Object.fromEntries(
  NAV.filter((n) => n.sheet).map((n) => [n.sheet as string, { label: n.label, to: n.path, ready: Boolean(n.component) }]),
);

/** 문자열에서 시트 기호를 중복 없이 순서대로 추출 */
export function sheetSymbols(text: string | null | undefined): string[] {
  const found = (text ?? '').match(/[②-⑬]/g) ?? [];
  return [...new Set(found)];
}

export function LinkChip({ to, ready = true, label, hint }: { to: string; ready?: boolean; label: string; hint?: string }) {
  if (!ready) {
    return (
      <span className="chip chip-off" title={hint ?? '준비 중인 화면입니다'}>
        {label}
      </span>
    );
  }
  return (
    <Link className="chip" to={to} title={hint} onClick={(e) => e.stopPropagation()}>
      {label}
    </Link>
  );
}

/** 산출물 문자열에서 시트 기호를 찾아 이동 칩으로 렌더 */
export function SheetChips({
  text,
  skip = [],
  links = [],
}: {
  text: string | null | undefined;
  /** 칩으로 만들지 않을 기호(이미 항목 단위 링크로 표시한 것) */
  skip?: string[];
  /** 서버가 계산한 실제 진행 현황(있으면 칩에 "수령 3/27" 같은 수치를 덧붙인다) */
  links?: LinkedProgress[];
}) {
  return (
    <>
      {sheetSymbols(text)
        .filter((sym) => !skip.includes(sym))
        .map((sym) => {
          const l = SHEET_LINKS[sym];
          const p = links.find((x) => x.sheet === sym);
          return (
            <LinkChip
              key={sym}
              to={l.to}
              ready={l.ready}
              label={`${sym} ${l.label}${p ? ` ${p.done}/${p.total}` : ''}`}
              hint={p ? `${p.label} — ${p.metric} ${p.done}/${p.total}` : undefined}
            />
          );
        })}
    </>
  );
}
