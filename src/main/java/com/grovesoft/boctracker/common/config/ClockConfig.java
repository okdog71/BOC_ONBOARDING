package com.grovesoft.boctracker.common.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 날짜 판단(지연·완료일)용 시계. 운영 기준 타임존은 서울. 테스트에서는 이 빈을 대체해 고정할 수 있다. */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("Asia/Seoul"));
    }
}
