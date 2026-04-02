package com.erp.system.common.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.common.dto.LookupItemDto;
import com.erp.system.common.service.LookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/lookups")
@RequiredArgsConstructor
public class LookupController {

    private final LookupService lookupService;

    @GetMapping("/{type}")
    public ApiResponse<List<LookupItemDto>> getLookups(@PathVariable String type) {
        return ApiResponse.success(lookupService.getLookups(type));
    }
}
