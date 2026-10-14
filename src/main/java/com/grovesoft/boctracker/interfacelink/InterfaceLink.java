package com.grovesoft.boctracker.interfacelink;

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
@Table(name = "interface_link")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterfaceLink extends BaseEntity {

    @Column(name = "system_name", nullable = false, length = 255)
    private String systemName;

    @Column(length = 50)
    private String direction;

    @Column(name = "interface_name", length = 255)
    private String interfaceName;

    @Column(name = "usage_desc", length = 1000)
    private String usage;

    @Column(length = 255)
    private String auth;

    @Column(name = "data_desc", length = 1000)
    private String data;

    @Column(name = "dev_check", length = 20)
    private String devCheck;

    @Column(length = 1000)
    private String note;

    @Builder
    private InterfaceLink(String systemName, String direction, String interfaceName, String usage,
                          String auth, String data, String devCheck, String note) {
        this.systemName = systemName;
        this.direction = direction;
        this.interfaceName = interfaceName;
        this.usage = usage;
        this.auth = auth;
        this.data = data;
        this.devCheck = devCheck;
        this.note = note;
    }

    /** 전체 필드 교체(PUT). source 는 Mapper 가 Request 로부터 만든 비영속 객체. */
    public void update(InterfaceLink source) {
        this.systemName = source.systemName;
        this.direction = source.direction;
        this.interfaceName = source.interfaceName;
        this.usage = source.usage;
        this.auth = source.auth;
        this.data = source.data;
        this.devCheck = source.devCheck;
        this.note = source.note;
    }
}
