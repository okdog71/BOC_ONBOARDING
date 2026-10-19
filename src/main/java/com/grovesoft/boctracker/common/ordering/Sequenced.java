package com.grovesoft.boctracker.common.ordering;

/**
 * 그룹(단계/구분/주차) 안에서 순서(No.)를 갖고, 그룹 접두 기반의 불변 코드를 갖는 엔티티.
 * 코드는 식별자(최초 부여 후 불변), 순서는 표시용(드래그로 바뀜) — 서로 독립이다.
 */
public interface Sequenced {

    Long getId();

    String getCode();

    int getSortOrder();

    /** 이 엔티티가 속한 그룹 값(예: category, week) */
    String groupValue();

    void assignCode(String code);

    void changeSortOrder(int sortOrder);

    void changeGroup(String group);
}
