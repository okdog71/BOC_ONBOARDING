import { api } from '../api';
import { State } from '../components';
import { useAsync } from '../hooks';
import type { LinkReport, Summary } from '../types';

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
  megaProcess: '메가프로세스',
  interfaceLink: '연동 인터페이스',
  docReliability: '문서 신뢰도',
  techDebt: '기술부채',
  handoverQuestion: '인수인계 질의',
};

/** 섹션의 total 은 제외하고, 값이 {라벨:건수} 맵인 항목(byStatus, byPriority …)을 그룹으로 반환 */
function groupsOf(section: Summary[string]) {
  return Object.entries(section).filter(([, v]) => typeof v === 'object') as [string, Record<string, number>][];
}

export default function Dashboard() {
  const { data, error, loading } = useAsync(() => api.get<Summary>('/api/summary'), []);
  const links = useAsync(() => api.get<LinkReport>('/api/summary/links'), []).data;
  return (
    <State loading={loading} error={error}>
      {links && (
        <section className={`card link-audit${links.issues.length ? ' has-issues' : ''}`}>
          <h2>
            메뉴·문서 링크 점검 <small>{links.issues.length === 0 ? '이상 없음' : `문제 ${links.issues.length}건`}</small>
          </h2>
          <ul className="audit-checks">
            {links.checks.map((c) => (
              <li key={c.name} className={c.problems ? 'bad' : 'ok'}>
                {c.problems ? '⚠' : '✓'} {c.name} <span className="muted">({c.checked}건 점검{c.problems ? `, 문제 ${c.problems}` : ''})</span>
              </li>
            ))}
          </ul>
          {links.issues.length > 0 && (
            <ul className="audit-issues">
              {links.issues.map((i) => (
                <li key={i.source + i.message}>
                  <b>{i.source}</b> {i.message}
                </li>
              ))}
            </ul>
          )}
        </section>
      )}
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
