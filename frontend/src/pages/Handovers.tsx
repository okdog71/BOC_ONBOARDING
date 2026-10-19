import { useState } from 'react';
import { api, query, type Page } from '../api';
import { FilterSelect, Pager, State, StatusSelect } from '../components';
import { useAsync, useEnums } from '../hooks';
import type { HandoverSession } from '../types';

export default function Handovers() {
  const enums = useEnums();
  const [status, setStatus] = useState('');
  const [week, setWeek] = useState('');
  const [category, setCategory] = useState('');
  const [page, setPage] = useState(0);
  const [saving, setSaving] = useState<number>();
  const [actionError, setActionError] = useState<string>();

  const { data, error, loading, reload } = useAsync(
    () => api.get<Page<HandoverSession>>('/api/handover-sessions' + query({ status, week, category, page, size: 25 })),
    [status, week, category, page],
  );

  const reset = (setter: (v: string) => void) => (v: string) => {
    setter(v);
    setPage(0);
  };

  async function changeStatus(s: HandoverSession, next: string) {
    setSaving(s.id);
    setActionError(undefined);
    try {
      await api.patchStatus<HandoverSession>(`/api/handover-sessions/${s.id}`, next);
      reload();
    } catch (e) {
      setActionError((e as Error).message);
    } finally {
      setSaving(undefined);
    }
  }

  return (
    <>
      <div className="toolbar">
        <FilterSelect label="주차" value={week} options={enums?.week ?? []} onChange={reset(setWeek)} />
        <FilterSelect label="구분" value={category} options={enums?.category ?? []} onChange={reset(setCategory)} />
        <FilterSelect label="상태" value={status} options={enums?.handoverStatus ?? []} onChange={reset(setStatus)} />
      </div>
      {actionError && <p className="error">상태 변경 실패: {actionError}</p>}
      <State loading={loading} error={error}>
        <table>
          <thead>
            <tr>
              <th>주차</th>
              <th>구분</th>
              <th>도메인</th>
              <th>주제</th>
              <th>KT 단계</th>
              <th>방식</th>
              <th>예정일</th>
              <th>상태</th>
            </tr>
          </thead>
          <tbody>
            {data?.content.map((s) => (
              <tr key={s.id}>
                <td>{s.week}</td>
                <td>{s.category}</td>
                <td>{s.domain ?? '-'}</td>
                <td className="wide">{s.topic}</td>
                <td className="nowrap">{s.ktStage ?? '-'}</td>
                <td>{s.method ?? '-'}</td>
                <td className="nowrap">{s.plannedDate ?? '-'}</td>
                <td>
                  <StatusSelect
                    value={s.status}
                    options={enums?.handoverStatus ?? [s.status]}
                    disabled={saving === s.id}
                    onChange={(v) => changeStatus(s, v)}
                  />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {data && (
          <Pager page={data.page} totalPages={data.totalPages} totalElements={data.totalElements} onPage={setPage} />
        )}
      </State>
    </>
  );
}
