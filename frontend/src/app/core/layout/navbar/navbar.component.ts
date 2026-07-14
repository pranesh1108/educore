<<<<<<< HEAD
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
=======
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
>>>>>>> 37751a7 (update the main code)
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
<<<<<<< HEAD
export class NavbarComponent {

  constructor(private authService: AuthService) {}
=======
export class NavbarComponent implements OnInit {
  notifications: any[] = [];
  unreadCount = 0;
  isNotificationDropdownOpen = false;
  private notificationApiUrl = 'http://localhost:9098/api/v1/notifications';

  constructor(
      private authService: AuthService,
      private http: HttpClient
  ) {}

  ngOnInit(): void {
    // Run immediately on initialization
    this.refreshNotifications();

    // Check for new notification updates every 10 seconds continuously
    setInterval(() => this.refreshNotifications(), 10000);
  }
>>>>>>> 37751a7 (update the main code)

  get currentUser() {
    return this.authService.getCurrentUser();
  }

<<<<<<< HEAD
  logout(): void {
    this.authService.logout();
  }
}
=======
  refreshNotifications(): void {
    // Only attempt to call the backend if a user session actively exists
    if (!this.currentUser) {
      return;
    }

    this.http.get<any[]>(this.notificationApiUrl).subscribe({
      next: (data) => this.notifications = data || [],
      error: (err) => console.log('Waiting for active session authorization token...')
    });

    this.http.get<number>(`${this.notificationApiUrl}/unread-count`).subscribe({
      next: (count) => this.unreadCount = count || 0,
      error: (err) => {}
    });
  }

  toggleNotificationDropdown(): void {
    this.isNotificationDropdownOpen = !this.isNotificationDropdownOpen;
    // Auto-refresh updates when the user opens the panel
    if (this.isNotificationDropdownOpen) {
      this.refreshNotifications();
    }
  }

  markAlertAsRead(alert: any): void {
    if (!alert.isRead) {
      this.http.put(`${this.notificationApiUrl}/${alert.notificationId}/read`, {}).subscribe({
        next: () => {
          alert.isRead = true;
          if (this.unreadCount > 0) this.unreadCount--;
        }
      });
    }
  }

  logout(): void {
    this.isNotificationDropdownOpen = false;
    this.notifications = [];
    this.unreadCount = 0;
    this.authService.logout();
  }
}
>>>>>>> 37751a7 (update the main code)
