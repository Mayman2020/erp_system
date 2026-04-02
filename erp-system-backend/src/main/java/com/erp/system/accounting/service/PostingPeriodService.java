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

@Service
@RequiredArgsConstructor
public class PostingPeriodService {

    private final FiscalYearRepository fiscalYearRepository;
    private final FiscalPeriodRepository fiscalPeriodRepository;

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

        FiscalPeriod openPeriod = fiscalPeriodRepository
                .findFirstByStartDateLessThanEqualAndEndDateGreaterThanEqual(postingDate, postingDate)
                .orElse(null);

        if (openPeriod == null) {
            throw new BusinessException("No fiscal period covers the posting date " + postingDate + ". Create a fiscal period first.");
        }
        if (!openPeriod.isOpen()) {
            throw new BusinessException("Posting is not allowed in a closed fiscal period");
        }
    }
}
