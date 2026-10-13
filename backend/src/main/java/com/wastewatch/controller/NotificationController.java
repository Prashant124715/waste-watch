package com.wastewatch.controller;

import com.wastewatch.dto.ApiResponse;
import com.wastewatch.dto.notification.NotificationResponseDto;
import com.wastewatch.security.UserPrincipal;
import com.wastewatch.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Phase 13: In-App Notification REST Controller.
 *
 * All endpoints require authentication (any role).
 *
 * Endpoints:
 *   GET   /api/notifications           — All my notifications
 *   GET   /api/notifications/unread    — My unread notifications
 *   GET   /api/notifications/count     — Unread count badge
 *   PATCH /api/notifications/{id}/read — Mark one as read
 *   PATCH /api/notifications/read-all  — Mark all as read
 */
@RestController
@RequestMapping("/api/notifications")
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * GET /api/notifications
     * Returns all notifications for the current user (newest first).
     */
    @GetMapping
    public ResponseEntity<List<NotificationResponseDto>> getMyNotifications(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(notificationService.getMyNotifications(currentUser));
    }

    /**
     * GET /api/notifications/unread
     * Returns only unread notifications for the current user.
     */
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponseDto>> getMyUnreadNotifications(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(notificationService.getMyUnreadNotifications(currentUser));
    }

    /**
     * GET /api/notifications/count
     * Returns unread count for notification badge rendering.
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        long count = notificationService.getUnreadCount(currentUser);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    /**
     * PATCH /api/notifications/{id}/read
     * Marks a specific notification as read.
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponseDto>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        NotificationResponseDto dto = notificationService.markAsRead(id, currentUser);
        return ResponseEntity.ok(new ApiResponse<>(true, "Notification marked as read", dto));
    }

    /**
     * PATCH /api/notifications/read-all
     * Marks all notifications for the current user as read.
     */
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> markAllAsRead(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        int count = notificationService.markAllAsRead(currentUser);
        return ResponseEntity.ok(new ApiResponse<>(true,
                count + " notification(s) marked as read",
                Map.of("markedRead", count)));
    }
}
