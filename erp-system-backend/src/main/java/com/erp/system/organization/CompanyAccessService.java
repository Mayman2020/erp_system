package com.erp.system.organization;

import com.erp.system.common.exception.BusinessException;
import com.erp.system.auth.repository.UserRepository;
import com.erp.system.organization.domain.*;
import com.erp.system.organization.dto.*;
import com.erp.system.organization.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CompanyAccessService {
    private final CompanyRepository companyRepository;
    private final UserCompanyAccessRepository accessRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CompanyDto> accessibleCompanies(Long userId) {
        return accessRepository.findByUserIdAndCompanyActiveTrueOrderByCompanyCodeAsc(userId)
                .stream().map(access -> toDto(access.getCompany(), access.isDefaultCompany())).toList();
    }

    @Transactional(readOnly = true)
    public Long resolveAndValidate(Long userId, String requestedCompanyId) {
        Long companyId;
        if (requestedCompanyId == null || requestedCompanyId.isBlank()) {
            companyId = accessRepository.findFirstByUserIdAndDefaultCompanyTrueAndCompanyActiveTrue(userId)
                    .orElseGet(() -> accessRepository.findByUserIdAndCompanyActiveTrueOrderByCompanyCodeAsc(userId)
                            .stream().findFirst()
                            .orElseThrow(() -> new BusinessException("COMPANY.ERRORS.NO_ACCESS")))
                    .getCompany().getId();
        } else {
            try {
                companyId = Long.valueOf(requestedCompanyId);
            } catch (NumberFormatException ex) {
                throw new BusinessException("COMPANY.ERRORS.INVALID_CONTEXT");
            }
        }
        if (!accessRepository.existsByUserIdAndCompanyIdAndCompanyActiveTrue(userId, companyId)) {
            throw new BusinessException("COMPANY.ERRORS.NO_ACCESS");
        }
        return companyId;
    }

    @Transactional(readOnly = true)
    public List<CompanyDto> allCompanies() {
        return companyRepository.findAllByOrderByCodeAsc().stream()
                .map(company -> toDto(company, false)).toList();
    }

    @Transactional
    public CompanyDto create(CompanyFormDto form, Long creatorUserId) {
        if (companyRepository.existsByCodeIgnoreCase(normalizeCode(form.getCode()))) {
            throw new BusinessException("COMPANY.ERRORS.CODE_EXISTS");
        }
        Company company = new Company();
        apply(company, form);
        Company saved = companyRepository.save(company);
        accessRepository.save(UserCompanyAccess.builder()
                .user(userRepository.findById(creatorUserId)
                        .orElseThrow(() -> new BusinessException("AUTH.ERRORS.ACCOUNT_DISABLED")))
                .company(saved)
                .defaultCompany(false)
                .build());
        return toDto(saved, false);
    }

    @Transactional
    public void grantAccess(Long companyId, Long userId, boolean makeDefault) {
        Company company = load(companyId);
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("AUTH.ERRORS.ACCOUNT_DISABLED"));
        if (makeDefault) {
            accessRepository.findByUserIdAndCompanyActiveTrueOrderByCompanyCodeAsc(userId)
                    .forEach(access -> access.setDefaultCompany(false));
        }
        UserCompanyAccess access = accessRepository.findById(new UserCompanyAccessId(userId, companyId))
                .orElseGet(() -> UserCompanyAccess.builder().user(user).company(company).build());
        access.setDefaultCompany(makeDefault);
        accessRepository.save(access);
    }

    @Transactional
    public CompanyDto update(Long id, CompanyFormDto form) {
        Company company = load(id);
        if (companyRepository.existsByCodeIgnoreCaseAndIdNot(normalizeCode(form.getCode()), id)) {
            throw new BusinessException("COMPANY.ERRORS.CODE_EXISTS");
        }
        apply(company, form);
        return toDto(companyRepository.save(company), false);
    }

    private void apply(Company company, CompanyFormDto form) {
        company.setCode(normalizeCode(form.getCode()));
        company.setNameEn(form.getNameEn().trim());
        company.setNameAr(form.getNameAr().trim());
        company.setEntityType(form.getEntityType());
        if (form.getEntityType() == CompanyEntityType.BRANCH) {
            if (form.getParentId() == null) throw new BusinessException("COMPANY.ERRORS.BRANCH_PARENT_REQUIRED");
            Company parent = load(form.getParentId());
            if (!parent.isActive() || Objects.equals(parent.getId(), company.getId())) {
                throw new BusinessException("COMPANY.ERRORS.INVALID_PARENT");
            }
            company.setParent(parent);
        } else {
            company.setParent(null);
        }
        company.setTaxId(trim(form.getTaxId()));
        company.setRegistrationNo(trim(form.getRegistrationNo()));
        company.setCurrencyCode(form.getCurrencyCode().trim().toUpperCase(Locale.ROOT));
        company.setCountryCode(form.getCountryCode().trim().toUpperCase(Locale.ROOT));
        company.setEmail(trim(form.getEmail()));
        company.setPhone(trim(form.getPhone()));
        company.setAddress(trim(form.getAddress()));
        company.setLogoUrl(trim(form.getLogoUrl()));
        company.setActive(!Boolean.FALSE.equals(form.getActive()));
    }

    private Company load(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new BusinessException("COMPANY.ERRORS.NOT_FOUND"));
    }
    private String normalizeCode(String value) { return value.trim().toUpperCase(Locale.ROOT); }
    private String trim(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private CompanyDto toDto(Company c, boolean isDefault) {
        return new CompanyDto(c.getId(), c.getCode(), c.getNameEn(), c.getNameAr(), c.getEntityType(),
                c.getParent() == null ? null : c.getParent().getId(),
                c.getParent() == null ? null : c.getParent().getNameEn(),
                c.getTaxId(), c.getRegistrationNo(), c.getCurrencyCode(), c.getCountryCode(),
                c.getEmail(), c.getPhone(), c.getAddress(), c.getLogoUrl(), c.isActive(), isDefault);
    }
}
