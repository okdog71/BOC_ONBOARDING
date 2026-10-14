package com.grovesoft.boctracker.menu;

import com.grovesoft.boctracker.common.entity.BaseEntity;
import com.grovesoft.boctracker.common.meta.CodeValues;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "menu_inventory")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuInventory extends BaseEntity {

    @Column(length = 100)
    private String gnb;

    @Column(nullable = false, length = 255)
    private String menu;

    @Column(length = 255)
    private String submenu;

    @Column(length = 500)
    private String path;

    @Column(length = 100)
    private String domain;

    @Column(name = "backend_service", length = 500)
    private String backendService;

    @Column(name = "data_store", length = 500)
    private String dataStore;

    @Column(name = "external_link", length = 500)
    private String externalLink;

    @Column(length = 500)
    private String permission;

    @Column(length = 20)
    private String generation;

    @Column(length = 10)
    private String priority;

    @Column(name = "known_issue", length = 500)
    private String knownIssue;

    @Column(name = "dev_check", length = 20)
    private String devCheck;

    @Column(length = 100)
    private String owner;

    @Column(name = "analysis_status", nullable = false, length = 20)
    private String analysisStatus;

    @Column(length = 1000)
    private String note;

    @Builder
    private MenuInventory(String gnb, String menu, String submenu, String path,
                          String domain, String backendService, String dataStore, String externalLink,
                          String permission, String generation, String priority, String knownIssue,
                          String devCheck, String owner, String analysisStatus, String note) {
        this.gnb = gnb;
        this.menu = menu;
        this.submenu = submenu;
        this.path = path;
        this.domain = domain;
        this.backendService = backendService;
        this.dataStore = dataStore;
        this.externalLink = externalLink;
        this.permission = permission;
        this.generation = generation;
        this.priority = priority;
        this.knownIssue = knownIssue;
        this.devCheck = devCheck;
        this.owner = owner;
        this.analysisStatus = analysisStatus != null ? analysisStatus : CodeValues.DEFAULT_STATUS;
        this.note = note;
    }

    /** 전체 필드 교체(PUT). source 는 Mapper 가 Request 로부터 만든 비영속 객체. */
    public void update(MenuInventory source) {
        this.gnb = source.gnb;
        this.menu = source.menu;
        this.submenu = source.submenu;
        this.path = source.path;
        this.domain = source.domain;
        this.backendService = source.backendService;
        this.dataStore = source.dataStore;
        this.externalLink = source.externalLink;
        this.permission = source.permission;
        this.generation = source.generation;
        this.priority = source.priority;
        this.knownIssue = source.knownIssue;
        this.devCheck = source.devCheck;
        this.owner = source.owner;
        this.analysisStatus = source.analysisStatus;
        this.note = source.note;
    }

    public void changeStatus(String status) {
        this.analysisStatus = status;
    }
}
