package com.grovesoft.boctracker.user;

import com.grovesoft.boctracker.user.dto.UserRequest;
import com.grovesoft.boctracker.user.dto.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    /** 프로필 값만 담은 엔티티(비밀번호는 서비스가 채운다). */
    public AppUser toEntity(UserRequest r, String passwordHash) {
        return AppUser.builder()
                .loginId(r.loginId().trim())
                .passwordHash(passwordHash)
                .mustChangePassword(true)
                .affiliation(r.affiliation().trim())
                .name(r.name().trim())
                .joinDate(r.joinDate())
                .leaveDate(r.leaveDate())
                .kraftonAccount(blankToNull(r.kraftonAccount()))
                .phone(blankToNull(r.phone()))
                .groveMail(blankToNull(r.groveMail()))
                .ip(blankToNull(r.ip()))
                .gmail(blankToNull(r.gmail()))
                .vpn(blankToNull(r.vpn()))
                .bocAccount(blankToNull(r.bocAccount()))
                .kecAccount(blankToNull(r.kecAccount()))
                .aws(blankToNull(r.aws()))
                .build();
    }

    public UserResponse toResponse(AppUser e) {
        return new UserResponse(e.getId(), e.getLoginId(), e.getAffiliation(), e.getName(), e.getJoinDate(),
                e.getLeaveDate(), e.getKraftonAccount(), e.getPhone(), e.getGroveMail(), e.getIp(), e.getGmail(),
                e.getVpn(), e.getBocAccount(), e.getKecAccount(), e.getAws(), e.isMustChangePassword(),
                e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy());
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
