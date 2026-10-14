package com.grovesoft.boctracker.megaprocess;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MegaProcessRepository extends JpaRepository<MegaProcess, Long> {

    @Query("""
            select e from MegaProcess e
            where (:l0 is null or e.l0 = :l0)
            """)
    Page<MegaProcess> search(@Param("l0") String l0, Pageable pageable);
}
