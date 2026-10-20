import { displayName, useUsers } from '../users';

export interface AuditFields {
  createdAt?: string | null;
  updatedAt?: string | null;
  createdBy?: string | null;
  updatedBy?: string | null;
}

const when = (iso?: string | null) => (iso ? iso.replace('T', ' ').slice(0, 16) : '-');

/** 상세 팝업 맨 아래: 등록자·등록일시 / 수정자·수정일시 */
export default function AuditLine({ row }: { row: AuditFields }) {
  const users = useUsers();
  if (!row.createdAt && !row.updatedAt) return null;
  return (
    <dl className="audit-line" aria-label="등록·수정 이력">
      <div>
        <dt>등록</dt>
        <dd>
          {displayName(users, row.createdBy)} · {when(row.createdAt)}
        </dd>
      </div>
      <div>
        <dt>수정</dt>
        <dd>
          {displayName(users, row.updatedBy)} · {when(row.updatedAt)}
        </dd>
      </div>
    </dl>
  );
}
