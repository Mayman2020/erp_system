package com.erp.system.accounting.repository;

import com.erp.system.accounting.domain.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    @Query("""
            select er from ExchangeRate er
            where er.sourceCurrency = :source
              and er.targetCurrency = :target
              and er.effectiveDate <= :date
              and (er.expiryDate is null or er.expiryDate >= :date)
            order by er.effectiveDate desc
            """)
    List<ExchangeRate> findApplicableRates(@Param("source") String source,
                                           @Param("target") String target,
                                           @Param("date") LocalDate date);

    default Optional<ExchangeRate> findRate(String source, String target, LocalDate date) {
        List<ExchangeRate> rates = findApplicableRates(source, target, date);
        return rates.isEmpty() ? Optional.empty() : Optional.of(rates.get(0));
    }

    List<ExchangeRate> findAllByOrderByEffectiveDateDesc();
}
