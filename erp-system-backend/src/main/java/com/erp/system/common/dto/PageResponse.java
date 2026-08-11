package com.erp.system.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;
    private boolean first;
    private boolean last;

    public static <T> PageResponse<T> from(Page<T> source) {
        return PageResponse.<T>builder()
                .content(source.getContent())
                .totalElements(source.getTotalElements())
                .totalPages(source.getTotalPages())
                .page(source.getNumber())
                .size(source.getSize())
                .first(source.isFirst())
                .last(source.isLast())
                .build();
    }
}
