package com.grovesoft.boctracker.auditlog;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLogEntry, Long> {

    @Query("""
            select a from AuditLogEntry a
            where (:domain is null or a.domain = :domain)
              and (:actor is null or a.actor = :actor)
              and (:action is null or a.action = :action)
              and (:code is null or a.targetCode = :code)
              and (:ip is null or a.clientIp = :ip)
              and a.actedAt >= :from and a.actedAt < :to
              and (:q is null or lower(a.targetLabel) like :q or lower(a.targetCode) like :q
                   or lower(a.changeDetail) like :q)
            """)
    Page<AuditLogEntry> search(@Param("domain") String domain, @Param("actor") String actor,
            @Param("action") String action, @Param("code") String code, @Param("ip") String ip,
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to, @Param("q") String q, Pageable pageable);
}
