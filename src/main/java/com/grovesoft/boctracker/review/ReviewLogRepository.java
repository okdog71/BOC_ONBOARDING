package com.grovesoft.boctracker.review;

import com.grovesoft.boctracker.common.entity.StatusCount;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewLogRepository extends JpaRepository<ReviewLog, Long> {

    @Query("""
            select e from ReviewLog e
            where (:status is null or e.status = :status)
            """)
    Page<ReviewLog> search(@Param("status") String status, Pageable pageable);

    long countByStatus(String status);

    @Query("select e.status as status, count(e) as count from ReviewLog e group by e.status")
    List<StatusCount> countGroupByStatus();
}
