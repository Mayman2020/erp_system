package com.erp.system.ui.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminMenuItemFormDto {

    @NotBlank
    @Size(max = 64)
    private String id;

    @Size(max = 64)
    private String parentId;

    @NotNull
    private Integer sortOrder = 0;

    @NotBlank
    @Size(max = 16)
    private String itemType;

    @NotBlank
    @Size(max = 128)
    private String titleKey;

    @Size(max = 64)
    private String icon;

    @Size(max = 512)
    private String url;

    private Boolean external = false;

    private Boolean targetBlank = false;

    @Size(max = 256)
    private String rolesCsv;

    @Size(max = 128)
    private String itemClasses;

    private Boolean breadcrumbsFlag;
}
