import { useEffect, useState, type MouseEvent } from 'react';

/** http(사내 IP 접속 등)에서는 navigator.clipboard 가 없으므로 textarea 방식으로 대체 */
async function copy(text: string) {
  if (navigator.clipboard && window.isSecureContext) {
    await navigator.clipboard.writeText(text);
    return;
  }
  const ta = document.createElement('textarea');
  ta.value = text;
  ta.setAttribute('readonly', '');
  ta.style.position = 'fixed';
  ta.style.opacity = '0';
  document.body.appendChild(ta);
  ta.select();
  try {
    if (!document.execCommand('copy')) throw new Error('copy failed');
  } finally {
    document.body.removeChild(ta);
  }
}

/** 값 옆에 클립보드 복사 아이콘을 붙인다. 값이 없으면 대체 문자만 보여준다. */
export default function CopyText({ value, empty = '-' }: { value?: string | null; empty?: string }) {
  const [state, setState] = useState<'idle' | 'done' | 'fail'>('idle');

  useEffect(() => {
    if (state === 'idle') return;
    const t = window.setTimeout(() => setState('idle'), 1200);
    return () => window.clearTimeout(t);
  }, [state]);

  if (!value) return <>{empty}</>;

  function onClick(e: MouseEvent) {
    e.stopPropagation();
    copy(value!).then(
      () => setState('done'),
      () => setState('fail'),
    );
  }

  const label = state === 'done' ? '복사됨' : state === 'fail' ? '복사 실패' : '복사';
  return (
    <span className="copy-text">
      <span>{value}</span>
      <button type="button" className={`copy-btn ${state}`} onClick={onClick} title={label} aria-label={`${value} ${label}`}>
        {state === 'done' ? (
          <svg viewBox="0 0 16 16" width="13" height="13" aria-hidden="true">
            <path d="M3 8.5l3 3 7-7" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
          </svg>
        ) : (
          <svg viewBox="0 0 16 16" width="13" height="13" aria-hidden="true">
            <rect x="5.5" y="5.5" width="8" height="8" rx="1.5" fill="none" stroke="currentColor" strokeWidth="1.4" />
            <path d="M10.5 3.5v-.5A1.5 1.5 0 0 0 9 1.5H4A1.5 1.5 0 0 0 2.5 3v5A1.5 1.5 0 0 0 4 9.5h.5" fill="none" stroke="currentColor" strokeWidth="1.4" />
          </svg>
        )}
      </button>
    </span>
  );
}
