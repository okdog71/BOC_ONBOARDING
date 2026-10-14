package com.grovesoft.boctracker.docreliability;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocReliabilityRepository extends JpaRepository<DocReliability, Long> {

    @Query("""
            select e from DocReliability e
            where (:grade is null or e.grade = :grade)
              and (:freshness is null or e.freshness = :freshness)
            """)
    Page<DocReliability> search(@Param("grade") String grade, @Param("freshness") String freshness, Pageable pageable);
}
