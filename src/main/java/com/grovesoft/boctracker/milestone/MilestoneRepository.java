package com.grovesoft.boctracker.milestone;

import com.grovesoft.boctracker.common.entity.StatusCount;
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
            """)
    Page<Milestone> search(@Param("status") String status, @Param("phase") String phase, Pageable pageable);

    long countByStatus(String status);

    @Query("select m.status as status, count(m) as count from Milestone m group by m.status")
    List<StatusCount> countGroupByStatus();
}
