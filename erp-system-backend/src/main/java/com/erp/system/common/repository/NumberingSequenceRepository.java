package com.erp.system.common.repository;

import com.erp.system.common.entity.NumberingSequence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface NumberingSequenceRepository extends JpaRepository<NumberingSequence, Long> {

    Optional<NumberingSequence> findBySequenceName(String sequenceName);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM NumberingSequence s WHERE s.sequenceName = :sequenceName")
    Optional<NumberingSequence> findBySequenceNameForUpdate(@Param("sequenceName") String sequenceName);
}