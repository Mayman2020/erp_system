package com.erp.system.organization;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.common.security.JwtPrincipal;
import com.erp.system.organization.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/organizations/companies")
@RequiredArgsConstructor
public class CompanyController {
    private final CompanyAccessService companyAccessService;

    @GetMapping("/accessible")
    public ApiResponse<List<CompanyDto>> accessible(@AuthenticationPrincipal JwtPrincipal principal) {
        return ApiResponse.success(companyAccessService.accessibleCompanies(principal.userId()));
    }

    @GetMapping
    public ApiResponse<List<CompanyDto>> all() {
        return ApiResponse.success(companyAccessService.allCompanies());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CompanyDto> create(@AuthenticationPrincipal JwtPrincipal principal,
                                          @Valid @RequestBody CompanyFormDto form) {
        return ApiResponse.success(companyAccessService.create(form, principal.userId()));
    }

    @PutMapping("/{id}")
    public ApiResponse<CompanyDto> update(@PathVariable Long id, @Valid @RequestBody CompanyFormDto form) {
        return ApiResponse.success(companyAccessService.update(id, form));
    }

    @PutMapping("/{companyId}/users/{userId}")
    public ApiResponse<Void> grantAccess(@PathVariable Long companyId, @PathVariable Long userId,
                                         @RequestParam(defaultValue = "false") boolean makeDefault) {
        companyAccessService.grantAccess(companyId, userId, makeDefault);
        return ApiResponse.success(null);
    }
}
