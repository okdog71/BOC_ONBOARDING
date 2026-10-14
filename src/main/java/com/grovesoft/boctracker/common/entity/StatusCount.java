package com.grovesoft.boctracker.common.entity;

/** group-by 집계용 인터페이스 프로젝션(summary API 에서 재사용). */
public interface StatusCount {

    String getStatus();

    long getCount();
}
