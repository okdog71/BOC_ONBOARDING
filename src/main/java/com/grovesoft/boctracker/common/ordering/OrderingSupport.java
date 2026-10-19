package com.grovesoft.boctracker.common.ordering;

import com.grovesoft.boctracker.common.web.BusinessRuleException;
import com.grovesoft.boctracker.common.web.NotFoundException;
import jakarta.persistence.EntityManager;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 그룹 내 순서(No.)·코드 자동 부여·재정렬을 JPQL 로 처리하는 공통 부품.
 * 엔티티 이름과 그룹 속성명은 서비스가 상수로 넘기므로(사용자 입력 아님) 쿼리 조립이 안전하다.
 */
public class OrderingSupport<E extends Sequenced> {

    private final EntityManager em;
    private final Class<E> type;
    private final String entityName;
    private final String groupAttr;

    public OrderingSupport(EntityManager em, Class<E> type, String groupAttr) {
        this.em = em;
        this.type = type;
        this.entityName = type.getSimpleName();
        this.groupAttr = groupAttr;
    }

    /** 그룹 구성원을 No. 순으로 */
    public List<E> members(String group) {
        return em.createQuery("select e from " + entityName + " e where e." + groupAttr
                        + " = :g order by e.sortOrder, e.id", type)
                .setParameter("g", group).getResultList();
    }

    /** 그룹의 다음 No.(맨 뒤) */
    public int nextSortOrder(String group) {
        Integer max = em.createQuery("select max(e.sortOrder) from " + entityName + " e where e." + groupAttr
                + " = :g", Integer.class).setParameter("g", group).getSingleResult();
        return (max == null ? 0 : max) + 1;
    }

    /** {key}.{같은 key 코드의 최대 순번 + 1}. 삭제된 번호는 재사용하지 않는다(최대값 기준). */
    public String nextCode(String key) {
        List<String> codes = em.createQuery("select e.code from " + entityName + " e where e.code like :p",
                String.class).setParameter("p", key + ".%").getResultList();
        int max = codes.stream().map(c -> c.substring(key.length() + 1))
                .filter(s -> !s.isEmpty() && s.chars().allMatch(Character::isDigit))
                .mapToInt(Integer::parseInt).max().orElse(0);
        return key + "." + (max + 1);
    }

    /** 그룹 전체 id 를 새 순서로 받아 No. 를 다시 매긴다. 코드는 바뀌지 않는다. */
    public List<E> reorder(String group, List<Long> ids) {
        List<E> current = members(group);
        if (current.isEmpty()) {
            throw new NotFoundException("Group", group);
        }
        Set<Long> expected = current.stream().map(Sequenced::getId).collect(Collectors.toSet());
        if (ids.size() != expected.size() || !expected.equals(new HashSet<>(ids))) {
            throw new BusinessRuleException("순서 목록이 해당 그룹의 항목과 일치하지 않습니다. 목록을 새로 고친 뒤 다시 시도하세요.");
        }
        Map<Long, E> byId = current.stream().collect(Collectors.toMap(Sequenced::getId, Function.identity()));
        for (int i = 0; i < ids.size(); i++) {
            byId.get(ids.get(i)).changeSortOrder(i + 1);
        }
        return ids.stream().map(byId::get).toList();
    }

    /** 그룹 내 No. 를 1..n 으로 촘촘히(기존 순서 유지) */
    public void resequence(String group) {
        List<E> rows = members(group);
        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).changeSortOrder(i + 1);
        }
    }

    /** 그룹명 일괄 변경. 이미 있는 이름이면 병합(이동분은 대상 그룹 맨 뒤). 코드는 바뀌지 않는다. */
    public void renameGroup(String from, String to) {
        List<E> moving = members(from);
        if (moving.isEmpty()) {
            throw new NotFoundException("Group", from);
        }
        if (from.equals(to)) {
            return;
        }
        int next = nextSortOrder(to);
        for (E e : moving) {
            e.changeGroup(to);
            e.changeSortOrder(next++);
        }
    }
}
