package com.grovesoft.boctracker.access;

import com.grovesoft.boctracker.common.entity.StatusCount;
import java.time.LocalDate;
import java.util.Collection;
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
              and (:owner is null or e.owner = :owner)
              and (:q is null
                   or lower(e.code) like lower(concat('%', :q, '%'))
                   or lower(e.target) like lower(concat('%', :q, '%'))
                   or lower(e.usage) like lower(concat('%', :q, '%')))
              and (:overdueOnly = false
                   or (e.targetDate is not null and e.targetDate < :today and e.status <> '승인'))
            """)
    Page<AccessItem> search(@Param("status") String status, @Param("category") String category,
                            @Param("owner") String owner, @Param("q") String q,
                            @Param("overdueOnly") boolean overdueOnly, @Param("today") LocalDate today,
                            Pageable pageable);

    boolean existsByCode(String code);

    long countByStatus(String status);

    @Query("select e.status as status, count(e) as count from AccessItem e group by e.status")
    List<StatusCount> countGroupByStatus();

    /** 접근 전제(용도에 '접근 전제' 포함) 항목 중 아직 승인되지 않은 것의 수 */
    @Query("select count(e) from AccessItem e where e.usage like '%접근 전제%' and e.status <> '승인'")
    long countPrerequisitesNotApproved();

    List<AccessItem> findByCodeIn(Collection<String> codes);
}
