package com.cts.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.cts.entity.Notification;
import com.cts.repository.NotificationRepository;
import com.cts.util.SecurityUtils;

public class NotificationControllerTest {

    @Mock private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationController notificationController;

    private final String MOCK_EMAIL = "student@educore.com";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getMyAlerts_success() {
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getLoggedInEmail).thenReturn(MOCK_EMAIL);

            Notification notification = Notification.builder().notificationId(1L).title("Alert").build();            when(notificationRepository.findByUserEmailOrderByCreatedAtDesc(MOCK_EMAIL))
                    .thenReturn(List.of(notification));

            ResponseEntity<List<Notification>> response = notificationController.getMyAlerts();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(1, response.getBody().size());
        }
    }

    @Test
    void markAsRead_success() {
        Notification notification = Notification.builder().notificationId(1L).isRead(false).build();        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        ResponseEntity<Void> response = notificationController.markAsRead(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(notification.isRead());
        verify(notificationRepository).save(notification);
    }
}