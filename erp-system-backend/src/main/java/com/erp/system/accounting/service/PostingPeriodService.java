package com.erp.system.accounting.service;

import com.erp.system.accounting.domain.FiscalPeriod;
import com.erp.system.accounting.domain.FiscalYear;
import com.erp.system.accounting.repository.FiscalPeriodRepository;
import com.erp.system.accounting.repository.FiscalYearRepository;
import com.erp.system.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostingPeriodService {

    private final FiscalYearRepository fiscalYearRepository;
    private final FiscalPeriodRepository fiscalPeriodRepository;

    /**
     * Posting is governed by the fiscal year only. If a fiscal period exists that covers the date
     * and is closed, posting is blocked (freeze). If no period covers the date, posting is still allowed.
     */
    @Transactional(readOnly = true)
    public void validatePostingDate(LocalDate postingDate) {
        FiscalYear fiscalYear = fiscalYearRepository
                .findFirstByStartDateLessThanEqualAndEndDateGreaterThanEqual(postingDate, postingDate)
                .orElse(null);

        if (fiscalYear == null) {
            throw new BusinessException("No fiscal year covers the posting date " + postingDate + ". Create a fiscal year first.");
        }
        if (!fiscalYear.isOpen()) {
            throw new BusinessException("Posting is not allowed in a closed fiscal year");
        }

        Optional<FiscalPeriod> covering = fiscalPeriodRepository
                .findFirstByStartDateLessThanEqualAndEndDateGreaterThanEqual(postingDate, postingDate);

        if (covering.isPresent() && !covering.get().isOpen()) {
            throw new BusinessException("Posting is not allowed in a closed fiscal period");
        }
    }
}
