package com.grovesoft.boctracker.user;

import com.grovesoft.boctracker.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 시스템 사용자(내부 인원). 비밀번호는 BCrypt 해시만 보관하고 어떤 응답에도 싣지 않는다. */
@Getter
@Entity
@Table(name = "app_user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppUser extends BaseEntity {

    @Column(name = "login_id", nullable = false, length = 30, updatable = false)
    private String loginId;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "must_change_password", nullable = false)
    private boolean mustChangePassword;

    @Column(nullable = false, length = 50)
    private String affiliation;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "join_date")
    private LocalDate joinDate;

    @Column(name = "leave_date")
    private LocalDate leaveDate;

    @Column(name = "krafton_account", length = 100)
    private String kraftonAccount;

    @Column(length = 30)
    private String phone;

    @Column(name = "grove_mail", length = 100)
    private String groveMail;

    @Column(length = 100)
    private String ip;

    @Column(length = 100)
    private String gmail;

    @Column(length = 100)
    private String vpn;

    @Column(name = "boc_account", length = 100)
    private String bocAccount;

    @Column(name = "kec_account", length = 100)
    private String kecAccount;

    @Column(length = 100)
    private String aws;

    @Builder
    private AppUser(String loginId, String passwordHash, boolean mustChangePassword, String affiliation, String name,
            LocalDate joinDate, LocalDate leaveDate, String kraftonAccount, String phone, String groveMail, String ip,
            String gmail, String vpn, String bocAccount, String kecAccount, String aws) {
        this.loginId = loginId;
        this.passwordHash = passwordHash;
        this.mustChangePassword = mustChangePassword;
        this.affiliation = affiliation;
        this.name = name;
        this.joinDate = joinDate;
        this.leaveDate = leaveDate;
        this.kraftonAccount = kraftonAccount;
        this.phone = phone;
        this.groveMail = groveMail;
        this.ip = ip;
        this.gmail = gmail;
        this.vpn = vpn;
        this.bocAccount = bocAccount;
        this.kecAccount = kecAccount;
        this.aws = aws;
    }

    /** 프로필 갱신(로그인 ID·비밀번호 제외). */
    public void updateProfile(AppUser src) {
        this.affiliation = src.affiliation;
        this.name = src.name;
        this.joinDate = src.joinDate;
        this.leaveDate = src.leaveDate;
        this.kraftonAccount = src.kraftonAccount;
        this.phone = src.phone;
        this.groveMail = src.groveMail;
        this.ip = src.ip;
        this.gmail = src.gmail;
        this.vpn = src.vpn;
        this.bocAccount = src.bocAccount;
        this.kecAccount = src.kecAccount;
        this.aws = src.aws;
    }

    /** 본인이 바꾼 비밀번호(초기 비밀번호 변경 안내 해제). */
    public void changePassword(String hash) {
        this.passwordHash = hash;
        this.mustChangePassword = false;
    }

    /** 초기 비밀번호(=로그인 ID)로 되돌림 — 다음 로그인 후 변경을 안내한다. */
    public void resetPassword(String hash) {
        this.passwordHash = hash;
        this.mustChangePassword = true;
    }
}
