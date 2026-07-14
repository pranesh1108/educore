package com.cts.controller;

import com.cts.entity.Notification;
import com.cts.repository.NotificationRepository;
import com.cts.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications Catalogue", description = "Context-aware global alerts lookup hub for all authenticated user roles")
@CrossOrigin(origins = "http://localhost:4200")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @Operation(summary = "Get current user alerts trail")
    @GetMapping
    public ResponseEntity<List<Notification>> getMyAlerts() {
        String email = SecurityUtils.getLoggedInEmail();
        return ResponseEntity.ok(notificationRepository.findByUserEmailOrderByCreatedAtDesc(email));
    }

    @Operation(summary = "Get current total counts of unread metrics")
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount() {
        String email = SecurityUtils.getLoggedInEmail();
        return ResponseEntity.ok(notificationRepository.countByUserEmailAndIsReadFalse(email));
    }

    @Operation(summary = "Mark a single specific alert as read")
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationRepository.findById(id).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
        return ResponseEntity.ok().build();
    }
}