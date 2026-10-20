package com.grovesoft.boctracker.auditlog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

/** 변경 이력 조회용(쓰기는 AuditEventListener 가 JDBC 로 직접 한다). */
@Getter
@Entity
@Immutable
@Table(name = "audit_log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditLogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "acted_at", nullable = false)
    private LocalDateTime actedAt;

    @Column(nullable = false, length = 50)
    private String actor;

    @Column(name = "actor_name", length = 50)
    private String actorName;

    @Column(nullable = false, length = 10)
    private String action;

    @Column(length = 40)
    private String domain;

    @Column(name = "entity_type", nullable = false, length = 60)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "target_code", length = 100)
    private String targetCode;

    @Column(name = "target_label", length = 255)
    private String targetLabel;

    @Column(name = "change_detail", length = 4000)
    private String changeDetail;

    @Column(name = "client_ip", length = 45)
    private String clientIp;

    @Column(name = "request_desc", length = 255)
    private String requestDesc;
}
