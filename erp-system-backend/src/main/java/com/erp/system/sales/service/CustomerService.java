package com.erp.system.sales.service;

import com.erp.system.accounting.domain.Account;
import com.erp.system.accounting.repository.AccountRepository;
import com.erp.system.common.enums.AccountingType;
import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.common.service.NumberingService;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.sales.domain.Customer;
import com.erp.system.sales.dto.display.CustomerDisplayDto;
import com.erp.system.sales.dto.form.CustomerFormDto;
import com.erp.system.sales.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private static final String MODULE = "SALES";

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final NumberingService numberingService;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<CustomerDisplayDto> getCustomers(Boolean active, String search) {
        List<Customer> customers = active == null
                ? customerRepository.findAllByOrderByCodeAsc()
                : active
                ? customerRepository.findByActiveTrueOrderByCodeAsc()
                : customerRepository.findAllByOrderByCodeAsc().stream().filter(c -> !c.isActive()).toList();

        String normalizedSearch = search == null || search.isBlank() ? null : search.trim().toLowerCase();
        return customers.stream()
                .filter(customer -> normalizedSearch == null
                        || customer.getCode().toLowerCase().contains(normalizedSearch)
                        || customer.getNameEn().toLowerCase().contains(normalizedSearch)
                        || (customer.getNameAr() != null && customer.getNameAr().toLowerCase().contains(normalizedSearch))
                        || (customer.getEmail() != null && customer.getEmail().toLowerCase().contains(normalizedSearch)))
                .map(this::toDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerDisplayDto getCustomer(Long id) {
        return toDisplay(loadCustomer(id));
    }

    @Transactional
    public CustomerDisplayDto createCustomer(CustomerFormDto request) {
        String code = resolveCustomerCode(request.getCode());
        if (customerRepository.existsByCodeIgnoreCase(code)) {
            throw new BusinessException("Customer code already exists: " + code);
        }

        Customer customer = Customer.builder()
                .code(code)
                .active(true)
                .creditLimit(BigDecimal.ZERO)
                .build();
        applyForm(customer, request);
        customer = customerRepository.save(customer);

        activityLogService.log(MODULE, "CREATE", "Customer", customer.getId(), customer.getCode(),
                "Created customer " + customer.getCode());
        return toDisplay(customer);
    }

    @Transactional
    public CustomerDisplayDto updateCustomer(Long id, CustomerFormDto request) {
        Customer customer = loadCustomer(id);
        String code = request.getCode() == null || request.getCode().isBlank()
                ? customer.getCode()
                : request.getCode().trim();
        if (!code.equalsIgnoreCase(customer.getCode()) && customerRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw new BusinessException("Customer code already exists: " + code);
        }
        customer.setCode(code);
        applyForm(customer, request);
        customer = customerRepository.save(customer);

        activityLogService.log(MODULE, "UPDATE", "Customer", customer.getId(), customer.getCode(),
                "Updated customer " + customer.getCode());
        return toDisplay(customer);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = loadCustomer(id);
        customerRepository.delete(customer);
        activityLogService.log(MODULE, "DELETE", "Customer", customer.getId(), customer.getCode(),
                "Deleted customer " + customer.getCode());
    }

    Customer loadCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }

    private void applyForm(Customer customer, CustomerFormDto request) {
        customer.setNameEn(request.getNameEn().trim());
        customer.setNameAr(normalizeOptional(request.getNameAr()));
        customer.setEmail(normalizeOptional(request.getEmail()));
        customer.setPhone(normalizeOptional(request.getPhone()));
        customer.setTaxNumber(normalizeOptional(request.getTaxNumber()));
        customer.setAddress(normalizeOptional(request.getAddress()));
        customer.setCreditLimit(request.getCreditLimit() == null
                ? BigDecimal.ZERO
                : request.getCreditLimit().setScale(2, RoundingMode.HALF_UP));

        if (request.getReceivableAccountId() != null) {
            Account receivableAccount = accountRepository.findById(request.getReceivableAccountId())
                    .orElseThrow(() -> new ResourceNotFoundException("Account", request.getReceivableAccountId()));
            if (!receivableAccount.isActive() || receivableAccount.getAccountType() != AccountingType.ASSET) {
                throw new BusinessException("Receivable account must be an active asset account");
            }
            customer.setReceivableAccount(receivableAccount);
        } else {
            customer.setReceivableAccount(null);
        }

        if (request.getActive() != null) {
            customer.setActive(request.getActive());
        }
    }

    private String resolveCustomerCode(String code) {
        String normalized = normalizeOptional(code);
        if (normalized != null) {
            return normalized;
        }
        try {
            return numberingService.generateNextNumber("CUSTOMER_CODE");
        } catch (Exception exception) {
            return "CUS-" + System.currentTimeMillis();
        }
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private CustomerDisplayDto toDisplay(Customer customer) {
        Account receivable = customer.getReceivableAccount();
        return CustomerDisplayDto.builder()
                .id(customer.getId())
                .code(customer.getCode())
                .nameEn(customer.getNameEn())
                .nameAr(customer.getNameAr())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .taxNumber(customer.getTaxNumber())
                .address(customer.getAddress())
                .creditLimit(customer.getCreditLimit())
                .receivableAccountId(receivable != null ? receivable.getId() : null)
                .receivableAccountCode(receivable != null ? receivable.getCode() : null)
                .receivableAccountName(receivable != null ? resolveLocalizedName(receivable.getNameEn(), receivable.getNameAr()) : null)
                .active(customer.isActive())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }

    private String resolveLocalizedName(String nameEn, String nameAr) {
        if ("ar".equalsIgnoreCase(LocaleContextHolder.getLocale().getLanguage()) && nameAr != null) {
            return nameAr;
        }
        return nameEn;
    }
}
