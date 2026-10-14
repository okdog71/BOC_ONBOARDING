package com.grovesoft.boctracker.analysiscard;

import com.grovesoft.boctracker.common.entity.StatusCount;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnalysisCardRepository extends JpaRepository<AnalysisCard, Long> {

    @Query("""
            select e from AnalysisCard e
            where (:status is null or e.status = :status)
              and (:domain is null or e.domain = :domain)
            """)
    Page<AnalysisCard> search(@Param("status") String status, @Param("domain") String domain, Pageable pageable);

    long countByStatus(String status);

    @Query("select e.status as status, count(e) as count from AnalysisCard e group by e.status")
    List<StatusCount> countGroupByStatus();
}
