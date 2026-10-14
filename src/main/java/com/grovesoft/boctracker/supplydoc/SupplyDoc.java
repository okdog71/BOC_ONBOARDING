package com.grovesoft.boctracker.supplydoc;

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
@Table(name = "supply_doc")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SupplyDoc extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "expected_content", length = 1000)
    private String expectedContent;

    @Column(name = "usage_purpose", length = 1000)
    private String usagePurpose;

    @Column(length = 20)
    private String existence;

    @Column(length = 500)
    private String location;

    @Column(length = 20)
    private String freshness;

    @Column(length = 100)
    private String owner;

    @Column(name = "supply_status", nullable = false, length = 20)
    private String supplyStatus;

    @Column(length = 1000)
    private String note;

    @Builder
    private SupplyDoc(String category, String name, String expectedContent, String usagePurpose,
                      String existence, String location, String freshness, String owner,
                      String supplyStatus, String note) {
        this.category = category;
        this.name = name;
        this.expectedContent = expectedContent;
        this.usagePurpose = usagePurpose;
        this.existence = existence;
        this.location = location;
        this.freshness = freshness;
        this.owner = owner;
        this.supplyStatus = supplyStatus != null ? supplyStatus : CodeValues.DEFAULT_SUPPLY_STATUS;
        this.note = note;
    }

    /** 전체 필드 교체(PUT). source 는 Mapper 가 Request 로부터 만든 비영속 객체. */
    public void update(SupplyDoc source) {
        this.category = source.category;
        this.name = source.name;
        this.expectedContent = source.expectedContent;
        this.usagePurpose = source.usagePurpose;
        this.existence = source.existence;
        this.location = source.location;
        this.freshness = source.freshness;
        this.owner = source.owner;
        this.supplyStatus = source.supplyStatus;
        this.note = source.note;
    }

    public void changeStatus(String status) {
        this.supplyStatus = status;
    }
}
