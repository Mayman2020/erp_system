package com.erp.system.ui.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MenuNodeDto {

    private String id;
    /** i18n key (same as frontend NavigationItem.title) */
    private String title;
    private String type;
    private String icon;
    private String url;
    private Boolean external;
    private Boolean target;
    private String classes;
    private Boolean breadcrumbs;
    private List<MenuNodeDto> children;
}
