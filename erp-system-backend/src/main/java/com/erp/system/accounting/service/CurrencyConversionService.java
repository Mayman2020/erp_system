package com.erp.system.accounting.service;

import com.erp.system.accounting.domain.ExchangeRate;
import com.erp.system.accounting.dto.display.ExchangeRateDisplayDto;
import com.erp.system.accounting.dto.form.ExchangeRateFormDto;
import com.erp.system.accounting.repository.ExchangeRateRepository;
import com.erp.system.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CurrencyConversionService {

    private final ExchangeRateRepository exchangeRateRepository;

    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency, LocalDate date) {
        if (fromCurrency.equalsIgnoreCase(toCurrency)) {
            return amount;
        }
        ExchangeRate rate = exchangeRateRepository.findRate(fromCurrency.toUpperCase(), toCurrency.toUpperCase(), date)
                .orElse(null);
        if (rate != null) {
            return amount.multiply(rate.getRate()).setScale(2, RoundingMode.HALF_UP);
        }
        ExchangeRate inverseRate = exchangeRateRepository.findRate(toCurrency.toUpperCase(), fromCurrency.toUpperCase(), date)
                .orElse(null);
        if (inverseRate != null && inverseRate.getRate().compareTo(BigDecimal.ZERO) > 0) {
            return amount.divide(inverseRate.getRate(), 2, RoundingMode.HALF_UP);
        }
        throw new BusinessException("No exchange rate found for " + fromCurrency + " → " + toCurrency + " on " + date);
    }

    public BigDecimal getRate(String fromCurrency, String toCurrency, LocalDate date) {
        if (fromCurrency.equalsIgnoreCase(toCurrency)) {
            return BigDecimal.ONE;
        }
        return exchangeRateRepository.findRate(fromCurrency.toUpperCase(), toCurrency.toUpperCase(), date)
                .map(ExchangeRate::getRate)
                .orElseGet(() -> exchangeRateRepository.findRate(toCurrency.toUpperCase(), fromCurrency.toUpperCase(), date)
                        .map(er -> BigDecimal.ONE.divide(er.getRate(), 6, RoundingMode.HALF_UP))
                        .orElseThrow(() -> new BusinessException("No exchange rate found for " + fromCurrency + " → " + toCurrency)));
    }

    @Transactional(readOnly = true)
    public List<ExchangeRateDisplayDto> getAllRates() {
        return exchangeRateRepository.findAllByOrderByEffectiveDateDesc().stream()
                .map(this::toDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExchangeRateDisplayDto getRateById(Long id) {
        return exchangeRateRepository.findById(id)
                .map(this::toDisplay)
                .orElseThrow(() -> new BusinessException("Exchange rate not found: " + id));
    }

    @Transactional
    public ExchangeRateDisplayDto createRate(ExchangeRateFormDto form) {
        validateRateForm(form);
        ExchangeRate rate = ExchangeRate.builder()
                .sourceCurrency(form.getSourceCurrency().toUpperCase())
                .targetCurrency(form.getTargetCurrency().toUpperCase())
                .rate(form.getRate())
                .effectiveDate(form.getEffectiveDate())
                .expiryDate(form.getExpiryDate())
                .build();
        return toDisplay(exchangeRateRepository.save(rate));
    }

    @Transactional
    public ExchangeRateDisplayDto updateRate(Long id, ExchangeRateFormDto form) {
        validateRateForm(form);
        ExchangeRate rate = exchangeRateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Exchange rate not found: " + id));
        rate.setSourceCurrency(form.getSourceCurrency().toUpperCase());
        rate.setTargetCurrency(form.getTargetCurrency().toUpperCase());
        rate.setRate(form.getRate());
        rate.setEffectiveDate(form.getEffectiveDate());
        rate.setExpiryDate(form.getExpiryDate());
        return toDisplay(exchangeRateRepository.save(rate));
    }

    @Transactional
    public void deleteRate(Long id) {
        if (!exchangeRateRepository.existsById(id)) {
            throw new BusinessException("Exchange rate not found: " + id);
        }
        exchangeRateRepository.deleteById(id);
    }

    private void validateRateForm(ExchangeRateFormDto form) {
        if (form.getRate() == null || form.getRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Exchange rate must be greater than zero");
        }
        if (form.getSourceCurrency() == null || form.getSourceCurrency().isBlank()) {
            throw new BusinessException("Source currency is required");
        }
        if (form.getTargetCurrency() == null || form.getTargetCurrency().isBlank()) {
            throw new BusinessException("Target currency is required");
        }
        if (form.getSourceCurrency().equalsIgnoreCase(form.getTargetCurrency())) {
            throw new BusinessException("Source and target currency must differ");
        }
    }

    private ExchangeRateDisplayDto toDisplay(ExchangeRate er) {
        return ExchangeRateDisplayDto.builder()
                .id(er.getId())
                .sourceCurrency(er.getSourceCurrency())
                .targetCurrency(er.getTargetCurrency())
                .rate(er.getRate())
                .effectiveDate(er.getEffectiveDate())
                .expiryDate(er.getExpiryDate())
                .build();
    }
}
