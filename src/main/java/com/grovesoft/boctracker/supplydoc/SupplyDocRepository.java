package com.grovesoft.boctracker.supplydoc;

import com.grovesoft.boctracker.common.entity.StatusCount;
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
            """)
    Page<SupplyDoc> search(@Param("supplyStatus") String supplyStatus, @Param("category") String category, Pageable pageable);

    long countBySupplyStatus(String supplyStatus);

    @Query("select e.supplyStatus as status, count(e) as count from SupplyDoc e group by e.supplyStatus")
    List<StatusCount> countGroupByStatus();
}
