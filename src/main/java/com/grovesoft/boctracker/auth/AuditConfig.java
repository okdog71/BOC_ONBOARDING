package com.grovesoft.boctracker.auth;

import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

/** JPA Auditing 의 등록자·수정자(@CreatedBy/@LastModifiedBy) 공급원. */
@Configuration
public class AuditConfig {

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> Optional.of(CurrentUser.actor());
    }
}
