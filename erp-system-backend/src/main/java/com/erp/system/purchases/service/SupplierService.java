package com.erp.system.purchases.service;

import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.purchases.domain.Supplier;
import com.erp.system.purchases.dto.display.SupplierDisplayDto;
import com.erp.system.purchases.dto.form.SupplierFormDto;
import com.erp.system.purchases.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class SupplierService {

    private static final String MODULE = "PURCHASES";

    private final SupplierRepository supplierRepository;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<SupplierDisplayDto> getAll() {
        return supplierRepository.findAllByOrderByIdDesc().stream()
                .map(this::toDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public SupplierDisplayDto getById(Long id) {
        return toDisplay(loadSupplier(id));
    }

    @Transactional
    public SupplierDisplayDto create(SupplierFormDto request) {
        Supplier supplier = new Supplier();
        applyForm(supplier, request);
        supplier = supplierRepository.save(supplier);
        activityLogService.log(MODULE, "CREATE", "Supplier", supplier.getId(), String.valueOf(supplier.getId()),
                "Created Supplier " + supplier.getId());
        return toDisplay(supplier);
    }

    @Transactional
    public SupplierDisplayDto update(Long id, SupplierFormDto request) {
        Supplier supplier = loadSupplier(id);
        applyForm(supplier, request);
        supplier = supplierRepository.save(supplier);
        activityLogService.log(MODULE, "UPDATE", "Supplier", supplier.getId(), String.valueOf(supplier.getId()),
                "Updated Supplier " + supplier.getId());
        return toDisplay(supplier);
    }

    @Transactional
    public void delete(Long id) {
        Supplier supplier = loadSupplier(id);
        supplierRepository.delete(supplier);
        activityLogService.log(MODULE, "DELETE", "Supplier", id, String.valueOf(id),
                "Deleted Supplier " + id);
    }

    private Supplier loadSupplier(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", id));
    }

    private void applyForm(Supplier supplier, SupplierFormDto request) {

        supplier.setCode(request.getCode().trim());
        supplier.setNameEn(request.getNameEn().trim());
        supplier.setNameAr(request.getNameAr());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        supplier.setTaxNumber(request.getTaxNumber());
        supplier.setAddress(request.getAddress());
        supplier.setPayableAccountId(request.getPayableAccountId());
        supplier.setActive(request.getActive() == null || request.getActive());

    }

    private SupplierDisplayDto toDisplay(Supplier supplier) {
        return SupplierDisplayDto.builder()
                .id(supplier.getId())

                .code(supplier.getCode())
                .nameEn(supplier.getNameEn())
                .nameAr(supplier.getNameAr())
                .email(supplier.getEmail())
                .phone(supplier.getPhone())
                .taxNumber(supplier.getTaxNumber())
                .address(supplier.getAddress())
                .payableAccountId(supplier.getPayableAccountId())
                .active(supplier.isActive())

                .createdAt(supplier.getCreatedAt())
                .updatedAt(supplier.getUpdatedAt())
                .build();
    }

}
