package com.cts.repository;

import com.cts.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Fetch notifications for a specific user ordered from newest to oldest
    List<Notification> findByUserEmailOrderByCreatedAtDesc(String userEmail);
    
    // Count unread notifications for the status badge indicators
    long countByUserEmailAndIsReadFalse(String userEmail);
}