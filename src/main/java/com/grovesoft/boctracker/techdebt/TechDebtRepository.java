package com.grovesoft.boctracker.techdebt;

import com.grovesoft.boctracker.common.entity.GroupCount;
import com.grovesoft.boctracker.common.entity.StatusCount;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TechDebtRepository extends JpaRepository<TechDebt, Long> {

    @Query("""
            select e from TechDebt e
            where (:status is null or e.status = :status)
              and (:priority is null or e.priority = :priority)
              and (:system is null or e.system = :system)
              and (:owner is null or e.owner = :owner)
              and (:q is null
                   or lower(e.code) like lower(concat('%', :q, '%'))
                   or lower(e.name) like lower(concat('%', :q, '%'))
                   or lower(e.summary) like lower(concat('%', :q, '%'))
                   or lower(e.area) like lower(concat('%', :q, '%')))
              and (:overdueOnly = false
                   or (e.targetDate is not null and e.targetDate < :today
                       and e.status not in ('완료', '보류')))
            """)
    Page<TechDebt> search(@Param("status") String status, @Param("priority") String priority,
                          @Param("system") String system, @Param("owner") String owner, @Param("q") String q,
                          @Param("overdueOnly") boolean overdueOnly, @Param("today") LocalDate today,
                          Pageable pageable);

    boolean existsByCode(String code);

    @Query("select e.status as status, count(e) as count from TechDebt e group by e.status")
    List<StatusCount> countGroupByStatus();

    @Query("select e.priority as groupKey, count(e) as count from TechDebt e group by e.priority")
    List<GroupCount> countGroupByPriority();
}
