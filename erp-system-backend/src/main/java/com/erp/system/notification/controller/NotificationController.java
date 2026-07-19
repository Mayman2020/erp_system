package com.erp.system.notification.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.notification.dto.NotificationPageResponse;
import com.erp.system.notification.dto.NotificationResponse;
import com.erp.system.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/my")
    public ApiResponse<NotificationPageResponse> getMy(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(notificationService.getMy(page, size));
    }

    @GetMapping("/my/unread-count")
    public ApiResponse<Map<String, Long>> unreadCount() {
        return ApiResponse.success(Map.of("unreadCount", notificationService.getUnreadCount()));
    }

    @PatchMapping("/{id}/read")
    public ApiResponse<NotificationResponse> markRead(@PathVariable Long id) {
        return ApiResponse.success(notificationService.markRead(id));
    }

    @PatchMapping("/my/read-all")
    public ApiResponse<Void> markAllRead() {
        notificationService.markAllRead();
        return ApiResponse.success(null);
    }
}
