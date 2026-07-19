package com.erp.system.notification.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NotificationPageResponse {
    private List<NotificationResponse> content;
    private long totalElements;
    private int page;
    private int size;
}
