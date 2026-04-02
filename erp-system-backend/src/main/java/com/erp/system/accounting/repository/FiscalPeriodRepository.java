package com.erp.system.accounting.repository;

import com.erp.system.accounting.domain.FiscalPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FiscalPeriodRepository extends JpaRepository<FiscalPeriod, Long> {

    List<FiscalPeriod> findAllByOrderByStartDateDesc();

    List<FiscalPeriod> findByFiscalYearIdOrderByStartDateAsc(Long fiscalYearId);

    Optional<FiscalPeriod> findFirstByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate date, LocalDate sameDate);
}
