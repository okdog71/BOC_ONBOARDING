package com.grovesoft.boctracker.common.entity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 응답 DTO 를 만들기 직전에 변경 내용을 flush 한다.
 * 감사 값(수정 시각·수정자)은 flush 때 채워지므로, flush 전에 응답을 만들면 방금 수정한 값이 아닌 이전 값이 나간다.
 * 쓰기 트랜잭션 안에서만 동작하고, 변경이 없으면 flush 는 아무 일도 하지 않는다.
 */
@Aspect
@Component
public class AuditFlushAspect {

    @PersistenceContext
    private EntityManager em;

    @Before("execution(* com.grovesoft.boctracker..*Mapper.toResponse(..))")
    public void flushBeforeMapping() {
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && !TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
            em.flush();
        }
    }
}
