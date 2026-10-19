import { api } from '../api';
import { State } from '../components';
import { useAsync } from '../hooks';
import type { Summary } from '../types';

const TITLES: Record<string, string> = {
  milestone: '마일스톤',
  handover: '인수인계(KT)',
  catchup: '캐치업',
  supply: '수급 산출물',
  access: '접근권한',
  menu: '메뉴 분석',
  issue: '이슈·미확인',
  raci: 'RACI',
  review: '회고',
  analysisCard: '분석 카드',
};

/** 섹션의 total 은 제외하고, 값이 {라벨:건수} 맵인 항목(byStatus, byPriority …)을 그룹으로 반환 */
function groupsOf(section: Summary[string]) {
  return Object.entries(section).filter(([, v]) => typeof v === 'object') as [string, Record<string, number>][];
}

export default function Dashboard() {
  const { data, error, loading } = useAsync(() => api.get<Summary>('/api/summary'), []);
  return (
    <State loading={loading} error={error}>
      <div className="cards">
        {data &&
          Object.entries(data).map(([key, section]) => (
            <section className="card" key={key}>
              <h2>
                {TITLES[key] ?? key} <small>총 {section.total}건</small>
              </h2>
              {groupsOf(section).map(([group, counts]) => (
                <div key={group} className="group">
                  <div className="group-name">{group === 'byPriority' ? '우선순위' : '상태'}</div>
                  {Object.entries(counts).map(([label, n]) => (
                    <div className="bar-row" key={label}>
                      <span className="bar-label">{label}</span>
                      <span className="bar-track">
                        <span className="bar-fill" style={{ width: section.total ? `${(n / section.total) * 100}%` : 0 }} />
                      </span>
                      <span className="bar-n">{n}</span>
                    </div>
                  ))}
                </div>
              ))}
            </section>
          ))}
      </div>
    </State>
  );
}
