package com.grovesoft.boctracker.supplydoc;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplyDocFileRepository extends JpaRepository<SupplyDocFile, Long> {

    List<SupplyDocFile> findBySupplyDocIdOrderByIdAsc(Long supplyDocId);

    List<SupplyDocFile> findBySupplyDocIdInOrderByIdAsc(Collection<Long> supplyDocIds);

    long countBySupplyDocId(Long supplyDocId);

    boolean existsBySupplyDocIdAndSha256(Long supplyDocId, String sha256);
}
