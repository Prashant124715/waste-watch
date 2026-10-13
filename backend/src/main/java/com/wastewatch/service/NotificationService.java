package com.wastewatch.service;

import com.wastewatch.dto.notification.NotificationResponseDto;
import com.wastewatch.entity.AppUser;
import com.wastewatch.entity.Notification;
import com.wastewatch.entity.enums.NotificationType;
import com.wastewatch.entity.enums.UserRole;
import com.wastewatch.exception.ResourceNotFoundException;
import com.wastewatch.repository.NotificationRepository;
import com.wastewatch.repository.UserRepository;
import com.wastewatch.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages creation and retrieval of in-app notifications.
 *
 * Notification Triggers:
 * - VIOLATION_CREATED: When an inspection produces a POOR score (auto-violation).
 * - VIOLATION_SEVERITY_HIGH: When the violation severity is HIGH or CRITICAL.
 * - PERSISTENT_NON_COMPLIANCE: When a room has 3+ consecutive POOR inspections.
 * - COVERAGE_ALERT: When inspection coverage in an area drops below target (future).
 *
 * Notifications are broadcast to all ADMIN users.
 */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    /**
     * Sends a notification to all admin users.
     */
    @Transactional
    public void notifyAllAdmins(String title, String message, NotificationType type) {
        List<AppUser> admins = userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.ADMIN)
                .collect(Collectors.toList());

        for (AppUser admin : admins) {
            Notification notification = new Notification(admin, title, message, type);
            notificationRepository.save(notification);
        }
    }

    /**
     * Sends a notification to a specific user.
     */
    @Transactional
    public void notifyUser(AppUser user, String title, String message, NotificationType type) {
        Notification notification = new Notification(user, title, message, type);
        notificationRepository.save(notification);
    }

    /**
     * Returns all notifications for the current authenticated user.
     */
    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getMyNotifications(UserPrincipal currentUser) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(NotificationResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Returns all unread notifications for the current authenticated user.
     */
    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getMyUnreadNotifications(UserPrincipal currentUser) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(NotificationResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Marks a specific notification as read.
     */
    @Transactional
    public NotificationResponseDto markAsRead(Long notificationId, UserPrincipal currentUser) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        if (!notification.getUser().getId().equals(currentUser.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Cannot access another user's notification");
        }

        notification.setIsRead(true);
        Notification saved = notificationRepository.save(notification);
        return NotificationResponseDto.fromEntity(saved);
    }

    /**
     * Marks all notifications as read for the current user.
     */
    @Transactional
    public int markAllAsRead(UserPrincipal currentUser) {
        List<Notification> unread = notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(currentUser.getId());

        for (Notification n : unread) {
            n.setIsRead(true);
            notificationRepository.save(n);
        }
        return unread.size();
    }

    /**
     * Returns unread notification count for the current user.
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(UserPrincipal currentUser) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(currentUser.getId()).size();
    }
}
