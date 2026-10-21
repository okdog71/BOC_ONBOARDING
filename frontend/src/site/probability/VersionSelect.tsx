import type { VersionItem } from './types';

/** 적용일 선택(제목 오른쪽). 라벨은 API 가 준 값을 그대로 쓴다('2026-10-21 (수정)') */
export default function VersionSelect({
  versions,
  value,
  disabled,
  onChange,
}: {
  versions: VersionItem[];
  value: number;
  disabled?: boolean;
  onChange: (version: number) => void;
}) {
  return (
    <label className="pi-version">
      <span className="pi-sr">적용일</span>
      <select value={value} disabled={disabled || versions.length === 0} onChange={(e) => onChange(Number(e.target.value))}>
        {versions.map((v) => (
          <option key={v.version} value={v.version}>
            {v.label}
          </option>
        ))}
      </select>
    </label>
  );
}
