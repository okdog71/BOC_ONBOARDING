import { Fragment, useEffect, useMemo, useState, type ReactNode } from 'react';
import { api } from '../api';
import { StatusBadge, StatusSelect } from '../components';
import { phaseHue } from '../phase';
import { Banner, DialogFrame, Facts, FootBar, FormField, FormGrid, KV, KVGrid, Section } from '../ui/detail';
import type { BaseRow, Ctx, Draft, FieldDef, ResourceConfig } from './types';

/** 빈 문자열은 null 로 보내 서버에서 "미입력"으로 저장하고, 코드는 서버가 관리하므로 보내지 않는다. */
const toPayload = (d: Draft) =>
  Object.fromEntries(
    Object.entries(d)
      .filter(([k]) => k !== 'code')
      .map(([k, v]) => [k, typeof v === 'string' && v.trim() === '' ? null : v]),
  );

const DEFAULT_SECTION = '기본';

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

/** 필드를 구역(section)별로 묶는다. 구역 순서는 처음 등장한 순서이고, 그룹 필드는 '기본' 맨 앞에서 따로 그린다. */
function bySection<T extends BaseRow>(fields: FieldDef<T>[], groupField: string) {
  const map = new Map<string, FieldDef<T>[]>();
  fields
    .filter((f) => f.key !== groupField)
    .forEach((f) => {
      const s = f.section ?? DEFAULT_SECTION;
      map.set(s, [...(map.get(s) ?? []), f]);
    });
  if (!map.has(DEFAULT_SECTION)) map.set(DEFAULT_SECTION, []);
  // '기본' 은 항상 맨 앞
  const base = map.get(DEFAULT_SECTION)!;
  map.delete(DEFAULT_SECTION);
  return [[DEFAULT_SECTION, base] as const, ...map.entries()];
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

  const sections = useMemo(() => bySection(config.fields, config.groupField), [config.fields, config.groupField]);
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

  function cancelEdit() {
    if (creating) onClose();
    else {
      setDraft(config.toDraft(row));
      setEditing(false);
      setError(undefined);
    }
  }

  const statusValue = row ? String(row[config.statusField]) : '';
  const groupValue = String(draft[config.groupField] ?? '');
  const title = creating ? `새 ${config.itemLabel}` : String(draft[config.titleField] || row.code);

  // ---------- 읽기 ----------
  const viewValue = (f: FieldDef<T>): ReactNode => {
    const v = draft[f.key];
    return v === null || v === undefined || v === '' ? null : <span className="pre">{String(v)}</span>;
  };

  const readBody = row && (
    <>
      {(row.overdue || row.warnings.length > 0) && (
        <Banner tone={row.overdue ? 'danger' : 'warn'}>
          <ul>
            {row.overdue && <li>목표 일정이 지났습니다.</li>}
            {row.warnings.map((w) => (
              <li key={w}>{w}</li>
            ))}
          </ul>
        </Banner>
      )}
      {config.alerts?.(row)}
      {config.facts && <Facts items={config.facts(row)} />}
      {sections.map(([name, fields]) => {
        const shown = fields.filter((f) => f.kind !== 'custom' && f.key !== config.statusField && f.key !== config.titleField);
        const isBase = name === DEFAULT_SECTION;
        if (shown.length === 0 && !isBase) return null;
        return (
          <Section key={name} title={name}>
            <KVGrid>
              {isBase && (
                <KV label={config.groupLabel}>
                  {groupValue} <span className="muted">· No. {row.no}</span>
                </KV>
              )}
              {shown.map((f) => (
                <KV key={f.key} label={f.label} wide={f.wide || f.kind === 'textarea'}>
                  {viewValue(f)}
                </KV>
              ))}
            </KVGrid>
          </Section>
        );
      })}
      {config.detail?.(row, ctx)}
    </>
  );

  // ---------- 편집 ----------
  const renderField = (f: FieldDef<T>) => {
    const value = String(draft[f.key] ?? '');
    const wide = f.wide || f.kind === 'textarea' || f.kind === 'custom';
    const common = { label: f.label, required: f.required, hint: f.hint, wide };
    switch (f.kind) {
      case 'textarea':
        return (
          <FormField {...common}>
            <textarea rows={3} value={value} placeholder={f.placeholder} onChange={(e) => set(f.key, e.target.value)} />
          </FormField>
        );
      case 'date':
        return (
          <FormField {...common}>
            <input type="date" value={value} onChange={(e) => set(f.key, e.target.value)} />
          </FormField>
        );
      case 'select':
        return (
          <FormField {...common}>
            <select value={value} onChange={(e) => set(f.key, e.target.value)}>
              {f.allowEmpty !== false && <option value="">(선택 안 함)</option>}
              {f.options.map((o) => (
                <option key={o}>{o}</option>
              ))}
            </select>
          </FormField>
        );
      case 'combo':
        return (
          <FormField {...common}>
            <input list={`dl-${f.key}`} value={value} placeholder={f.placeholder} onChange={(e) => set(f.key, e.target.value)} />
            <datalist id={`dl-${f.key}`}>
              {f.options(ctx.rows).map((o) => (
                <option key={o} value={o} />
              ))}
            </datalist>
          </FormField>
        );
      case 'custom':
        return (
          <FormField {...common} as="div">
            {f.render(draft, set, ctx)}
          </FormField>
        );
      default:
        return (
          <FormField {...common}>
            <input value={value} placeholder={f.placeholder} onChange={(e) => set(f.key, e.target.value)} />
          </FormField>
        );
    }
  };

  const groupField = (
    <FormField label={config.groupLabel} required>
      {config.fixedGroups ? (
        <select value={groupValue} onChange={(e) => set(config.groupField, e.target.value)}>
          {config.fixedGroups.map((g) => (
            <option key={g}>{g}</option>
          ))}
        </select>
      ) : (
        <>
          <input
            list="group-list"
            value={groupValue}
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
    </FormField>
  );

  const editBody = (
    <form
      id="resource-form"
      onSubmit={(e) => {
        e.preventDefault();
        save();
      }}
    >
      {config.notice && <Banner tone="info">{config.notice}</Banner>}
      {sections.map(([name, fields]) => (
        <Section key={name} title={name}>
          <FormGrid>
            {name === DEFAULT_SECTION && (
              <>
                {groupField}
                <FormField label="코드" hint="자동 부여 · 변경 불가">
                  <input readOnly className="readonly" value={creating ? '저장 시 자동 부여' : row.code} />
                </FormField>
              </>
            )}
            {fields.map((f) => (
              <Fragment key={f.key}>{renderField(f)}</Fragment>
            ))}
          </FormGrid>
        </Section>
      ))}
    </form>
  );

  return (
    <DialogFrame
      label={config.itemLabel}
      eyebrow={config.title}
      code={creating ? undefined : row.code}
      title={title}
      hue={groupValue ? phaseHue(groupValue) : undefined}
      locked={editing}
      onClose={onClose}
      meta={
        creating ? undefined : (
          <>
            <StatusBadge status={statusValue} />
            {row.overdue && <span className="flag flag-danger">지연</span>}
            <span className="muted">
              {groupValue} · No. {row.no}
            </span>
          </>
        )
      }
      footer={
        editing ? (
          <FootBar
            right={
              <>
                <button type="button" disabled={busy} onClick={cancelEdit}>
                  취소
                </button>
                <button type="submit" form="resource-form" className="primary" disabled={busy}>
                  {creating ? '추가' : '저장'}
                </button>
              </>
            }
          />
        ) : (
          <FootBar
            left={
              <label className="foot-status">
                상태
                <StatusSelect value={statusValue} options={config.statuses} disabled={busy} onChange={changeStatus} />
              </label>
            }
            right={
              <>
                <button className="danger" onClick={remove} disabled={busy}>
                  삭제
                </button>
                <button className="primary" onClick={() => setEditing(true)} disabled={busy}>
                  편집
                </button>
              </>
            }
          />
        )
      }
    >
      {error && (
        <Banner tone="danger">
          <span>{error}</span>
        </Banner>
      )}
      {editing ? editBody : readBody}
    </DialogFrame>
  );
}
