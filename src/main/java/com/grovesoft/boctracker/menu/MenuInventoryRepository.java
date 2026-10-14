package com.grovesoft.boctracker.menu;

import com.grovesoft.boctracker.common.entity.GroupCount;
import com.grovesoft.boctracker.common.entity.StatusCount;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MenuInventoryRepository extends JpaRepository<MenuInventory, Long> {

    @Query("""
            select e from MenuInventory e
            where (:status is null or e.analysisStatus = :status)
              and (:priority is null or e.priority = :priority)
              and (:domain is null or e.domain = :domain)
              and (:devCheck is null or e.devCheck = :devCheck)
            """)
    Page<MenuInventory> search(@Param("status") String status, @Param("priority") String priority, @Param("domain") String domain, @Param("devCheck") String devCheck, Pageable pageable);

    long countByAnalysisStatus(String analysisStatus);

    @Query("select e.analysisStatus as status, count(e) as count from MenuInventory e group by e.analysisStatus")
    List<StatusCount> countGroupByStatus();

    /** priority 별 건수(summary). priority 미지정(null) 행은 groupKey=null 로 집계된다. */
    @Query("select e.priority as groupKey, count(e) as count from MenuInventory e group by e.priority")
    List<GroupCount> countGroupByPriority();
}
