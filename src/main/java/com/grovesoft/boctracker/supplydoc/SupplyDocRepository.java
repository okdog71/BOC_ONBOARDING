package com.grovesoft.boctracker.supplydoc;

import com.grovesoft.boctracker.common.entity.StatusCount;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SupplyDocRepository extends JpaRepository<SupplyDoc, Long> {

    @Query("""
            select e from SupplyDoc e
            where (:supplyStatus is null or e.supplyStatus = :supplyStatus)
              and (:category is null or e.category = :category)
              and (:owner is null or e.owner = :owner)
              and (:q is null
                   or lower(e.code) like lower(concat('%', :q, '%'))
                   or lower(e.name) like lower(concat('%', :q, '%'))
                   or lower(e.usagePurpose) like lower(concat('%', :q, '%')))
              and (:overdueOnly = false
                   or (e.targetDate is not null and e.targetDate < :today
                       and e.supplyStatus not in ('수령', '해당없음')))
            """)
    Page<SupplyDoc> search(@Param("supplyStatus") String supplyStatus, @Param("category") String category,
                           @Param("owner") String owner, @Param("q") String q,
                           @Param("overdueOnly") boolean overdueOnly, @Param("today") LocalDate today,
                           Pageable pageable);

    boolean existsByCode(String code);

    long countBySupplyStatus(String supplyStatus);

    @Query("select e.supplyStatus as status, count(e) as count from SupplyDoc e group by e.supplyStatus")
    List<StatusCount> countGroupByStatus();

    @Query("select count(e) from SupplyDoc e where e.supplyStatus in ('수령', '해당없음')")
    long countReady();

    List<SupplyDoc> findByCodeIn(java.util.Collection<String> codes);
}
