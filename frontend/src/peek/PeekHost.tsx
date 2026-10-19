import { useCallback, useMemo, useState, type ReactNode } from 'react';
import PeekDialog from './PeekDialog';
import { PeekContext, type PeekApi, type PeekKind } from './context';

interface Entry {
  kind: PeekKind;
  code: string;
}

/** 확인 팝업 스택을 관리한다. 팝업 안의 칩을 누르면 한 겹 더 쌓이고, 닫으면 한 단계씩 돌아간다. */
export default function PeekHost({ children }: { children: ReactNode }) {
  const [stack, setStack] = useState<Entry[]>([]);
  const [version, setVersion] = useState(0);

  const open = useCallback((kind: PeekKind, code: string) => setStack((s) => [...s, { kind, code }]), []);
  const notifyChanged = useCallback(() => setVersion((v) => v + 1), []);
  const api = useMemo<PeekApi>(() => ({ open, version, notifyChanged }), [open, version, notifyChanged]);
  const top = stack[stack.length - 1];

  return (
    <PeekContext.Provider value={api}>
      {children}
      {top && (
        <PeekDialog
          key={`${stack.length}:${top.kind}:${top.code}`}
          kind={top.kind}
          code={top.code}
          depth={stack.length}
          onBack={() => setStack((s) => s.slice(0, -1))}
          onCloseAll={() => setStack([])}
        />
      )}
    </PeekContext.Provider>
  );
}
