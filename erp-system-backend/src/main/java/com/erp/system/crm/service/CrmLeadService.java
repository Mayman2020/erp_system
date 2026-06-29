package com.erp.system.crm.service;

import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.crm.domain.LeadStatus;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.crm.domain.CrmLead;
import com.erp.system.crm.dto.display.CrmLeadDisplayDto;
import com.erp.system.crm.dto.form.CrmLeadFormDto;
import com.erp.system.crm.repository.CrmLeadRepository;
import com.erp.system.sales.dto.display.CustomerDisplayDto;
import com.erp.system.sales.dto.form.CustomerFormDto;
import com.erp.system.sales.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CrmLeadService {

    private static final String MODULE = "CRM";

    private final CrmLeadRepository crmLeadRepository;
    private final ActivityLogService activityLogService;
    private final CustomerService customerService;

    @Transactional(readOnly = true)
    public List<CrmLeadDisplayDto> getAll() {
        return crmLeadRepository.findAllByOrderByIdDesc().stream()
                .map(this::toDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public CrmLeadDisplayDto getById(Long id) {
        return toDisplay(loadCrmLead(id));
    }

    @Transactional
    public CrmLeadDisplayDto create(CrmLeadFormDto request) {
        CrmLead crmLead = new CrmLead();
        applyForm(crmLead, request);
        crmLead = crmLeadRepository.save(crmLead);
        activityLogService.log(MODULE, "CREATE", "CrmLead", crmLead.getId(), String.valueOf(crmLead.getId()),
                "Created CrmLead " + crmLead.getId());
        return toDisplay(crmLead);
    }

    @Transactional
    public CrmLeadDisplayDto update(Long id, CrmLeadFormDto request) {
        CrmLead crmLead = loadCrmLead(id);
        applyForm(crmLead, request);
        crmLead = crmLeadRepository.save(crmLead);
        activityLogService.log(MODULE, "UPDATE", "CrmLead", crmLead.getId(), String.valueOf(crmLead.getId()),
                "Updated CrmLead " + crmLead.getId());
        return toDisplay(crmLead);
    }

    @Transactional
    public void delete(Long id) {
        CrmLead crmLead = loadCrmLead(id);
        crmLeadRepository.delete(crmLead);
        activityLogService.log(MODULE, "DELETE", "CrmLead", id, String.valueOf(id),
                "Deleted CrmLead " + id);
    }

    @Transactional
    public CustomerDisplayDto convertToCustomer(Long id) {
        CrmLead lead = loadCrmLead(id);
        if (lead.getCustomerId() != null) {
            throw new BusinessException("Lead is already linked to a customer");
        }
        if (lead.getStatus() == LeadStatus.LOST) {
            throw new BusinessException("Lost leads cannot be converted");
        }
        CustomerFormDto form = new CustomerFormDto();
        form.setNameEn(lead.getName());
        form.setEmail(lead.getEmail());
        form.setPhone(lead.getPhone());
        form.setActive(true);
        CustomerDisplayDto customer = customerService.createCustomer(form);
        lead.setCustomerId(customer.getId());
        lead.setStatus(LeadStatus.WON);
        crmLeadRepository.save(lead);
        activityLogService.log(MODULE, "CONVERT", "CrmLead", lead.getId(), lead.getLeadNumber(),
                "Converted lead " + lead.getLeadNumber() + " to customer " + customer.getCode());
        return customer;
    }

    private CrmLead loadCrmLead(Long id) {
        return crmLeadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CrmLead", id));
    }

    private void applyForm(CrmLead crmLead, CrmLeadFormDto request) {

        crmLead.setLeadNumber(request.getLeadNumber().trim());
        crmLead.setName(request.getName().trim());
        crmLead.setCompany(request.getCompany());
        crmLead.setEmail(request.getEmail());
        crmLead.setPhone(request.getPhone());
        crmLead.setSource(request.getSource());
        crmLead.setStatus(request.getStatus());
        crmLead.setCustomerId(request.getCustomerId());
        crmLead.setAssignedTo(request.getAssignedTo());
        crmLead.setNotes(request.getNotes());

    }

    private CrmLeadDisplayDto toDisplay(CrmLead crmLead) {
        return CrmLeadDisplayDto.builder()
                .id(crmLead.getId())

                .leadNumber(crmLead.getLeadNumber())
                .name(crmLead.getName())
                .company(crmLead.getCompany())
                .email(crmLead.getEmail())
                .phone(crmLead.getPhone())
                .source(crmLead.getSource())
                .status(crmLead.getStatus())
                .customerId(crmLead.getCustomerId())
                .assignedTo(crmLead.getAssignedTo())
                .notes(crmLead.getNotes())

                .createdAt(crmLead.getCreatedAt())
                .updatedAt(crmLead.getUpdatedAt())
                .build();
    }

}
