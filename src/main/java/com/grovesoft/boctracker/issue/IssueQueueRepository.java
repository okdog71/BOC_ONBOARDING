package com.grovesoft.boctracker.issue;

import com.grovesoft.boctracker.common.entity.StatusCount;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IssueQueueRepository extends JpaRepository<IssueQueue, Long> {

    @Query("""
            select e from IssueQueue e
            where (:status is null or e.status = :status)
              and (:type is null or e.type = :type)
            """)
    Page<IssueQueue> search(@Param("status") String status, @Param("type") String type, Pageable pageable);

    long countByStatus(String status);

    @Query("select e.status as status, count(e) as count from IssueQueue e group by e.status")
    List<StatusCount> countGroupByStatus();
}
