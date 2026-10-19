import { useRef, useState } from 'react';
import { api } from '../api';
import type { SupplyDoc, SupplyDocFile } from '../types';
import { Banner, Section } from '../ui/detail';

export function formatSize(n: number): string {
  if (n < 1024) return `${n} B`;
  if (n < 1024 * 1024) return `${(n / 1024).toFixed(1)} KB`;
  return `${(n / 1024 / 1024).toFixed(1)} MB`;
}

const MAX_BYTES = 50 * 1024 * 1024;
const BLOCKED = ['exe', 'bat', 'cmd', 'com', 'msi', 'scr', 'sh', 'ps1', 'vbs', 'jar', 'dll', 'js'];

/** 첨부 파일 목록(다운로드). readOnly 가 아니면 끌어놓기/선택 업로드와 삭제를 제공한다. */
export default function FilesSection({
  doc,
  readOnly = false,
  onChanged,
}: {
  doc: Pick<SupplyDoc, 'id' | 'code' | 'supplyStatus' | 'files'>;
  readOnly?: boolean;
  /** 업로드·삭제로 서버 데이터가 바뀜 */
  onChanged: () => void;
}) {
  const [busy, setBusy] = useState(false);
  const [over, setOver] = useState(false);
  const [error, setError] = useState<string>();
  const input = useRef<HTMLInputElement>(null);
  const base = `/api/supply-docs/${doc.id}/files`;

  async function uploadAll(files: File[]) {
    if (files.length === 0) return;
    setBusy(true);
    setError(undefined);
    const failed: string[] = [];
    let uploaded = 0;
    for (const f of files) {
      const ext = f.name.split('.').pop()?.toLowerCase() ?? '';
      if (f.size > MAX_BYTES) failed.push(`${f.name}: 50MB를 넘습니다.`);
      else if (BLOCKED.includes(ext)) failed.push(`${f.name}: 올릴 수 없는 형식(.${ext})입니다.`);
      else
        try {
          await api.upload(base, f);
          uploaded += 1;
        } catch (e) {
          failed.push(`${f.name}: ${(e as Error).message}`);
        }
    }
    if (failed.length) setError(failed.join('\n'));
    // 받은 파일이 생겼는데 아직 수령 처리 전이면 상태 변경을 제안한다
    if (uploaded > 0 && (doc.supplyStatus === '미요청' || doc.supplyStatus === '요청')) {
      if (window.confirm(`파일을 첨부했습니다. 이 산출물을 "수령"으로 변경할까요?\n(현재: ${doc.supplyStatus})`)) {
        try {
          await api.patchStatus(`/api/supply-docs/${doc.id}`, '수령');
        } catch (e) {
          setError((e as Error).message);
        }
      }
    }
    setBusy(false);
    if (input.current) input.current.value = '';
    if (uploaded > 0 || failed.length === 0) onChanged();
  }

  async function remove(f: SupplyDocFile) {
    if (!window.confirm(`첨부 파일을 삭제할까요?\n${f.name}`)) return;
    setBusy(true);
    setError(undefined);
    try {
      await api.send('DELETE', `${base}/${f.id}`);
      onChanged();
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <Section title="첨부 파일" aside={doc.files.length > 0 ? `${doc.files.length}개` : undefined}>
      <div className="files">
        {error && (
          <Banner tone="danger">
            <span className="pre">{error}</span>
          </Banner>
        )}
        {doc.files.length === 0 && <p className="muted small">첨부된 파일이 없습니다.</p>}
        <ul className="file-list">
          {doc.files.map((f) => (
            <li key={f.id} className="file-item">
              <a className="file-name" href={f.downloadUrl} title={`${f.name} 내려받기`}>
                📄 {f.name}
              </a>
              <span className="file-meta">
                {formatSize(f.size)} · {f.uploadedAt.slice(0, 10)}
              </span>
              {!readOnly && (
                <button type="button" className="icon" title="삭제" aria-label={`${f.name} 삭제`} disabled={busy} onClick={() => remove(f)}>
                  ✕
                </button>
              )}
            </li>
          ))}
        </ul>
        {!readOnly && (
          <label
            className={`dropzone${over ? ' over' : ''}`}
            onDragOver={(e) => {
              e.preventDefault();
              setOver(true);
            }}
            onDragLeave={() => setOver(false)}
            onDrop={(e) => {
              e.preventDefault();
              setOver(false);
              void uploadAll([...e.dataTransfer.files]);
            }}
          >
            <input ref={input} type="file" multiple disabled={busy} onChange={(e) => void uploadAll([...(e.target.files ?? [])])} />
            {busy ? '올리는 중…' : '파일을 끌어놓거나 눌러서 선택 (최대 50MB, 산출물당 20개)'}
          </label>
        )}
        {!readOnly && <p className="muted small">계정·토큰 등 민감정보가 담긴 파일은 올리지 마세요. 실행 파일(.exe 등)은 올릴 수 없습니다.</p>}
      </div>
    </Section>
  );
}
