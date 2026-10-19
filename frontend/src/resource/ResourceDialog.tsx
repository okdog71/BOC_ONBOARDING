import { Fragment, useEffect, useState } from 'react';
import { api } from '../api';
import { StatusSelect } from '../components';
import type { BaseRow, Ctx, Draft, FieldDef, ResourceConfig } from './types';

/** 빈 문자열은 null 로 보내 서버에서 "미입력"으로 저장하고, 코드는 서버가 관리하므로 보내지 않는다. */
const toPayload = (d: Draft) =>
  Object.fromEntries(
    Object.entries(d)
      .filter(([k]) => k !== 'code')
      .map(([k, v]) => [k, typeof v === 'string' && v.trim() === '' ? null : v]),
  );

interface Props<T extends BaseRow> {
  config: ResourceConfig<T>;
  row?: T;
  ctx: Ctx<T>;
  groups: string[];
  defaultGroup?: string;
  onClose: () => void;
  /** 저장·삭제 등 서버 데이터가 바뀜. code 가 있으면 그 항목을 선택한다. */
  onChanged: (selectCode?: string | null) => void;
}

export default function ResourceDialog<T extends BaseRow>({ config, row, ctx, groups, defaultGroup, onClose, onChanged }: Props<T>) {
  const creating = !row;
  const [editing, setEditing] = useState(creating);
  const [draft, setDraft] = useState<Draft>(() => (row ? config.toDraft(row) : config.emptyDraft(defaultGroup)));
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string>();

  // 저장 후 서버 값이 갱신되면 초안을 맞춘다(편집 중에는 덮어쓰지 않는다).
  useEffect(() => {
    if (!editing) setDraft(row ? config.toDraft(row) : config.emptyDraft(defaultGroup));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [row]);
  useEffect(() => {
    setEditing(!row);
    setError(undefined);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [row?.id]);

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && !editing) onClose();
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [editing, onClose]);

  const set = (k: string, v: unknown) => setDraft((d) => ({ ...d, [k]: v }));

  async function run(fn: () => Promise<unknown>) {
    setBusy(true);
    setError(undefined);
    try {
      await fn();
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setBusy(false);
    }
  }

  function save() {
    const missing = config.fields.filter((f) => f.required && !String(draft[f.key] ?? '').trim());
    if (missing.length) {
      setError(`${missing.map((f) => f.label).join(', ')}은(는) 필수입니다.`);
      return;
    }
    run(async () => {
      const saved = creating
        ? await api.send<T>('POST', config.base, toPayload(draft))
        : await api.send<T>('PUT', `${config.base}/${row!.id}`, toPayload(draft));
      setEditing(false);
      onChanged(saved.code);
    });
  }

  function remove() {
    if (!row || !window.confirm(`${row.code}\n정말 삭제할까요? 되돌릴 수 없습니다.`)) return;
    run(async () => {
      await api.send('DELETE', `${config.base}/${row.id}`);
      onChanged(null);
      onClose();
    });
  }

  function changeStatus(next: string) {
    if (!row) return;
    const msg = config.confirmStatus?.(row, next);
    if (msg && !window.confirm(msg)) return;
    run(async () => {
      await api.patchStatus<T>(`${config.base}/${row.id}`, next);
      onChanged(row.code);
    });
  }

  const statusValue = row ? String(row[config.statusField]) : '';
  const view = (f: FieldDef<T>) => {
    if (f.kind === 'custom') return null;
    const v = draft[f.key];
    return (
      <Fragment key={f.key}>
        <dt>{f.label}</dt>
        <dd>{v === null || v === undefined || v === '' ? '-' : String(v)}</dd>
      </Fragment>
    );
  };

  return (
    <div
      className="modal-backdrop"
      onMouseDown={(e) => {
        if (e.target === e.currentTarget && !editing) onClose();
      }}
    >
      <div className="modal" role="dialog" aria-modal="true" aria-label={config.itemLabel}>
        <div className="panel-head">
          <h2>
            {creating ? `새 ${config.itemLabel}` : String(draft[config.titleField] || row.code)}
            {!creating && <span className="code-tag">{row.code}</span>}
          </h2>
          <button onClick={onClose} aria-label="닫기">
            ✕
          </button>
        </div>
        {config.notice && <p className="notice">{config.notice}</p>}
        {error && <p className="error">{error}</p>}

        {!creating && !editing && (
          <div className="panel-actions">
            <StatusSelect value={statusValue} options={config.statuses} disabled={busy} onChange={changeStatus} />
            <button onClick={() => setEditing(true)}>편집</button>
            <button className="danger" onClick={remove} disabled={busy}>
              삭제
            </button>
          </div>
        )}

        {editing ? (
          <form
            className="form"
            onSubmit={(e) => {
              e.preventDefault();
              save();
            }}
          >
            <label>
              {config.groupLabel} *
              {config.fixedGroups ? (
                <select value={String(draft[config.groupField] ?? '')} onChange={(e) => set(config.groupField, e.target.value)}>
                  {config.fixedGroups.map((g) => (
                    <option key={g}>{g}</option>
                  ))}
                </select>
              ) : (
                <>
                  <input
                    list="group-list"
                    value={String(draft[config.groupField] ?? '')}
                    onChange={(e) => set(config.groupField, e.target.value)}
                    placeholder="기존 값 선택 또는 새로 입력"
                  />
                  <datalist id="group-list">
                    {groups.map((g) => (
                      <option key={g} value={g} />
                    ))}
                  </datalist>
                </>
              )}
            </label>
            <label>
              코드 (자동 부여·변경 불가)
              <input readOnly className="readonly" value={creating ? '저장 시 자동 부여' : row.code} />
            </label>
            {config.fields.map((f) => {
              if (f.key === config.groupField) return null;
              const common = { key: f.key };
              const value = String(draft[f.key] ?? '');
              const label = (
                <>
                  {f.label}
                  {f.required && ' *'}
                  {f.hint && <small className="hint"> {f.hint}</small>}
                </>
              );
              switch (f.kind) {
                case 'textarea':
                  return (
                    <label {...common}>
                      {label}
                      <textarea rows={2} value={value} placeholder={f.placeholder} onChange={(e) => set(f.key, e.target.value)} />
                    </label>
                  );
                case 'date':
                  return (
                    <label {...common}>
                      {label}
                      <input type="date" value={value} onChange={(e) => set(f.key, e.target.value)} />
                    </label>
                  );
                case 'select':
                  return (
                    <label {...common}>
                      {label}
                      <select value={value} onChange={(e) => set(f.key, e.target.value)}>
                        {f.allowEmpty !== false && <option value="">(선택 안 함)</option>}
                        {f.options.map((o) => (
                          <option key={o}>{o}</option>
                        ))}
                      </select>
                    </label>
                  );
                case 'combo':
                  return (
                    <label {...common}>
                      {label}
                      <input list={`dl-${f.key}`} value={value} placeholder={f.placeholder} onChange={(e) => set(f.key, e.target.value)} />
                      <datalist id={`dl-${f.key}`}>
                        {f.options(ctx.rows).map((o) => (
                          <option key={o} value={o} />
                        ))}
                      </datalist>
                    </label>
                  );
                case 'custom':
                  return (
                    <div className="field-custom" {...common}>
                      <span>{label}</span>
                      {f.render(draft, set, ctx)}
                    </div>
                  );
                default:
                  return (
                    <label {...common}>
                      {label}
                      <input value={value} placeholder={f.placeholder} onChange={(e) => set(f.key, e.target.value)} />
                    </label>
                  );
              }
            })}
            <div className="panel-actions">
              <button type="submit" className="primary" disabled={busy}>
                {creating ? '추가' : '저장'}
              </button>
              <button
                type="button"
                disabled={busy}
                onClick={() => {
                  if (creating) onClose();
                  else {
                    setDraft(config.toDraft(row));
                    setEditing(false);
                    setError(undefined);
                  }
                }}
              >
                취소
              </button>
            </div>
          </form>
        ) : (
          row && (
            <>
              {(row.warnings.length > 0 || row.overdue) && (
                <ul className="warn-list">
                  {row.overdue && <li className="overdue">목표 일정이 지났습니다.</li>}
                  {row.warnings.map((w) => (
                    <li key={w}>{w}</li>
                  ))}
                </ul>
              )}
              <dl className="detail">
                <dt>{config.groupLabel}</dt>
                <dd>
                  {String(draft[config.groupField])} <span className="muted">· No. {row.no}</span>
                </dd>
                {config.fields.filter((f) => f.key !== config.groupField).map(view)}
              </dl>
              {config.detail?.(row, ctx)}
            </>
          )
        )}
      </div>
    </div>
  );
}
