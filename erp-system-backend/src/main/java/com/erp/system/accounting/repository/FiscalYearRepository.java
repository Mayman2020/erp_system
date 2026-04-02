package com.erp.system.accounting.repository;

import com.erp.system.accounting.domain.FiscalYear;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FiscalYearRepository extends JpaRepository<FiscalYear, Long> {

    List<FiscalYear> findAllByOrderByYearDesc();

    Optional<FiscalYear> findByYear(Integer year);

    Optional<FiscalYear> findFirstByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate date, LocalDate sameDate);
}
