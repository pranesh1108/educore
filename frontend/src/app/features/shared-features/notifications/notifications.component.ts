import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

@Component({
    selector: 'app-global-notifications',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './notifications.component.html',
    styleUrls: ['./notifications.component.css']
})
export class NotificationsComponent implements OnInit {
    notifications: any[] = [];
    unreadCount = 0;
    isOpen = false; // Toggle view state popup panel

    private baseUrl = 'http://localhost:9098/api/v1/notifications';

    constructor(private http: HttpClient) {}

    ngOnInit(): void {
        this.refreshNotificationMetrics();
        // Optional: Poll backend alerts metric updates every 30 seconds
        setInterval(() => this.refreshNotificationMetrics(), 30000);
    }

    refreshNotificationMetrics(): void {
        this.http.get<any[]>(this.baseUrl).subscribe(data => {
            this.notifications = data || [];
        });
        this.http.get<number>(`${this.baseUrl}/unread-count`).subscribe(count => {
            this.unreadCount = count || 0;
        });
    }

    toggleDropdown(): void {
        this.isOpen = !this.isOpen;
    }

    markAsRead(notification: any): void {
        if (!notification.isRead) {
            this.http.put(`${this.baseUrl}/${notification.notificationId}/read`, {}).subscribe(() => {
                notification.isRead = true;
                if (this.unreadCount > 0) this.unreadCount--;
            });
        }
    }
}