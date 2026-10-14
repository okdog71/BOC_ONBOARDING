package com.grovesoft.boctracker.raci;

import com.grovesoft.boctracker.common.entity.StatusCount;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RaciAssignmentRepository extends JpaRepository<RaciAssignment, Long> {

    @Query("""
            select e from RaciAssignment e
            where (:status is null or e.status = :status)
            """)
    Page<RaciAssignment> search(@Param("status") String status, Pageable pageable);

    long countByStatus(String status);

    @Query("select e.status as status, count(e) as count from RaciAssignment e group by e.status")
    List<StatusCount> countGroupByStatus();
}
