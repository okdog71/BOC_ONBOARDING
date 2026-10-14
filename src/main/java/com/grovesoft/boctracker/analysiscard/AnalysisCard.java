package com.grovesoft.boctracker.analysiscard;

import com.grovesoft.boctracker.common.entity.BaseEntity;
import com.grovesoft.boctracker.common.meta.CodeValues;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "analysis_card")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnalysisCard extends BaseEntity {

    @Column(name = "menu_name", nullable = false, length = 255)
    private String menuName;

    @Column(length = 500)
    private String path;

    @Column(length = 100)
    private String domain;

    @Column(name = "backend_service", length = 500)
    private String backendService;

    @Column(length = 20)
    private String generation;

    @Column(length = 1000)
    private String summary;

    @Column(length = 2000)
    private String scenario;

    @Column(length = 1000)
    private String input;

    @Column(length = 1000)
    private String output;

    @Column(name = "backend_mapping", length = 1000)
    private String backendMapping;

    @Column(name = "data_store", length = 500)
    private String dataStore;

    @Column(name = "external_link", length = 500)
    private String externalLink;

    @Column(length = 500)
    private String permission;

    @Column(name = "related_process", length = 500)
    private String relatedProcess;

    @Column(name = "known_issue", length = 500)
    private String knownIssue;

    @Column(name = "dev_check", length = 20)
    private String devCheck;

    @Column(length = 1000)
    private String risk;

    @Column(length = 100)
    private String owner;

    @Column(name = "analyzed_date")
    private LocalDate analyzedDate;

    @Column(nullable = false, length = 20)
    private String status;

    @Builder
    private AnalysisCard(String menuName, String path, String domain, String backendService,
                         String generation, String summary, String scenario, String input,
                         String output, String backendMapping, String dataStore, String externalLink,
                         String permission, String relatedProcess, String knownIssue, String devCheck,
                         String risk, String owner, LocalDate analyzedDate, String status) {
        this.menuName = menuName;
        this.path = path;
        this.domain = domain;
        this.backendService = backendService;
        this.generation = generation;
        this.summary = summary;
        this.scenario = scenario;
        this.input = input;
        this.output = output;
        this.backendMapping = backendMapping;
        this.dataStore = dataStore;
        this.externalLink = externalLink;
        this.permission = permission;
        this.relatedProcess = relatedProcess;
        this.knownIssue = knownIssue;
        this.devCheck = devCheck;
        this.risk = risk;
        this.owner = owner;
        this.analyzedDate = analyzedDate;
        this.status = status != null ? status : CodeValues.DEFAULT_STATUS;
    }

    /** 전체 필드 교체(PUT). source 는 Mapper 가 Request 로부터 만든 비영속 객체. */
    public void update(AnalysisCard source) {
        this.menuName = source.menuName;
        this.path = source.path;
        this.domain = source.domain;
        this.backendService = source.backendService;
        this.generation = source.generation;
        this.summary = source.summary;
        this.scenario = source.scenario;
        this.input = source.input;
        this.output = source.output;
        this.backendMapping = source.backendMapping;
        this.dataStore = source.dataStore;
        this.externalLink = source.externalLink;
        this.permission = source.permission;
        this.relatedProcess = source.relatedProcess;
        this.knownIssue = source.knownIssue;
        this.devCheck = source.devCheck;
        this.risk = source.risk;
        this.owner = source.owner;
        this.analyzedDate = source.analyzedDate;
        this.status = source.status;
    }

    public void changeStatus(String status) {
        this.status = status;
    }
}
