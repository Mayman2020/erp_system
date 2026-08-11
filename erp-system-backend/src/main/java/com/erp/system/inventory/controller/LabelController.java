package com.erp.system.inventory.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.inventory.dto.display.LabelPreviewDisplayDto;
import com.erp.system.inventory.service.LabelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory/labels")
@RequiredArgsConstructor
public class LabelController {

    private final LabelService labelService;

    @GetMapping("/preview")
    public ApiResponse<LabelPreviewDisplayDto> preview(@RequestParam Long productId) {
        return ApiResponse.success(labelService.preview(productId));
    }
}
