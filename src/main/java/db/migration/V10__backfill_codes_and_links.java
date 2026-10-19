package db.migration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

/**
 * 기존 행 백필: ②③⑤ 의 코드·순서 부여, 인수인계 → 마일스톤(HO.n) 연결, 인수인계 선행 텍스트에 적힌 수급/접근 항목 연결.
 * 코드 규칙: 수급 {구분 알파벳}.{n}, 접근 AC.{n}, 인수인계 {주차}.{n} (id 순서대로 부여).
 * 새 DB 는 비어 있으므로 아무 일도 하지 않고, 이후 seed 가 서비스 경로로 같은 규칙을 적용한다.
 */
public class V10__backfill_codes_and_links extends BaseJavaMigration {

    private static final Pattern LETTER = Pattern.compile("^([A-Za-z])\\s*\\.");

    /** 인수인계 도메인 → 선행 텍스트(수급:/접근:)에 적혀 있던 항목. 이름은 seed 의 정확한 값. */
    private static final Map<String, String[]> SUPPLY_BY_DOMAIN = Map.of(
            "전체 개요", new String[] {"시스템 구조/아키텍처 문서"},
            "환경·배포", new String[] {"배포 구조·환경별 구성"},
            "권한 심화·운영자 가이드", new String[] {"운영자 가이드/FAQ"},
            "장애 대응 실습", new String[] {"장애 대응 가이드/에스컬레이션"},
            "배치·스케줄러", new String[] {"정기 배치·스케줄러 목록"});
    private static final Map<String, String[]> ACCESS_BY_DOMAIN = Map.of(
            "환경·배포", new String[] {"ArgoCD/config 저장소(참조)"},
            "모니터링·장애", new String[] {"Datadog(대시보드/로그 참조)"});

    @Override
    public void migrate(Context context) throws Exception {
        Connection c = context.getConnection();
        assignCodes(c, "supply_doc", "category", true);
        assignCodes(c, "access_item", "category", false);
        assignHandoverCodes(c);
        linkHandovers(c);
    }

    private record Row(long id, String group) {
    }

    private List<Row> rows(Connection c, String table, String groupCol, String where) throws Exception {
        List<Row> out = new ArrayList<>();
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("select id, " + groupCol + " from " + table + " " + where + " order by id")) {
            while (rs.next()) {
                out.add(new Row(rs.getLong(1), rs.getString(2)));
            }
        }
        return out;
    }

    private void assignCodes(Connection c, String table, String groupCol, boolean letterKey) throws Exception {
        Map<String, Integer> seqByKey = new HashMap<>();
        Map<String, Integer> orderByGroup = new HashMap<>();
        try (PreparedStatement ps = c.prepareStatement("update " + table + " set code = ?, sort_order = ? where id = ?")) {
            for (Row r : rows(c, table, groupCol, "where code is null")) {
                String key = "AC";
                if (letterKey) {
                    Matcher m = LETTER.matcher(r.group().trim());
                    key = m.find() ? m.group(1).toUpperCase() : "S";
                }
                ps.setString(1, key + "." + seqByKey.merge(key, 1, Integer::sum));
                ps.setInt(2, orderByGroup.merge(r.group(), 1, Integer::sum));
                ps.setLong(3, r.id());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void assignHandoverCodes(Connection c) throws Exception {
        Map<String, Integer> seqByWeek = new HashMap<>();
        try (PreparedStatement ps = c.prepareStatement(
                "update handover_session set code = ?, sort_order = ?, completed_date = case when status = '완료' "
                        + "then cast(updated_at as date) else null end where id = ?")) {
            for (Row r : rows(c, "handover_session", "week", "where code is null")) {
                int n = seqByWeek.merge(r.group(), 1, Integer::sum);
                ps.setString(1, r.group() + "." + n);
                ps.setInt(2, n);
                ps.setLong(3, r.id());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private Map<String, Long> idByName(Connection c, String table, String nameCol) throws Exception {
        Map<String, Long> out = new LinkedHashMap<>();
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("select id, " + nameCol + " from " + table)) {
            while (rs.next()) {
                out.put(rs.getString(2), rs.getLong(1));
            }
        }
        return out;
    }

    private void linkHandovers(Connection c) throws Exception {
        Map<String, Long> milestoneByCode = idByName(c, "milestone", "code");
        Map<String, Long> supplyByName = idByName(c, "supply_doc", "name");
        Map<String, Long> accessByTarget = idByName(c, "access_item", "target");

        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("select id, week, domain from handover_session order by id");
             PreparedStatement ms = c.prepareStatement("update handover_session set milestone_id = ? where id = ?");
             PreparedStatement ps = c.prepareStatement("insert into handover_supply_doc (handover_id, supply_doc_id) values (?, ?)");
             PreparedStatement as = c.prepareStatement("insert into handover_access_item (handover_id, access_item_id) values (?, ?)")) {
            while (rs.next()) {
                long id = rs.getLong("id");
                String week = rs.getString("week");
                String domain = rs.getString("domain");
                Long milestone = milestoneByCode.get("HO." + week.substring(1));
                if (milestone != null) {
                    ms.setLong(1, milestone);
                    ms.setLong(2, id);
                    ms.addBatch();
                }
                for (String name : SUPPLY_BY_DOMAIN.getOrDefault(domain, new String[0])) {
                    Long ref = supplyByName.get(name);
                    if (ref != null) {
                        ps.setLong(1, id);
                        ps.setLong(2, ref);
                        ps.addBatch();
                    }
                }
                for (String target : ACCESS_BY_DOMAIN.getOrDefault(domain, new String[0])) {
                    Long ref = accessByTarget.get(target);
                    if (ref != null) {
                        as.setLong(1, id);
                        as.setLong(2, ref);
                        as.addBatch();
                    }
                }
            }
            ms.executeBatch();
            ps.executeBatch();
            as.executeBatch();
        }
    }
}
