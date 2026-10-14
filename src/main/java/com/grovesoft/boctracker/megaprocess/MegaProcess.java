package com.grovesoft.boctracker.megaprocess;

import com.grovesoft.boctracker.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "mega_process")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MegaProcess extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String l0;

    @Column(nullable = false, length = 255)
    private String l1;

    @Column(length = 255)
    private String l2;

    @Column(length = 255)
    private String l3;

    @Column(name = "related_menu", length = 1000)
    private String relatedMenu;

    @Column(name = "related_service", length = 1000)
    private String relatedService;

    @Column(name = "external_link", length = 1000)
    private String externalLink;

    @Column(name = "output_data", length = 1000)
    private String outputData;

    @Column(length = 1000)
    private String note;

    @Builder
    private MegaProcess(String l0, String l1, String l2, String l3,
                        String relatedMenu, String relatedService, String externalLink, String outputData,
                        String note) {
        this.l0 = l0;
        this.l1 = l1;
        this.l2 = l2;
        this.l3 = l3;
        this.relatedMenu = relatedMenu;
        this.relatedService = relatedService;
        this.externalLink = externalLink;
        this.outputData = outputData;
        this.note = note;
    }

    /** 전체 필드 교체(PUT). source 는 Mapper 가 Request 로부터 만든 비영속 객체. */
    public void update(MegaProcess source) {
        this.l0 = source.l0;
        this.l1 = source.l1;
        this.l2 = source.l2;
        this.l3 = source.l3;
        this.relatedMenu = source.relatedMenu;
        this.relatedService = source.relatedService;
        this.externalLink = source.externalLink;
        this.outputData = source.outputData;
        this.note = source.note;
    }
}
