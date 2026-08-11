package com.erp.system.maintenance.service;

import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.maintenance.domain.MaintenanceAsset;
import com.erp.system.maintenance.dto.display.MaintenanceAssetDisplayDto;
import com.erp.system.maintenance.dto.form.MaintenanceAssetFormDto;
import com.erp.system.maintenance.repository.MaintenanceAssetRepository;
import com.erp.system.sales.domain.Customer;
import com.erp.system.sales.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MaintenanceAssetService {

    private static final String MODULE = "MAINTENANCE";

    private final MaintenanceAssetRepository assetRepository;
    private final CustomerRepository customerRepository;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<MaintenanceAssetDisplayDto> getAll(String status) {
        return assetRepository.findAllByOrderByAssetCodeAsc().stream()
                .filter(asset -> !StringUtils.hasText(status) || status.equalsIgnoreCase(asset.getStatus()))
                .map(this::toDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public MaintenanceAssetDisplayDto getById(Long id) {
        return toDisplay(load(id));
    }

    @Transactional
    public MaintenanceAssetDisplayDto create(MaintenanceAssetFormDto request) {
        String code = request.getAssetCode().trim();
        if (assetRepository.existsByAssetCodeIgnoreCase(code)) {
            throw new BusinessException("Asset code already exists");
        }
        MaintenanceAsset asset = new MaintenanceAsset();
        applyForm(asset, request);
        asset = assetRepository.save(asset);
        activityLogService.log(MODULE, "CREATE", "MaintenanceAsset", asset.getId(), asset.getAssetCode(),
                "Created maintenance asset " + asset.getAssetCode());
        return toDisplay(asset);
    }

    @Transactional
    public MaintenanceAssetDisplayDto update(Long id, MaintenanceAssetFormDto request) {
        MaintenanceAsset asset = load(id);
        String code = request.getAssetCode().trim();
        if (!asset.getAssetCode().equalsIgnoreCase(code) && assetRepository.existsByAssetCodeIgnoreCase(code)) {
            throw new BusinessException("Asset code already exists");
        }
        applyForm(asset, request);
        asset = assetRepository.save(asset);
        activityLogService.log(MODULE, "UPDATE", "MaintenanceAsset", asset.getId(), asset.getAssetCode(),
                "Updated maintenance asset " + asset.getAssetCode());
        return toDisplay(asset);
    }

    @Transactional
    public void delete(Long id) {
        MaintenanceAsset asset = load(id);
        assetRepository.delete(asset);
        activityLogService.log(MODULE, "DELETE", "MaintenanceAsset", id, asset.getAssetCode(),
                "Deleted maintenance asset " + asset.getAssetCode());
    }

    MaintenanceAsset load(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaintenanceAsset", id));
    }

    private void applyForm(MaintenanceAsset asset, MaintenanceAssetFormDto request) {
        asset.setAssetCode(request.getAssetCode().trim());
        asset.setName(request.getName().trim());
        asset.setSerialNo(trimToNull(request.getSerialNo()));
        if (request.getCustomerId() != null) {
            customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new BusinessException("Customer not found"));
            asset.setCustomerId(request.getCustomerId());
        } else {
            asset.setCustomerId(null);
        }
        if (StringUtils.hasText(request.getStatus())) {
            asset.setStatus(request.getStatus().trim().toUpperCase(Locale.ROOT));
        } else if (asset.getStatus() == null) {
            asset.setStatus("ACTIVE");
        }
        asset.setNotes(trimToNull(request.getNotes()));
    }

    private MaintenanceAssetDisplayDto toDisplay(MaintenanceAsset asset) {
        Customer customer = asset.getCustomerId() == null ? null
                : customerRepository.findById(asset.getCustomerId()).orElse(null);
        return MaintenanceAssetDisplayDto.builder()
                .id(asset.getId())
                .assetCode(asset.getAssetCode())
                .name(asset.getName())
                .serialNo(asset.getSerialNo())
                .customerId(asset.getCustomerId())
                .customerName(customer != null ? customer.getNameEn() : null)
                .status(asset.getStatus())
                .notes(asset.getNotes())
                .createdAt(asset.getCreatedAt())
                .updatedAt(asset.getUpdatedAt())
                .createdBy(asset.getCreatedBy())
                .updatedBy(asset.getUpdatedBy())
                .build();
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
