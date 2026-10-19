import { useEffect, useRef, type CSSProperties, type ReactNode, type Ref } from 'react';

/**
 * 상세(조회·편집) 팝업 공통 부품. 마일스톤·수급·접근·인수인계·확인 팝업이 같은 구조와 여백을 쓴다.
 *  DialogFrame(머리 · 본문 · 하단 바) → Banner / Facts / Section(KV 격자 · 폼 격자)
 */

export function DialogFrame({
  label,
  eyebrow,
  code,
  title,
  meta,
  hue,
  className = '',
  backdropClass = '',
  closeRef,
  locked = false,
  onClose,
  closeLabel = '닫기',
  closeText = '✕',
  footer,
  children,
}: {
  label: string;
  /** 제목 위 작은 분류(예: 수급 산출물) */
  eyebrow?: string;
  code?: string;
  title: string;
  /** 제목 아래 배지·그룹 정보 */
  meta?: ReactNode;
  /** 그룹 색상(상단 띠) */
  hue?: number;
  className?: string;
  backdropClass?: string;
  /** 닫기 버튼 ref(열릴 때 포커스용) */
  closeRef?: Ref<HTMLButtonElement>;
  /** 편집 중에는 바깥 클릭으로 닫히지 않는다 */
  locked?: boolean;
  onClose: () => void;
  closeLabel?: string;
  closeText?: string;
  footer?: ReactNode;
  children: ReactNode;
}) {
  return (
    <div
      className={`modal-backdrop ${backdropClass}`.trim()}
      onMouseDown={(e) => {
        if (e.target === e.currentTarget && !locked) onClose();
      }}
    >
      <div
        className={`modal dlg ${className}`.trim()}
        role="dialog"
        aria-modal="true"
        aria-label={label}
        style={hue === undefined ? undefined : ({ '--hue': hue } as CSSProperties)}
      >
        <header className="dlg-head">
          <div className="dlg-titles">
            <div className="dlg-eyebrow">
              {eyebrow}
              {code && <span className="code-tag">{code}</span>}
            </div>
            <h2>{title}</h2>
            {meta && <div className="dlg-meta">{meta}</div>}
          </div>
          <button ref={closeRef} type="button" className="dlg-close" onClick={onClose} aria-label={closeLabel}>
            {closeText}
          </button>
        </header>
        <div className="dlg-body">{children}</div>
        {footer && <footer className="dlg-foot">{footer}</footer>}
      </div>
    </div>
  );
}

/** 하단 바: 왼쪽에는 상태 등 빠른 조작, 오른쪽에는 주요 버튼 */
export function FootBar({ left, right }: { left?: ReactNode; right?: ReactNode }) {
  return (
    <>
      <div className="foot-left">{left}</div>
      <div className="foot-right">{right}</div>
    </>
  );
}

export function Banner({ tone = 'warn', title, children }: { tone?: 'warn' | 'danger' | 'info'; title?: string; children?: ReactNode }) {
  return (
    <div className={`banner banner-${tone}`} role={tone === 'info' ? 'note' : 'alert'}>
      {title && <b>{title}</b>}
      {children}
    </div>
  );
}

export type Tone = 'ok' | 'warn' | 'danger' | 'muted';

/** 핵심 수치 몇 개를 한눈에(상태·담당·일정 등) */
export function Facts({ items }: { items: { label: string; value: ReactNode; tone?: Tone }[] }) {
  return (
    <div className="facts">
      {items.map((i) => (
        <div key={i.label} className={`fact${i.tone ? ` tone-${i.tone}` : ''}`}>
          <span className="fact-label">{i.label}</span>
          <span className="fact-value">{i.value}</span>
        </div>
      ))}
    </div>
  );
}

export function Section({ title, aside, children }: { title: string; aside?: ReactNode; children: ReactNode }) {
  return (
    <section className="sec">
      <div className="sec-head">
        <h3>{title}</h3>
        {aside && <span className="sec-aside">{aside}</span>}
      </div>
      {children}
    </section>
  );
}

const isEmpty = (v: ReactNode) => v === null || v === undefined || v === '' || v === false;

/** 읽기 격자: 라벨(작게) 위, 값 아래. 값이 없으면 흐린 "—" */
export function KVGrid({ children }: { children: ReactNode }) {
  return <dl className="kv-grid">{children}</dl>;
}

export function KV({ label, wide, children }: { label: string; wide?: boolean; children: ReactNode }) {
  return (
    <div className={`kv${wide ? ' wide' : ''}`}>
      <dt>{label}</dt>
      <dd>{isEmpty(children) ? <span className="muted">—</span> : children}</dd>
    </div>
  );
}

/** 편집 격자 */
export function FormGrid({ children }: { children: ReactNode }) {
  return <div className="form-grid">{children}</div>;
}

export function FormField({
  label,
  required,
  hint,
  wide,
  as: Tag = 'label',
  children,
}: {
  label: string;
  required?: boolean;
  hint?: string;
  wide?: boolean;
  as?: 'label' | 'div';
  children: ReactNode;
}) {
  return (
    <Tag className={`ff${wide ? ' wide' : ''}`}>
      <span className="ff-label">
        {label}
        {required && <i className="req" aria-hidden="true"> *</i>}
      </span>
      {children}
      {hint && <small className="ff-hint">{hint}</small>}
    </Tag>
  );
}

/** 열릴 때 포커스를 팝업 안으로 옮기고, 닫히면 원래 자리로 돌려준다 */
export function useRestoreFocus<T extends HTMLElement>() {
  const ref = useRef<T>(null);
  useEffect(() => {
    const before = document.activeElement as HTMLElement | null;
    ref.current?.focus();
    return () => before?.focus?.();
  }, []);
  return ref;
}
