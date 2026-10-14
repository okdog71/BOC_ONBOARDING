package com.grovesoft.boctracker.common.entity;

/** status 외의 컬럼(예: priority)으로 group-by 할 때 쓰는 인터페이스 프로젝션. groupKey 는 null 일 수 있다. */
public interface GroupCount {

    String getGroupKey();

    long getCount();
}
