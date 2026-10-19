package db.migration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

/**
 * 코드 체계 정리: 단계 내 순서(sort_order) 채우기 + 기존 코드 M{n}.{seq} → P{n}.{seq} 변환.
 * (HO.{seq} 는 그대로) 다른 마일스톤의 precondition 문자열에 들어 있는 옛 코드도 함께 바꾼다.
 * milestone_dependency 는 id 로 연결되어 있어 영향 없음.
 */
public class V8__milestone_code_scheme extends BaseJavaMigration {

    private static final Pattern OLD_CODE = Pattern.compile("^M(\\d+)\\.(\\d+)$");
    private static final Pattern OLD_TOKEN = Pattern.compile("(?<![A-Za-z0-9.])M(\\d+)\\.(\\d+)");

    private record Row(long id, String code, String phase, String precondition) {
    }

    @Override
    public void migrate(Context context) throws Exception {
        Map<Long, Row> rows = new LinkedHashMap<>();
        try (Statement st = context.getConnection().createStatement();
             ResultSet rs = st.executeQuery("select id, code, phase, precondition from milestone order by id")) {
            while (rs.next()) {
                rows.put(rs.getLong("id"),
                        new Row(rs.getLong("id"), rs.getString("code"), rs.getString("phase"), rs.getString("precondition")));
            }
        }

        Set<String> taken = new HashSet<>();
        rows.values().forEach(r -> taken.add(r.code()));
        Map<String, String> renamed = new HashMap<>();
        for (Row r : rows.values()) {
            Matcher m = OLD_CODE.matcher(r.code());
            if (m.matches()) {
                String next = "P" + m.group(1) + "." + m.group(2);
                if (!taken.contains(next)) {
                    renamed.put(r.code(), next);
                    taken.add(next);
                }
            }
        }

        Map<String, Integer> orderInPhase = new HashMap<>();
        try (PreparedStatement ps = context.getConnection().prepareStatement(
                "update milestone set code = ?, precondition = ?, sort_order = ? where id = ?")) {
            for (Row r : rows.values()) {
                String code = renamed.getOrDefault(r.code(), r.code());
                ps.setString(1, code);
                ps.setString(2, rewrite(r.precondition(), renamed));
                ps.setInt(3, orderInPhase.merge(r.phase(), 1, Integer::sum));
                ps.setLong(4, r.id());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private String rewrite(String precondition, Map<String, String> renamed) {
        if (precondition == null) {
            return null;
        }
        Matcher m = OLD_TOKEN.matcher(precondition);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String old = m.group();
            m.appendReplacement(sb, Matcher.quoteReplacement(renamed.getOrDefault(old, old)));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
