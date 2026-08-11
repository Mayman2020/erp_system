package com.erp.system.organization;

import org.springframework.stereotype.Component;

@Component
public class CompanyContext {
    private final ThreadLocal<Long> activeCompanyId = new ThreadLocal<>();
    public Long getCompanyId() { return activeCompanyId.get(); }
    public void setCompanyId(Long companyId) { activeCompanyId.set(companyId); }
    public void clear() { activeCompanyId.remove(); }
}
