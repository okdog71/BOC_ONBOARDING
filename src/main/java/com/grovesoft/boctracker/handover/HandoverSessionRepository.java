package com.grovesoft.boctracker.handover;

import com.grovesoft.boctracker.common.entity.StatusCount;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HandoverSessionRepository extends JpaRepository<HandoverSession, Long> {

    @Query("""
            select h from HandoverSession h
            where (:status is null or h.status = :status)
              and (:week is null or h.week = :week)
              and (:category is null or h.category = :category)
            """)
    Page<HandoverSession> search(@Param("status") String status, @Param("week") String week,
                                 @Param("category") String category, Pageable pageable);

    long countByStatus(String status);

    @Query("select h.status as status, count(h) as count from HandoverSession h group by h.status")
    List<StatusCount> countGroupByStatus();
}
