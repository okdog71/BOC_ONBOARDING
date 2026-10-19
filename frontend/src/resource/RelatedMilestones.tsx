import { Link } from 'react-router-dom';
import { api, type Page } from '../api';
import { useAsync } from '../hooks';
import type { Milestone } from '../types';

/**
 * 이 메뉴(시트 기호)를 산출물로 삼는 마일스톤을 보여 준다 — 메뉴 → 마일스톤 방향의 링크.
 * 반대 방향(마일스톤 → 메뉴)은 마일스톤 화면의 산출물/연결 현황 칩이 담당한다.
 */
export default function RelatedMilestones({ sheet, label }: { sheet: string; label: string }) {
  const { data } = useAsync(() => api.get<Page<Milestone>>('/api/milestones?size=500&sort=id,asc'), []);
  const related = (data?.content ?? []).filter((m) => m.output?.includes(sheet));
  if (!data) return null;
  return (
    <div className="related" aria-label="관련 마일스톤">
      <span className="related-title">관련 마일스톤</span>
      {related.length === 0 && <span className="muted small">이 {label}을(를) 산출물로 삼는 마일스톤이 없습니다.</span>}
      {related.map((m) => {
        const progress = m.links.find((l) => l.sheet === sheet);
        return (
          <Link key={m.id} className="chip related-chip" to={`/milestones?code=${m.code}`} title={`${m.name} — ${m.status}`}>
            <b>{m.code}</b> {m.name}
            <span className={`badge s-${m.status}`}>{m.status}</span>
            {progress && (
              <span className="muted">
                {' '}
                {progress.metric} {progress.done}/{progress.total}
              </span>
            )}
          </Link>
        );
      })}
    </div>
  );
}
