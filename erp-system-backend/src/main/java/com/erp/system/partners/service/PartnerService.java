package com.erp.system.partners.service;

import com.erp.system.accounting.domain.Account;
import com.erp.system.accounting.repository.AccountRepository;
import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.partners.domain.Partner;
import com.erp.system.partners.dto.display.PartnerDisplayDto;
import com.erp.system.partners.dto.form.PartnerFormDto;
import com.erp.system.partners.repository.PartnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PartnerService {

    private static final String MODULE = "PARTNERS";

    private final PartnerRepository partnerRepository;
    private final AccountRepository accountRepository;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<PartnerDisplayDto> getAll() {
        return partnerRepository.findAllByOrderByCodeAsc().stream().map(this::toDisplay).toList();
    }

    @Transactional(readOnly = true)
    public PartnerDisplayDto getById(Long id) {
        return toDisplay(loadPartner(id));
    }

    @Transactional
    public PartnerDisplayDto create(PartnerFormDto request) {
        if (partnerRepository.existsByCodeIgnoreCase(request.getCode().trim())) {
            throw new BusinessException("Partner code already exists");
        }
        Partner partner = new Partner();
        applyForm(partner, request);
        partner = partnerRepository.save(partner);
        activityLogService.log(MODULE, "CREATE", "Partner", partner.getId(), partner.getCode(),
                "Created partner " + partner.getCode());
        return toDisplay(partner);
    }

    @Transactional
    public PartnerDisplayDto update(Long id, PartnerFormDto request) {
        Partner partner = loadPartner(id);
        if (!partner.getCode().equalsIgnoreCase(request.getCode().trim())
                && partnerRepository.existsByCodeIgnoreCase(request.getCode().trim())) {
            throw new BusinessException("Partner code already exists");
        }
        applyForm(partner, request);
        partner = partnerRepository.save(partner);
        activityLogService.log(MODULE, "UPDATE", "Partner", partner.getId(), partner.getCode(),
                "Updated partner " + partner.getCode());
        return toDisplay(partner);
    }

    @Transactional
    public void delete(Long id) {
        Partner partner = loadPartner(id);
        partnerRepository.delete(partner);
        activityLogService.log(MODULE, "DELETE", "Partner", partner.getId(), partner.getCode(),
                "Deleted partner " + partner.getCode());
    }

    Partner loadPartner(Long id) {
        return partnerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found: " + id));
    }

    private void applyForm(Partner partner, PartnerFormDto request) {
        partner.setCode(request.getCode().trim());
        partner.setName(request.getName().trim());
        partner.setSharePercent(normalizeShare(request.getSharePercent()));
        if (request.getCapitalAccountId() != null) {
            validateAccount(request.getCapitalAccountId());
            partner.setCapitalAccountId(request.getCapitalAccountId());
        } else {
            partner.setCapitalAccountId(null);
        }
        if (request.getDrawingAccountId() != null) {
            validateAccount(request.getDrawingAccountId());
            partner.setDrawingAccountId(request.getDrawingAccountId());
        } else {
            partner.setDrawingAccountId(null);
        }
        if (request.getActive() != null) {
            partner.setActive(request.getActive());
        }
    }

    private BigDecimal normalizeShare(BigDecimal share) {
        if (share == null) {
            return BigDecimal.ZERO;
        }
        if (share.compareTo(BigDecimal.ZERO) < 0 || share.compareTo(new BigDecimal("100")) > 0) {
            throw new BusinessException("Share percent must be between 0 and 100");
        }
        return share.setScale(4, java.math.RoundingMode.HALF_UP);
    }

    private void validateAccount(Long accountId) {
        accountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException("Account not found: " + accountId));
    }

    private PartnerDisplayDto toDisplay(Partner partner) {
        Account capital = partner.getCapitalAccountId() == null ? null
                : accountRepository.findById(partner.getCapitalAccountId()).orElse(null);
        Account drawing = partner.getDrawingAccountId() == null ? null
                : accountRepository.findById(partner.getDrawingAccountId()).orElse(null);
        return PartnerDisplayDto.builder()
                .id(partner.getId())
                .code(partner.getCode())
                .name(partner.getName())
                .sharePercent(partner.getSharePercent())
                .capitalAccountId(partner.getCapitalAccountId())
                .capitalAccountCode(capital == null ? null : capital.getCode())
                .capitalAccountName(capital == null ? null : capital.getNameEn())
                .drawingAccountId(partner.getDrawingAccountId())
                .drawingAccountCode(drawing == null ? null : drawing.getCode())
                .drawingAccountName(drawing == null ? null : drawing.getNameEn())
                .active(partner.isActive())
                .createdAt(partner.getCreatedAt())
                .updatedAt(partner.getUpdatedAt())
                .createdBy(partner.getCreatedBy())
                .updatedBy(partner.getUpdatedBy())
                .build();
    }
}
