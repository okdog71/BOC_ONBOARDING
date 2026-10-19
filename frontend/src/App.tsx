import { NavLink, Navigate, Route, Routes } from 'react-router-dom';
import Dashboard from './pages/Dashboard';
import Milestones from './pages/Milestones';
import Handovers from './pages/Handovers';
import SupplyDocs from './pages/SupplyDocs';
import AccessItems from './pages/AccessItems';

export default function App() {
  return (
    <div className="app">
      <header>
        <h1>BOC 온보딩·인수인계 트래커</h1>
        <nav>
          <NavLink to="/dashboard">대시보드</NavLink>
          <NavLink to="/milestones">마일스톤</NavLink>
          <NavLink to="/supply-docs">② 수급 산출물</NavLink>
          <NavLink to="/access-items">③ 접근권한</NavLink>
          <NavLink to="/handover-sessions">⑤ 인수인계(KT)</NavLink>
        </nav>
      </header>
      <main>
        <Routes>
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/milestones" element={<Milestones />} />
          <Route path="/supply-docs" element={<SupplyDocs />} />
          <Route path="/access-items" element={<AccessItems />} />
          <Route path="/handover-sessions" element={<Handovers />} />
          <Route path="/handovers" element={<Navigate to="/handover-sessions" replace />} />
        </Routes>
      </main>
    </div>
  );
}
