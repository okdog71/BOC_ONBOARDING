import { createContext, useContext } from 'react';

/** 확인 팝업으로 열 수 있는 항목 종류 */
export type PeekKind = 'milestone' | 'supply' | 'access' | 'handover';

export interface PeekApi {
  /** 현재 화면 위에 해당 항목의 확인 팝업을 쌓는다(페이지 이동 없음) */
  open: (kind: PeekKind, code: string) => void;
  /** 팝업에서 데이터를 바꿀 때마다 증가 — 뒤쪽 화면이 다시 불러오는 신호 */
  version: number;
  notifyChanged: () => void;
}

export const PeekContext = createContext<PeekApi>({ open: () => undefined, version: 0, notifyChanged: () => undefined });

export const usePeek = () => useContext(PeekContext);
