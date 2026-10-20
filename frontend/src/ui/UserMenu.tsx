import { useEffect, useRef, useState, type FormEvent } from 'react';
import { api } from '../api';
import { useAuth } from '../auth';
import { Banner, DialogFrame, FootBar, FormField, FormGrid } from './detail';

/** 상단바 오른쪽: 로그인한 사용자 이름 → 비밀번호 변경 · 로그아웃 */
export default function UserMenu() {
  const { user, logout } = useAuth();
  const [open, setOpen] = useState(false);
  const [pwOpen, setPwOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!open) return;
    const away = (e: MouseEvent) => !ref.current?.contains(e.target as Node) && setOpen(false);
    const esc = (e: KeyboardEvent) => e.key === 'Escape' && setOpen(false);
    document.addEventListener('mousedown', away);
    document.addEventListener('keydown', esc);
    return () => {
      document.removeEventListener('mousedown', away);
      document.removeEventListener('keydown', esc);
    };
  }, [open]);

  if (!user) return null;
  return (
    <div className="user-menu" ref={ref}>
      <button type="button" className="user-btn" aria-haspopup="menu" aria-expanded={open} onClick={() => setOpen(!open)}>
        <span className="avatar" aria-hidden="true">{user.name.slice(0, 1)}</span>
        <span className="user-name">{user.name}</span>
        {user.mustChangePassword && <span className="dot" title="초기 비밀번호를 사용 중입니다" />}
      </button>
      {open && (
        <div className="user-pop" role="menu">
          <div className="user-pop-head">
            <strong>{user.name}</strong>
            <span>
              {user.loginId} · {user.affiliation}
            </span>
          </div>
          <button type="button" role="menuitem" onClick={() => { setOpen(false); setPwOpen(true); }}>
            비밀번호 변경{user.mustChangePassword && <em> · 권장</em>}
          </button>
          <button type="button" role="menuitem" onClick={() => void logout()}>
            로그아웃
          </button>
        </div>
      )}
      {pwOpen && <PasswordDialog onClose={() => setPwOpen(false)} />}
    </div>
  );
}

export function PasswordDialog({ onClose }: { onClose: () => void }) {
  const { user, refresh } = useAuth();
  const [current, setCurrent] = useState('');
  const [next, setNext] = useState('');
  const [again, setAgain] = useState('');
  const [error, setError] = useState<string>();
  const [done, setDone] = useState(false);
  const [busy, setBusy] = useState(false);

  async function submit(e: FormEvent) {
    e.preventDefault();
    if (next !== again) return setError('새 비밀번호가 서로 다릅니다.');
    if (next === user?.loginId) return setError('새 비밀번호는 로그인 ID 와 달라야 합니다.');
    setBusy(true);
    setError(undefined);
    try {
      await api.send('PUT', '/api/auth/password', { currentPassword: current, newPassword: next });
      await refresh();
      setDone(true);
    } catch (err) {
      setError((err as Error).message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <DialogFrame
      label="비밀번호 변경"
      eyebrow="내 계정"
      title="비밀번호 변경"
      mode="edit"
      locked
      className="narrow"
      onClose={onClose}
      footer={
        <FootBar
          right={
            done ? (
              <button type="button" className="primary" onClick={onClose}>
                확인
              </button>
            ) : (
              <>
                <button type="button" onClick={onClose} disabled={busy}>
                  취소
                </button>
                <button type="submit" form="pw-form" className="primary" disabled={busy || !current || !next || !again}>
                  변경
                </button>
              </>
            )
          }
        />
      }
    >
      {done ? (
        <Banner tone="info">비밀번호를 변경했습니다. 다음 로그인부터 새 비밀번호를 사용하세요.</Banner>
      ) : (
        <form id="pw-form" onSubmit={submit}>
          {error && <Banner tone="danger">{error}</Banner>}
          <FormGrid>
            <FormField label="현재 비밀번호" required wide>
              <input type="password" autoComplete="current-password" value={current} onChange={(e) => setCurrent(e.target.value)} autoFocus />
            </FormField>
            <FormField label="새 비밀번호" required wide hint="6자 이상, 로그인 ID 와 달라야 합니다">
              <input type="password" autoComplete="new-password" value={next} onChange={(e) => setNext(e.target.value)} minLength={6} maxLength={72} />
            </FormField>
            <FormField label="새 비밀번호 확인" required wide>
              <input type="password" autoComplete="new-password" value={again} onChange={(e) => setAgain(e.target.value)} />
            </FormField>
          </FormGrid>
        </form>
      )}
    </DialogFrame>
  );
}
