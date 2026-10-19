package com.grovesoft.boctracker.milestone;

import com.grovesoft.boctracker.common.entity.StatusCount;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MilestoneRepository extends JpaRepository<Milestone, Long> {

    @Query("""
            select m from Milestone m
            where (:status is null or m.status = :status)
              and (:phase is null or m.phase = :phase)
              and (:owner is null or m.owner = :owner)
              and (:q is null
                   or lower(m.code) like lower(concat('%', :q, '%'))
                   or lower(m.name) like lower(concat('%', :q, '%'))
                   or lower(m.goal) like lower(concat('%', :q, '%')))
              and (:overdueOnly = false
                   or (m.targetDate is not null and m.targetDate < :today and m.status <> '완료'))
            """)
    Page<Milestone> search(@Param("status") String status, @Param("phase") String phase,
                           @Param("owner") String owner, @Param("q") String q,
                           @Param("overdueOnly") boolean overdueOnly, @Param("today") LocalDate today,
                           Pageable pageable);

    long countByStatus(String status);

    boolean existsByCodeAndIdNot(String code, Long id);

    boolean existsByCode(String code);

    java.util.Optional<Milestone> findByCode(String code);

    List<Milestone> findByPhase(String phase);

    List<Milestone> findByPhaseOrderBySortOrderAscIdAsc(String phase);

    @Query("select coalesce(max(m.sortOrder), 0) from Milestone m where m.phase = :phase")
    int maxSortOrder(@Param("phase") String phase);

    @Query("select m.code from Milestone m where m.code like concat(:prefix, '.%')")
    List<String> findCodesByPrefix(@Param("prefix") String prefix);

    /** 이 마일스톤을 선행으로 삼는 마일스톤들. */
    @Query("select m from Milestone m join m.predecessors p where p.id = :id order by m.id")
    List<Milestone> findDependents(@Param("id") Long id);

    @Query("select m.status as status, count(m) as count from Milestone m group by m.status")
    List<StatusCount> countGroupByStatus();

    interface PhaseCount {
        String getPhase();

        long getTotal();

        long getDone();
    }

    /** 단계별 집계. 첫 등장(id 최소) 순 — 문자열 정렬이면 HO 가 P0 보다 앞서는 문제를 피한다. */
    @Query("""
            select m.phase as phase, count(m) as total,
                   sum(case when m.status = '완료' then 1L else 0L end) as done
            from Milestone m group by m.phase order by min(m.id)
            """)
    List<PhaseCount> phaseSummaries();

    @Query("select distinct m.owner from Milestone m where m.owner is not null order by m.owner")
    List<String> findDistinctOwners();
}
