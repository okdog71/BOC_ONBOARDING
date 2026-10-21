import type { TableIndex } from './types';

export interface TableNode {
  table: TableIndex;
  children: TableNode[];
}

/** 평면 표 목록(원천 순서)을 parentKey 로 트리로 만든다. 상위를 못 찾으면 최상위로 둔다 */
export function buildTree(tables: TableIndex[]): TableNode[] {
  const nodes = new Map<string, TableNode>(tables.map((t) => [t.tableKey, { table: t, children: [] }]));
  const roots: TableNode[] = [];
  for (const t of tables) {
    const node = nodes.get(t.tableKey)!;
    const parent = t.parentKey ? nodes.get(t.parentKey) : undefined;
    (parent ? parent.children : roots).push(node);
  }
  return roots;
}

/** 선택한 표를 보이게 하려고 펼쳐야 하는 상위 표들의 tableKey */
export function ancestorKeys(tables: TableIndex[], tableKey: string | null): Set<string> {
  const byKey = new Map(tables.map((t) => [t.tableKey, t]));
  const keys = new Set<string>();
  let cur = tableKey ? byKey.get(tableKey) : undefined;
  while (cur && cur.parentKey) {
    keys.add(cur.parentKey);
    cur = byKey.get(cur.parentKey);
  }
  return keys;
}

/** LNB 한 칸에 보여줄 이름: 제목 경로의 마지막 구간 */
export function leafTitle(t: TableIndex): string {
  return t.path.length > 0 ? t.path[t.path.length - 1] : t.title;
}

/** 선택한 표 아래의 모든 하위 표(자식·손자…)를 목록 순서대로(트리 전위 순회). 선택한 표 자신은 포함하지 않는다 */
export function descendantsOf(tables: TableIndex[], tableKey: string): TableIndex[] {
  const children = new Map<string, TableIndex[]>();
  for (const t of tables) {
    if (t.parentKey) children.set(t.parentKey, [...(children.get(t.parentKey) ?? []), t]);
  }
  const out: TableIndex[] = [];
  const walk = (key: string) => {
    for (const c of children.get(key) ?? []) {
      out.push(c);
      walk(c.tableKey);
    }
  };
  walk(tableKey);
  return out;
}
