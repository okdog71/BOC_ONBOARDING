import { useEffect, useMemo, useState } from 'react';
import type { TableIndex } from './types';
import { ancestorKeys, buildTree, leafTitle, type TableNode } from './tree';

/** 좌측 LNB: 선택된 버전의 표 목록을 트리(2~4단계)로 보여준다. 항목을 누르면 그 표를 본문에 연다 */
export default function TableMenu({
  tables,
  selectedKey,
  onSelect,
}: {
  tables: TableIndex[];
  selectedKey: string | null;
  onSelect: (tableKey: string) => void;
}) {
  const tree = useMemo(() => buildTree(tables), [tables]);
  const [open, setOpen] = useState<Set<string>>(() => ancestorKeys(tables, selectedKey));

  // 선택한 표가 가려지지 않도록 그 표의 상위 항목을 펼친다
  useEffect(() => {
    setOpen((prev) => {
      const next = new Set(prev);
      ancestorKeys(tables, selectedKey).forEach((k) => next.add(k));
      return next;
    });
  }, [tables, selectedKey]);

  const toggle = (key: string) =>
    setOpen((prev) => {
      const next = new Set(prev);
      if (!next.delete(key)) next.add(key);
      return next;
    });

  const renderNode = (node: TableNode, depth: number) => {
    const t = node.table;
    const hasChildren = node.children.length > 0;
    const expanded = open.has(t.tableKey);
    const active = t.tableKey === selectedKey;
    return (
      <li key={t.tableKey}>
        <div className={`pi-menu-row${active ? ' is-active' : ''}`} style={{ paddingLeft: 10 + depth * 14 }}>
          <button
            type="button"
            className="pi-menu-label"
            aria-current={active ? 'page' : undefined}
            title={t.title}
            onClick={() => {
              onSelect(t.tableKey);
              if (hasChildren) setOpen((prev) => new Set(prev).add(t.tableKey));
            }}
          >
            {leafTitle(t)}
          </button>
          {hasChildren && (
            <button
              type="button"
              className="pi-menu-chevron"
              aria-label={`${leafTitle(t)} 하위 표 ${expanded ? '접기' : '펼치기'}`}
              aria-expanded={expanded}
              onClick={() => toggle(t.tableKey)}
            >
              <span aria-hidden="true">{expanded ? '⌄' : '›'}</span>
            </button>
          )}
        </div>
        {hasChildren && expanded && <ul>{node.children.map((c) => renderNode(c, depth + 1))}</ul>}
      </li>
    );
  };

  return (
    <nav className="pi-menu" aria-label="확률표 목록">
      <ul>{tree.map((n) => renderNode(n, 0))}</ul>
    </nav>
  );
}
