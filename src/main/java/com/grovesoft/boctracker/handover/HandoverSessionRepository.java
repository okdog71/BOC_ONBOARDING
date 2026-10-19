package com.grovesoft.boctracker.handover;

import com.grovesoft.boctracker.common.entity.StatusCount;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HandoverSessionRepository extends JpaRepository<HandoverSession, Long> {

    @Query("""
            select h from HandoverSession h
            where (:status is null or h.status = :status)
              and (:week is null or h.week = :week)
              and (:category is null or h.category = :category)
              and (:person is null or h.handoverFrom = :person or h.handoverTo = :person)
              and (:q is null
                   or lower(h.code) like lower(concat('%', :q, '%'))
                   or lower(h.topic) like lower(concat('%', :q, '%'))
                   or lower(h.domain) like lower(concat('%', :q, '%')))
              and (:overdueOnly = false
                   or (h.plannedDate is not null and h.plannedDate < :today and h.status <> '완료'))
            """)
    Page<HandoverSession> search(@Param("status") String status, @Param("week") String week,
                                 @Param("category") String category, @Param("person") String person,
                                 @Param("q") String q, @Param("overdueOnly") boolean overdueOnly,
                                 @Param("today") LocalDate today, Pageable pageable);

    boolean existsByCode(String code);

    long countByStatus(String status);

    long countByPlannedDateIsNotNull();

    List<HandoverSession> findByMilestoneId(Long milestoneId);

    List<HandoverSession> findByMilestoneIsNull();

    @Query("select h.status as status, count(h) as count from HandoverSession h group by h.status")
    List<StatusCount> countGroupByStatus();

    /** (수급 산출물 id, 세션 코드) 쌍 */
    @Query("select d.id as refId, h.code as code from HandoverSession h join h.supplyDocs d order by h.week, h.sortOrder")
    List<RefCode> supplyRefs();

    @Query("select a.id as refId, h.code as code from HandoverSession h join h.accessItems a order by h.week, h.sortOrder")
    List<RefCode> accessRefs();

    interface RefCode {
        Long getRefId();

        String getCode();
    }

    interface MilestoneCount {
        Long getMilestoneId();

        long getTotal();

        long getDone();
    }

    /** 마일스톤별 연결 세션 수/완료 수 */
    @Query("""
            select h.milestone.id as milestoneId, count(h) as total,
                   sum(case when h.status = '완료' then 1L else 0L end) as done
            from HandoverSession h where h.milestone is not null group by h.milestone.id
            """)
    List<MilestoneCount> countByMilestone();
}
