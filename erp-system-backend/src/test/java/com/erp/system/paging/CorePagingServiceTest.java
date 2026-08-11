package com.erp.system.paging;

import com.erp.system.accounting.repository.AccountRepository;
import com.erp.system.common.dto.PageResponse;
import com.erp.system.common.service.NumberingService;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.hr.dto.display.EmployeeDisplayDto;
import com.erp.system.hr.repository.EmployeeRepository;
import com.erp.system.hr.service.EmployeeService;
import com.erp.system.inventory.dto.display.ProductDisplayDto;
import com.erp.system.inventory.repository.ProductCategoryRepository;
import com.erp.system.inventory.repository.ProductRepository;
import com.erp.system.inventory.repository.StockLevelRepository;
import com.erp.system.inventory.service.ProductService;
import com.erp.system.inventory.service.UnitOfMeasureService;
import com.erp.system.sales.dto.display.CustomerDisplayDto;
import com.erp.system.sales.repository.CustomerRepository;
import com.erp.system.sales.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CorePagingServiceTest {

    @Mock ProductRepository productRepository;
    @Mock ProductCategoryRepository productCategoryRepository;
    @Mock UnitOfMeasureService unitOfMeasureService;
    @Mock StockLevelRepository stockLevelRepository;
    @Mock ActivityLogService activityLogService;
    @InjectMocks ProductService productService;

    @Mock CustomerRepository customerRepository;
    @Mock AccountRepository accountRepository;
    @Mock NumberingService numberingService;
    @InjectMocks CustomerService customerService;

    @Mock EmployeeRepository employeeRepository;
    @InjectMocks EmployeeService employeeService;

    @Test
    void productsReturnPageShapeAndUseTrimmedSearch() {
        PageRequest pageable = PageRequest.of(1, 5);
        when(productRepository.searchPaged(true, null, "bolt", pageable)).thenReturn(Page.empty(pageable));

        PageResponse<ProductDisplayDto> result = productService.getProductsPaged(true, null, "  bolt ", pageable);

        assertPageShape(result, 1, 5);
        verify(productRepository).searchPaged(true, null, "bolt", pageable);
    }

    @Test
    void customersReturnPageShapeAndUseTrimmedSearch() {
        PageRequest pageable = PageRequest.of(0, 5);
        when(customerRepository.searchPaged(null, "acme", pageable)).thenReturn(Page.empty(pageable));

        PageResponse<CustomerDisplayDto> result = customerService.getCustomersPaged(null, " acme ", pageable);

        assertPageShape(result, 0, 5);
        verify(customerRepository).searchPaged(null, "acme", pageable);
    }

    @Test
    void employeesTreatBlankSearchAsUnfilteredAndReturnPageShape() {
        PageRequest pageable = PageRequest.of(0, 5);
        when(employeeRepository.findPaged(true, pageable)).thenReturn(Page.empty(pageable));

        PageResponse<EmployeeDisplayDto> result = employeeService.getPaged(true, "   ", pageable);

        assertPageShape(result, 0, 5);
        verify(employeeRepository).findPaged(true, pageable);
    }

    private static void assertPageShape(PageResponse<?> result, int page, int size) {
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getTotalPages()).isZero();
        assertThat(result.getPage()).isEqualTo(page);
        assertThat(result.getSize()).isEqualTo(size);
        assertThat(result.isFirst()).isEqualTo(page == 0);
        assertThat(result.isLast()).isTrue();
    }
}
