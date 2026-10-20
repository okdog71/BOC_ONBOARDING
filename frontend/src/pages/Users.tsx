import { useMemo, useState, type FormEvent } from 'react';
import { api } from '../api';
import { useAuth } from '../auth';
import { State } from '../components';
import { useAsync } from '../hooks';
import type { AppUser } from '../types';
import AuditLine from '../ui/AuditLine';
import { Banner, DialogFrame, FootBar, FormField, FormGrid, KV, KVGrid, Section } from '../ui/detail';
import { invalidateUsers } from '../users';

type Field = { key: keyof AppUser & string; label: string; kind?: 'date'; required?: boolean; section: string; hint?: string };

const FIELDS: Field[] = [
  { key: 'loginId', label: '사용자 ID', required: true, section: '기본', hint: '등록 후 변경할 수 없습니다. 초기 비밀번호는 ID 와 같습니다' },
  { key: 'name', label: '이름', required: true, section: '기본' },
  { key: 'affiliation', label: '소속', required: true, section: '기본' },
  { key: 'joinDate', label: '투입일', kind: 'date', section: '기본' },
  { key: 'leaveDate', label: '철수일', kind: 'date', section: '기본' },
  { key: 'kraftonAccount', label: '크래프톤 계정', section: '연락처' },
  { key: 'phone', label: '연락처', section: '연락처' },
  { key: 'groveMail', label: 'Grove 메일', section: '연락처' },
  { key: 'gmail', label: 'G메일', section: '연락처' },
  { key: 'ip', label: 'IP', section: '접속 계정' },
  { key: 'vpn', label: 'VPN', section: '접속 계정' },
  { key: 'bocAccount', label: 'BOC 계정', section: '접속 계정' },
  { key: 'kecAccount', label: 'KEC 계정', section: '접속 계정' },
  { key: 'aws', label: 'AWS', section: '접속 계정' },
];
const SECTIONS = ['기본', '연락처', '접속 계정'];

type Draft = Record<string, string>;
const toDraft = (u?: AppUser): Draft => Object.fromEntries(FIELDS.map((f) => [f.key, u ? String(u[f.key] ?? '') : '']));
const payload = (d: Draft) => Object.fromEntries(Object.entries(d).map(([k, v]) => [k, v.trim() === '' ? null : v.trim()]));

