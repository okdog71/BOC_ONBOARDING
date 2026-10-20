import { useState, type FormEvent } from 'react';
import { useAuth } from '../auth';

/** 로그인 화면(내부 사용자 전용). 실패 시 서버 메시지를 그대로 보여 준다. */
export default function Login() {
  const { login } = useAuth();
  const [loginId, setLoginId] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string>();
  const [busy, setBusy] = useState(false);

  async function submit(e: FormEvent) {
    e.preventDefault();
    if (busy) return;
    setBusy(true);
    setError(undefined);
    try {
      await login(loginId.trim(), password);
    } catch (err) {
      setError((err as Error).message);
      setPassword('');
    } finally {
      setBusy(false);
    }
  }

  return (
    <main className="login-page">
      <form className="login-card" onSubmit={submit}>
        <div className="login-brand">
          <span className="brand-mark" aria-hidden="true">B</span>
          <div>
            <strong>BOC</strong>
            <span>온보딩·인수인계 트래커</span>
          </div>
        </div>
        <label className="login-field">
          <span>사용자 ID</span>
          <input autoFocus autoComplete="username" value={loginId} onChange={(e) => setLoginId(e.target.value)} required maxLength={30} />
        </label>
        <label className="login-field">
          <span>비밀번호</span>
          <input type="password" autoComplete="current-password" value={password} onChange={(e) => setPassword(e.target.value)} required maxLength={72} />
        </label>
        {error && (
          <p className="login-error" role="alert">
            {error}
          </p>
        )}
        <button type="submit" className="primary" disabled={busy || !loginId.trim() || !password}>
          {busy ? '확인 중…' : '로그인'}
        </button>
      </form>
    </main>
  );
}
