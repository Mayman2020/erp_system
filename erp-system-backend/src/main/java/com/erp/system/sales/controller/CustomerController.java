package com.erp.system.sales.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.sales.dto.display.CustomerDisplayDto;
import com.erp.system.sales.dto.form.CustomerFormDto;
import com.erp.system.sales.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sales/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public ApiResponse<List<CustomerDisplayDto>> getCustomers(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search) {
        return ApiResponse.success(customerService.getCustomers(active, search));
    }

    @GetMapping("/{id}")
    public ApiResponse<CustomerDisplayDto> getCustomer(@PathVariable Long id) {
        return ApiResponse.success(customerService.getCustomer(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CustomerDisplayDto> createCustomer(@Valid @RequestBody CustomerFormDto request) {
        return ApiResponse.success(customerService.createCustomer(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<CustomerDisplayDto> updateCustomer(@PathVariable Long id,
                                                          @Valid @RequestBody CustomerFormDto request) {
        return ApiResponse.success(customerService.updateCustomer(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ApiResponse.success(null);
    }
}
