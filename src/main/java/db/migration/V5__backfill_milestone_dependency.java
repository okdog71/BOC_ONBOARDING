package db.migration;

import com.grovesoft.boctracker.milestone.PreconditionParser;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

/** 기존 milestone.precondition 을 파싱해 milestone_dependency 를 채운다(DB 종류 무관). */
public class V5__backfill_milestone_dependency extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Map<String, Long> idByCode = new LinkedHashMap<>();
        Map<Long, String> preconditionById = new LinkedHashMap<>();
        try (Statement st = context.getConnection().createStatement();
             ResultSet rs = st.executeQuery("select id, code, precondition from milestone order by id")) {
            while (rs.next()) {
                idByCode.put(rs.getString("code"), rs.getLong("id"));
                preconditionById.put(rs.getLong("id"), rs.getString("precondition"));
            }
        }
        List<String> orderedCodes = new ArrayList<>(idByCode.keySet());
        try (PreparedStatement ps = context.getConnection().prepareStatement(
                "insert into milestone_dependency (milestone_id, predecessor_id) values (?, ?)")) {
            for (Map.Entry<Long, String> e : preconditionById.entrySet()) {
                for (String code : PreconditionParser.parse(e.getValue(), orderedCodes)) {
                    Long predecessorId = idByCode.get(code);
                    if (predecessorId.equals(e.getKey())) {
                        continue;
                    }
                    ps.setLong(1, e.getKey());
                    ps.setLong(2, predecessorId);
                    ps.addBatch();
                }
            }
            ps.executeBatch();
        }
    }
}
