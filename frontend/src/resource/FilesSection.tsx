import { useRef, useState } from 'react';
import { api } from '../api';
import type { SupplyDocFile } from '../types';
import { Banner, Section } from '../ui/detail';

export function formatSize(n: number): string {
  if (n < 1024) return `${n} B`;
  if (n < 1024 * 1024) return `${(n / 1024).toFixed(1)} KB`;
  return `${(n / 1024 / 1024).toFixed(1)} MB`;
}

const MAX_BYTES = 50 * 1024 * 1024;
const BLOCKED = ['exe', 'bat', 'cmd', 'com', 'msi', 'scr', 'sh', 'ps1', 'vbs', 'jar', 'dll', 'js'];

/**
 * 첨부 파일 목록(다운로드). readOnly 가 아니면 끌어놓기/선택 업로드와 삭제를 제공한다.
 * 수급 산출물·기술부채·인수인계 질의가 함께 쓴다(base 만 다르다).
 */
export default function FilesSection({
  doc,
  base,
  noun = '항목',
  title = '첨부 파일',
  readOnly = false,
  onChanged,
  afterUpload,
}: {
  doc: { id: number; files: SupplyDocFile[] };
  /** API 경로, 예: /api/supply-docs */
  base: string;
  /** 안내 문구에 쓰는 이름(산출물·항목 …) */
  noun?: string;
  title?: string;
  readOnly?: boolean;
  /** 업로드·삭제로 서버 데이터가 바뀜 */
  onChanged: () => void;
  /** 업로드가 하나 이상 성공한 뒤 호출(예: 수령 처리 제안). 오류 메시지를 던지면 화면에 보인다 */
  afterUpload?: () => Promise<void>;
}) {
  const [busy, setBusy] = useState(false);
  const [over, setOver] = useState(false);
  const [error, setError] = useState<string>();
  const input = useRef<HTMLInputElement>(null);
  const filesUrl = `${base}/${doc.id}/files`;

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
          await api.upload(filesUrl, f);
          uploaded += 1;
        } catch (e) {
          failed.push(`${f.name}: ${(e as Error).message}`);
        }
    }
    if (failed.length) setError(failed.join('\n'));
    if (uploaded > 0 && afterUpload) {
      try {
        await afterUpload();
      } catch (e) {
        setError((e as Error).message);
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
      await api.send('DELETE', `${filesUrl}/${f.id}`);
      onChanged();
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <Section title={title} aside={doc.files.length > 0 ? `${doc.files.length}개` : undefined}>
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
            {busy ? '올리는 중…' : `파일을 끌어놓거나 눌러서 선택 (최대 50MB, ${noun}당 20개)`}
          </label>
        )}
        {!readOnly && <p className="muted small">계정·토큰 등 민감정보가 담긴 파일은 올리지 마세요. 실행 파일(.exe 등)은 올릴 수 없습니다.</p>}
      </div>
    </Section>
  );
}
