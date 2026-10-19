package com.grovesoft.boctracker.handoverquestion;

import com.grovesoft.boctracker.common.entity.GroupCount;
import com.grovesoft.boctracker.common.entity.StatusCount;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HandoverQuestionRepository extends JpaRepository<HandoverQuestion, Long> {

    @Query("""
            select e from HandoverQuestion e
            where (:status is null or e.status = :status)
              and (:priority is null or e.priority = :priority)
              and (:kind is null or e.kind = :kind)
              and (:askTo is null or lower(e.askTo) like lower(concat('%', :askTo, '%')))
              and (:q is null
                   or lower(e.code) like lower(concat('%', :q, '%'))
                   or lower(e.question) like lower(concat('%', :q, '%'))
                   or lower(e.relatedNode) like lower(concat('%', :q, '%'))
                   or lower(e.answer) like lower(concat('%', :q, '%')))
            """)
    Page<HandoverQuestion> search(@Param("status") String status, @Param("priority") String priority,
                                  @Param("kind") String kind, @Param("askTo") String askTo, @Param("q") String q,
                                  Pageable pageable);

    boolean existsByCode(String code);

    @Query("select e.status as status, count(e) as count from HandoverQuestion e group by e.status")
    List<StatusCount> countGroupByStatus();

    @Query("select e.priority as groupKey, count(e) as count from HandoverQuestion e group by e.priority")
    List<GroupCount> countGroupByPriority();
}
