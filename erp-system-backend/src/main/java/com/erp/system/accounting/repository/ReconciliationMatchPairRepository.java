package com.erp.system.accounting.repository;

import com.erp.system.accounting.domain.ReconciliationMatchPair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReconciliationMatchPairRepository extends JpaRepository<ReconciliationMatchPair, Long> {

    List<ReconciliationMatchPair> findByReconciliationIdOrderByMatchedAtDesc(Long reconciliationId);

    List<ReconciliationMatchPair> findByReconciliationIdAndActiveTrueOrderByMatchedAtDesc(Long reconciliationId);

    @Query("""
            select mp from ReconciliationMatchPair mp
            where mp.active = true
              and (mp.statementLine.id = :lineId or mp.systemLine.id = :lineId)
            order by mp.matchedAt desc
            """)
    List<ReconciliationMatchPair> findActiveByLineId(@Param("lineId") Long lineId);

    @Query("""
            select mp from ReconciliationMatchPair mp
            where (mp.statementLine.id = :lineId or mp.systemLine.id = :lineId)
            order by mp.matchedAt desc
            """)
    List<ReconciliationMatchPair> findAllByLineId(@Param("lineId") Long lineId);
}
