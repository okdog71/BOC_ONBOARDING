package com.grovesoft.boctracker.access;

import com.grovesoft.boctracker.common.entity.StatusCount;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccessItemRepository extends JpaRepository<AccessItem, Long> {

    @Query("""
            select e from AccessItem e
            where (:status is null or e.status = :status)
              and (:category is null or e.category = :category)
            """)
    Page<AccessItem> search(@Param("status") String status, @Param("category") String category, Pageable pageable);

    long countByStatus(String status);

    @Query("select e.status as status, count(e) as count from AccessItem e group by e.status")
    List<StatusCount> countGroupByStatus();
}
