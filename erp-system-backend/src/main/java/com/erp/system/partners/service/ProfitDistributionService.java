package com.erp.system.partners.service;

import com.erp.system.accounting.domain.Account;
import com.erp.system.accounting.domain.JournalEntry;
import com.erp.system.accounting.repository.AccountRepository;
import com.erp.system.accounting.service.AccountingPostingService;
import com.erp.system.accounting.service.AccountingReportService;
import com.erp.system.accounting.support.JournalPostingNarratives;
import com.erp.system.common.enums.TransactionStatus;
import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.common.service.NumberingService;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.partners.domain.Partner;
import com.erp.system.partners.domain.ProfitDistribution;
import com.erp.system.partners.domain.ProfitDistributionLine;
import com.erp.system.partners.dto.display.ProfitDistributionDisplayDto;
import com.erp.system.partners.dto.display.ProfitDistributionLineDisplayDto;
import com.erp.system.partners.dto.form.ProfitDistributionFormDto;
import com.erp.system.partners.repository.PartnerRepository;
import com.erp.system.partners.repository.ProfitDistributionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfitDistributionService {

    private static final String MODULE = "PARTNERS";

    private final ProfitDistributionRepository profitDistributionRepository;
    private final PartnerRepository partnerRepository;
    private final AccountRepository accountRepository;
    private final AccountingReportService accountingReportService;
    private final ObjectProvider<AccountingPostingService> accountingPostingServiceProvider;
    private final NumberingService numberingService;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<ProfitDistributionDisplayDto> getAll() {
        return profitDistributionRepository.findAllByOrderByIdDesc().stream().map(this::toDisplay).toList();
    }

    @Transactional(readOnly = true)
    public ProfitDistributionDisplayDto getById(Long id) {
        return toDisplay(loadDistribution(id));
    }

    @Transactional
    public ProfitDistributionDisplayDto create(ProfitDistributionFormDto request) {
        BigDecimal totalProfit = resolveTotalProfit(request);
        if (totalProfit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Total profit must be greater than zero");
        }

        ProfitDistribution distribution = ProfitDistribution.builder()
                .distributionNo(resolveDistributionNo(request.getDistributionNo()))
                .periodLabel(request.getPeriodLabel().trim())
                .totalProfit(totalProfit.setScale(2, RoundingMode.HALF_UP))
                .status(TransactionStatus.DRAFT)
                .lines(new ArrayList<>())
                .build();
        distribution.getLines().addAll(buildLines(distribution, totalProfit));
        distribution = profitDistributionRepository.save(distribution);
        activityLogService.log(MODULE, "CREATE", "ProfitDistribution", distribution.getId(),
                distribution.getDistributionNo(), "Created profit distribution " + distribution.getDistributionNo());
        return toDisplay(distribution);
    }

    @Transactional
    public ProfitDistributionDisplayDto update(Long id, ProfitDistributionFormDto request) {
        ProfitDistribution distribution = loadDistribution(id);
        if (distribution.getStatus() != TransactionStatus.DRAFT) {
            throw new BusinessException("Only draft distributions can be edited");
        }
        BigDecimal totalProfit = resolveTotalProfit(request);
        if (totalProfit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Total profit must be greater than zero");
        }

        distribution.setPeriodLabel(request.getPeriodLabel().trim());
        distribution.setTotalProfit(totalProfit.setScale(2, RoundingMode.HALF_UP));
        distribution.getLines().clear();
        distribution.getLines().addAll(buildLines(distribution, totalProfit));
        distribution = profitDistributionRepository.save(distribution);
        activityLogService.log(MODULE, "UPDATE", "ProfitDistribution", distribution.getId(),
                distribution.getDistributionNo(), "Updated profit distribution " + distribution.getDistributionNo());
        return toDisplay(distribution);
    }

    @Transactional
    public ProfitDistributionDisplayDto approve(Long id, String actor) {
        ProfitDistribution distribution = loadDistribution(id);
        if (distribution.getStatus() == TransactionStatus.CANCELLED) {
            throw new BusinessException("Cancelled distribution cannot be approved");
        }
        if (distribution.getStatus() == TransactionStatus.APPROVED) {
            return toDisplay(distribution);
        }
        if (distribution.getLines().isEmpty()) {
            throw new BusinessException("Distribution has no partner lines");
        }

        AccountingPostingService postingService = accountingPostingServiceProvider.getIfAvailable();
        if (postingService != null) {
            Account retainedEarnings = accountRepository.findByCode("3200")
                    .or(() -> accountRepository.findByCode("3120"))
                    .orElseThrow(() -> new BusinessException("Retained earnings account not found"));
            String narrative = JournalPostingNarratives.entryHeader(
                    distribution.getPeriodLabel(),
                    "Profit distribution",
                    distribution.getDistributionNo()
            );
            List<AccountingPostingService.JournalLineDraft> journalLines = new ArrayList<>();
            journalLines.add(AccountingPostingService.JournalLineDraft.builder()
                    .accountId(retainedEarnings.getId())
                    .description(narrative)
                    .debit(distribution.getTotalProfit())
                    .credit(BigDecimal.ZERO)
                    .build());
            for (ProfitDistributionLine line : distribution.getLines()) {
                Partner partner = partnerRepository.findById(line.getPartnerId())
                        .orElseThrow(() -> new BusinessException("Partner not found: " + line.getPartnerId()));
                if (partner.getCapitalAccountId() == null) {
                    throw new BusinessException("Partner " + partner.getCode() + " has no capital account");
                }
                Account capital = accountRepository.findById(partner.getCapitalAccountId())
                        .orElseThrow(() -> new BusinessException("Capital account not found"));
                journalLines.add(AccountingPostingService.JournalLineDraft.builder()
                        .accountId(capital.getId())
                        .description(JournalPostingNarratives.lineWithAccount(narrative, capital, false))
                        .debit(BigDecimal.ZERO)
                        .credit(line.getAmount())
                        .build());
            }
            JournalEntry journalEntry = postingService.createPostedJournal(
                    LocalDate.now(),
                    narrative,
                    "PROFIT_DISTRIBUTION",
                    distribution.getId(),
                    actor,
                    journalLines
            );
            distribution.setJournalEntryId(journalEntry.getId());
        }

        distribution.setStatus(TransactionStatus.APPROVED);
        distribution.setApprovedAt(LocalDateTime.now());
        distribution = profitDistributionRepository.save(distribution);
        activityLogService.log(MODULE, "APPROVE", "ProfitDistribution", distribution.getId(),
                distribution.getDistributionNo(), "Approved profit distribution " + distribution.getDistributionNo());
        return toDisplay(distribution);
    }

    @Transactional
    public void delete(Long id) {
        ProfitDistribution distribution = loadDistribution(id);
        if (distribution.getStatus() != TransactionStatus.DRAFT) {
            throw new BusinessException("Only draft distributions can be deleted");
        }
        profitDistributionRepository.delete(distribution);
        activityLogService.log(MODULE, "DELETE", "ProfitDistribution", distribution.getId(),
                distribution.getDistributionNo(), "Deleted profit distribution " + distribution.getDistributionNo());
    }

    private BigDecimal resolveTotalProfit(ProfitDistributionFormDto request) {
        if (request.getProfitFromDate() != null && request.getProfitToDate() != null) {
            return accountingReportService.getProfitLoss(request.getProfitFromDate(), request.getProfitToDate())
                    .getNetProfit();
        }
        if (request.getTotalProfit() != null) {
            return request.getTotalProfit();
        }
        throw new BusinessException("Total profit or profit date range is required");
    }

    private List<ProfitDistributionLine> buildLines(ProfitDistribution distribution, BigDecimal totalProfit) {
        List<Partner> partners = partnerRepository.findByActiveTrueOrderByCodeAsc();
        if (partners.isEmpty()) {
            throw new BusinessException("No active partners configured");
        }
        BigDecimal totalShare = partners.stream()
                .map(Partner::getSharePercent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalShare.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Active partners must have share percentages");
        }

        List<ProfitDistributionLine> lines = new ArrayList<>();
        BigDecimal allocated = BigDecimal.ZERO;
        for (int i = 0; i < partners.size(); i++) {
            Partner partner = partners.get(i);
            BigDecimal amount;
            if (i == partners.size() - 1) {
                amount = totalProfit.subtract(allocated).setScale(2, RoundingMode.HALF_UP);
            } else {
                amount = totalProfit.multiply(partner.getSharePercent())
                        .divide(totalShare, 2, RoundingMode.HALF_UP);
                allocated = allocated.add(amount);
            }
            ProfitDistributionLine line = ProfitDistributionLine.builder()
                    .distribution(distribution)
                    .partnerId(partner.getId())
                    .sharePercent(partner.getSharePercent())
                    .amount(amount)
                    .build();
            lines.add(line);
        }
        return lines;
    }

    private String resolveDistributionNo(String requested) {
        if (requested != null && !requested.isBlank()) {
            String normalized = requested.trim();
            if (profitDistributionRepository.existsByDistributionNoIgnoreCase(normalized)) {
                throw new BusinessException("Distribution number already exists");
            }
            return normalized;
        }
        try {
            return numberingService.generateNextNumber("PROFIT_DISTRIBUTION");
        } catch (Exception e) {
            return "PD-" + System.currentTimeMillis();
        }
    }

    private ProfitDistribution loadDistribution(Long id) {
        return profitDistributionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profit distribution not found: " + id));
    }

    private ProfitDistributionDisplayDto toDisplay(ProfitDistribution distribution) {
        return ProfitDistributionDisplayDto.builder()
                .id(distribution.getId())
                .distributionNo(distribution.getDistributionNo())
                .periodLabel(distribution.getPeriodLabel())
                .totalProfit(distribution.getTotalProfit())
                .status(distribution.getStatus().name())
                .approvedAt(distribution.getApprovedAt())
                .journalEntryId(distribution.getJournalEntryId())
                .lines(distribution.getLines().stream().map(this::toLineDisplay).toList())
                .createdAt(distribution.getCreatedAt())
                .updatedAt(distribution.getUpdatedAt())
                .createdBy(distribution.getCreatedBy())
                .updatedBy(distribution.getUpdatedBy())
                .build();
    }

    private ProfitDistributionLineDisplayDto toLineDisplay(ProfitDistributionLine line) {
        Partner partner = partnerRepository.findById(line.getPartnerId()).orElse(null);
        return ProfitDistributionLineDisplayDto.builder()
                .id(line.getId())
                .partnerId(line.getPartnerId())
                .partnerCode(partner == null ? null : partner.getCode())
                .partnerName(partner == null ? null : partner.getName())
                .sharePercent(line.getSharePercent())
                .amount(line.getAmount())
                .build();
    }
}
