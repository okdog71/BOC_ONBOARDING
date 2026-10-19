import { Children, Fragment, cloneElement, isValidElement, useEffect, useRef, type CSSProperties, type ReactElement, type ReactNode, type Ref } from 'react';

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
  mode = 'view',
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
  /** edit 이면 머리·본문 색과 입력 칸이 편집 상태로 바뀐다 */
  mode?: 'view' | 'edit';
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
        className={`modal dlg${mode === 'edit' ? ' is-edit' : ''} ${className}`.trim()}
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

export function Banner({ tone = 'warn', title, children }: { tone?: 'warn' | 'danger' | 'info' | 'edit'; title?: string; children?: ReactNode }) {
  return (
    <div className={`banner banner-${tone}`} role={tone === 'info' || tone === 'edit' ? 'note' : 'alert'}>
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

/** Fragment 를 펼쳐 항목(요소) 목록으로 만든다 */
function flatten(children: ReactNode): ReactElement[] {
  const out: ReactElement[] = [];
  Children.forEach(children, (c) => {
    if (!isValidElement(c)) return;
    if (c.type === Fragment) out.push(...flatten((c.props as { children?: ReactNode }).children));
    else out.push(c);
  });
  return out;
}

/** 값이 차지할 글자 수(칩은 label 길이로 센다). 길이를 보고 한 줄 전체를 쓸지 정한다 */
function textLength(node: ReactNode): number {
  if (node === null || node === undefined || typeof node === 'boolean') return 0;
  if (typeof node === 'string' || typeof node === 'number') return String(node).length;
  if (Array.isArray(node)) return node.reduce((n: number, c) => n + textLength(c as ReactNode), 0);
  if (isValidElement(node)) {
    const props = node.props as { children?: ReactNode; label?: unknown };
    return textLength(props.children) + (typeof props.label === 'string' ? props.label.length + 2 : 0);
  }
  return 0;
}

/**
 * 짧은 항목은 두 개씩 한 줄(2열)에, 긴 항목은 한 줄 전체에 배치한다.
 * 짝이 없는 짧은 항목(바로 뒤가 긴 항목이거나 마지막)은 한 줄 전체를 쓴다.
 */
function pair(items: ReactElement[], isFull: (el: ReactElement) => boolean): ReactElement[] {
  const full = items.map(isFull);
  let pending = -1;
  items.forEach((_, i) => {
    if (full[i]) {
      if (pending >= 0) full[pending] = true;
      pending = -1;
    } else if (pending >= 0) {
      pending = -1;
    } else {
      pending = i;
    }
  });
  if (pending >= 0) full[pending] = true;
  return items.map((el, i) => cloneElement(el as ReactElement<{ full?: boolean }>, { full: full[i], key: el.key ?? i }));
}

/** 읽기 격자(2열): 라벨 칸 | 값 칸 ‖ 라벨 칸 | 값 칸. 값이 길면 한 줄 전체 */
const KV_WIDE_AT = 26;

export function KVGrid({ children }: { children: ReactNode }) {
  const items = pair(flatten(children), (el) => {
    const p = el.props as { wide?: boolean; children?: ReactNode };
    return Boolean(p.wide) || textLength(p.children) > KV_WIDE_AT;
  });
  return <dl className="kv-grid">{items}</dl>;
}

export function KV({ label, full, children }: { label: string; wide?: boolean; full?: boolean; children: ReactNode }) {
  return (
    <div className={`kv ${full ? 'full' : 'half'}`}>
      <dt>{label}</dt>
      <dd>{isEmpty(children) ? <span className="muted">—</span> : children}</dd>
    </div>
  );
}

/** 편집 격자(2열): 짧은 입력은 두 개씩, 긴 입력(여러 줄·선택 목록)은 한 줄 전체 */
export function FormGrid({ children }: { children: ReactNode }) {
  const items = pair(flatten(children), (el) => Boolean((el.props as { wide?: boolean }).wide));
  return <div className="form-grid">{items}</div>;
}

export function FormField({
  label,
  required,
  hint,
  wide: _wide,
  full,
  as: Tag = 'label',
  children,
}: {
  label: string;
  required?: boolean;
  hint?: string;
  wide?: boolean;
  full?: boolean;
  as?: 'label' | 'div';
  children: ReactNode;
}) {
  return (
    <Tag className={`ff ${full ? 'full' : 'half'}`}>
      <span className="ff-label">
        {label}
        {required && <i className="req" aria-hidden="true"> *</i>}
      </span>
      <span className="ff-control">
        {children}
        {hint && <small className="ff-hint">{hint}</small>}
      </span>
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