/** 사용자 관리: 내부 사용자 등록·수정·삭제, 비밀번호 초기화. 여기 등록된 사람이 각 화면의 담당자 후보가 된다. */
export default function Users() {
  const list = useAsync(() => api.get<AppUser[]>('/api/users'), []);
  const [selected, setSelected] = useState<AppUser | 'new'>();
  const rows = list.data ?? [];
  const current = useMemo(() => (selected && selected !== 'new' ? rows.find((r) => r.id === selected.id) : undefined), [rows, selected]);

  function changed() {
    invalidateUsers();
    list.reload();
  }

  return (
    <>
      <h2 className="page-title">사용자 관리</h2>
      <p className="muted small">여기에 등록된 사용자가 각 화면의 담당자 선택 목록과 등록자·수정자 이름으로 쓰입니다. 비밀번호·토큰은 이 화면에 입력하지 마세요.</p>
      <div className="toolbar">
        <span className="chip-stat total">전체 {rows.length}명</span>
        <span className="spacer" />
        <button className="primary" onClick={() => setSelected('new')}>
          + 사용자 추가
        </button>
      </div>
      <State loading={list.loading && !list.data} error={list.error}>
        <div className="table-scroll">
          <table className="ms-table">
            <thead>
              <tr>
                <th>사용자 ID</th>
                <th>이름</th>
                <th>소속</th>
                <th>투입일</th>
                <th>철수일</th>
                <th>크래프톤 계정</th>
                <th>연락처</th>
                <th>Grove 메일</th>
                <th>비밀번호</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((u) => (
                <tr key={u.id} className="ms-row clickable" onClick={() => setSelected(u)}>
                  <td className="nowrap">
                    <button
                      type="button"
                      className="link"
                      onClick={(e) => {
                        e.stopPropagation();
                        setSelected(u);
                      }}
                    >
                      {u.loginId}
                    </button>
                  </td>
                  <td className="nowrap">{u.name}</td>
                  <td className="nowrap small">{u.affiliation}</td>
                  <td className="nowrap small">{u.joinDate ?? '-'}</td>
                  <td className="nowrap small">{u.leaveDate ?? '-'}</td>
                  <td className="small">{u.kraftonAccount ?? '-'}</td>
                  <td className="nowrap small">{u.phone ?? '-'}</td>
                  <td className="small">{u.groveMail ?? '-'}</td>
                  <td className="nowrap small">{u.mustChangePassword ? <span className="flag flag-warn">초기값</span> : <span className="muted">변경됨</span>}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </State>
      {selected && <UserDialog user={selected === 'new' ? undefined : (current ?? selected)} onClose={() => setSelected(undefined)} onChanged={changed} />}
    </>
  );
}

function UserDialog({ user, onClose, onChanged }: { user?: AppUser; onClose: () => void; onChanged: () => void }) {
  const { user: me, refresh } = useAuth();
  const creating = !user;
  const [editing, setEditing] = useState(creating);
  const [draft, setDraft] = useState<Draft>(() => toDraft(user));
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string>();
  const isMe = user?.id === me?.id;

  async function run(fn: () => Promise<void>) {
    setBusy(true);
    setError(undefined);
    try {
      await fn();
    } catch (err) {
      setError((err as Error).message);
    } finally {
      setBusy(false);
    }
  }

  function save(e: FormEvent) {
    e.preventDefault();
    void run(async () => {
      if (user) await api.send('PUT', `/api/users/${user.id}`, payload(draft));
      else await api.send('POST', '/api/users', payload(draft));
      if (isMe) await refresh();
      onChanged();
      if (creating) onClose();
      else setEditing(false);
    });
  }

  const remove = () => {
    if (!user || !window.confirm(`${user.name}(${user.loginId}) 사용자를 삭제할까요? 되돌릴 수 없습니다.`)) return;
    void run(async () => {
      await api.send('DELETE', `/api/users/${user.id}`);
      onChanged();
      onClose();
    });
  };

  const reset = () => {
    if (!user || !window.confirm(`${user.name}의 비밀번호를 초기값(사용자 ID)으로 되돌릴까요?`)) return;
    void run(async () => {
      await api.send('POST', `/api/users/${user.id}/password-reset`);
      onChanged();
    });
  };

  return (
    <DialogFrame
      label="사용자"
      eyebrow="사용자 관리"
      code={user?.loginId}
      title={creating ? '새 사용자' : user.name}
      mode={editing ? 'edit' : 'view'}
      locked={editing}
      onClose={onClose}
      meta={
        creating ? (
          <span className="flag flag-edit">✎ 새로 만드는 중</span>
        ) : (
          <>
            {editing && <span className="flag flag-edit">✎ 편집 중</span>}
            <span className="muted">{user.affiliation}</span>
            {user.mustChangePassword && <span className="flag flag-warn">초기 비밀번호</span>}
          </>
        )
      }
      footer={
        editing ? (
          <FootBar
            key="foot-edit"
            right={
              <>
                <button
                  type="button"
                  disabled={busy}
                  onClick={() => {
                    if (creating) onClose();
                    else {
                      setDraft(toDraft(user));
                      setEditing(false);
                      setError(undefined);
                    }
                  }}
                >
                  취소
                </button>
                <button key="btn-save" type="submit" form="user-form" className="primary" disabled={busy}>
                  {creating ? '추가' : '저장'}
                </button>
              </>
            }
          />
        ) : (
          <FootBar
            key="foot-view"
            left={
              <button type="button" onClick={reset} disabled={busy}>
                비밀번호 초기화
              </button>
            }
            right={
              <>
                <button key="btn-delete" type="button" className="danger" onClick={remove} disabled={busy || isMe} title={isMe ? '본인 계정은 삭제할 수 없습니다' : undefined}>
                  삭제
                </button>
                <button key="btn-edit" type="button" className="primary" onClick={() => setEditing(true)} disabled={busy}>
                  편집
                </button>
              </>
            }
          />
        )
      }
    >
      {error && <Banner tone="danger">{error}</Banner>}
      {editing ? (
        <form id="user-form" onSubmit={save}>
          <Banner tone="edit">입력한 뒤 아래 "{creating ? '추가' : '저장'}"을 눌러야 반영됩니다.</Banner>
          {creating && <Banner tone="info">초기 비밀번호는 사용자 ID 와 같습니다. 첫 로그인 후 변경을 안내합니다.</Banner>}
          {SECTIONS.map((s) => (
            <Section key={s} title={s}>
              <FormGrid>
                {FIELDS.filter((f) => f.section === s).map((f) => (
                  <FormField key={f.key} label={f.label} required={f.required} hint={f.hint} wide={f.key === 'loginId'}>
                    <input
                      type={f.kind === 'date' ? 'date' : 'text'}
                      value={draft[f.key]}
                      readOnly={f.key === 'loginId' && !creating}
                      className={f.key === 'loginId' && !creating ? 'readonly' : undefined}
                      onChange={(e) => setDraft({ ...draft, [f.key]: e.target.value })}
                    />
                  </FormField>
                ))}
              </FormGrid>
            </Section>
          ))}
        </form>
      ) : (
        user && (
          <>
            {SECTIONS.map((s) => (
              <Section key={s} title={s}>
                <KVGrid>
                  {FIELDS.filter((f) => f.section === s).map((f) => (
                    <KV key={f.key} label={f.label}>
                      {user[f.key] as string | null}
                    </KV>
                  ))}
                </KVGrid>
              </Section>
            ))}
            <AuditLine row={user} />
          </>
        )
      )}
    </DialogFrame>
  );
}
