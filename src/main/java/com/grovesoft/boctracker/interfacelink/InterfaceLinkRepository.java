package com.grovesoft.boctracker.interfacelink;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InterfaceLinkRepository extends JpaRepository<InterfaceLink, Long> {

    @Query("""
            select e from InterfaceLink e
            where (:devCheck is null or e.devCheck = :devCheck)
            """)
    Page<InterfaceLink> search(@Param("devCheck") String devCheck, Pageable pageable);
}
