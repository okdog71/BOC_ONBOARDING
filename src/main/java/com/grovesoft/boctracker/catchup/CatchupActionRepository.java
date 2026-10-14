package com.grovesoft.boctracker.catchup;

import com.grovesoft.boctracker.common.entity.StatusCount;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CatchupActionRepository extends JpaRepository<CatchupAction, Long> {

    @Query("""
            select e from CatchupAction e
            where (:status is null or e.status = :status)
              and (:timing is null or e.timing = :timing)
            """)
    Page<CatchupAction> search(@Param("status") String status, @Param("timing") String timing, Pageable pageable);

    long countByStatus(String status);

    @Query("select e.status as status, count(e) as count from CatchupAction e group by e.status")
    List<StatusCount> countGroupByStatus();
}
