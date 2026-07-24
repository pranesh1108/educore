import { Component, OnInit, ElementRef, HostListener } from '@angular/core';
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
  isOpen = false;

  private baseUrl = 'http://localhost:9098/api/v1/notifications';

  constructor(
    private http: HttpClient,
    private elementRef: ElementRef // Inject ElementRef to detect clicks outside
  ) {}

  ngOnInit(): void {
    this.refreshNotificationMetrics();
    setInterval(() => this.refreshNotificationMetrics(), 15000);
  }

  // Listen for clicks anywhere in the DOM document
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event): void {
    // If click target is outside of this component, close the popover
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.isOpen = false;
    }
  }

  refreshNotificationMetrics(): void {
    this.http.get<any[]>(this.baseUrl).subscribe({
      next: (data) => {
        this.notifications = data || [];
        this.recalculateUnreadCount();
      },
      error: (err) => console.error('Error fetching notifications:', err)
    });
  }

  recalculateUnreadCount(): void {
    this.unreadCount = this.notifications.filter(n => !n.isRead && !n.read).length;
  }

  toggleDropdown(event?: Event): void {
    if (event) {
      event.stopPropagation(); // Prevents immediate closure on button click
    }
    this.isOpen = !this.isOpen;
    if (this.isOpen) {
      this.refreshNotificationMetrics();
    }
  }

  markAsRead(notification: any, event?: Event): void {
    if (event) {
      event.stopPropagation();
    }

    const id = notification.notificationId || notification.id;

    // Immediately update UI locally
    notification.isRead = true;
    notification.read = true;
    this.recalculateUnreadCount();

    if (id) {
      this.http.put(`${this.baseUrl}/${id}/read`, {}).subscribe({
        next: () => console.log(`Notification ${id} marked read.`),
        error: () => {
          this.http.patch(`${this.baseUrl}/${id}/read`, {}).subscribe();
        }
      });
    }
  }

  markAllAsRead(event?: Event): void {
    if (event) {
      event.stopPropagation();
    }

    this.notifications.forEach(n => {
      n.isRead = true;
      n.read = true;
    });
    this.unreadCount = 0;

    this.http.put(`${this.baseUrl}/read-all`, {}).subscribe({
      next: () => console.log('All notifications marked as read.'),
      error: () => {
        this.notifications.forEach(n => {
          const id = n.notificationId || n.id;
          if (id) this.http.put(`${this.baseUrl}/${id}/read`, {}).subscribe();
        });
      }
    });
  }

  isItemUnread(alert: any): boolean {
    return !alert.isRead && !alert.read;
  }

  getNotificationIcon(alert: any): string {
    const title = alert.title || alert.message || '';
    if (title.includes('Assignment') || title.includes('Task')) return 'bi-journal-plus';
    if (title.includes('Handout') || title.includes('Material')) return 'bi-file-earmark-pdf';
    if (title.includes('Graded') || title.includes('Score')) return 'bi-check2-circle';
    if (title.includes('Exam') || title.includes('Result')) return 'bi-award';
    return 'bi-bell';
  }

  getNotificationIconClass(alert: any): string {
    const title = alert.title || alert.message || '';
    if (title.includes('Assignment')) return 'bg-primary-subtle text-primary';
    if (title.includes('Handout')) return 'bg-info-subtle text-info';
    if (title.includes('Graded')) return 'bg-success-subtle text-success';
    if (title.includes('Exam') || title.includes('Result')) return 'bg-warning-subtle text-warning-emphasis';
    return 'bg-purple text-white';
  }

  getCategoryLabel(alert: any): string {
    const title = alert.title || alert.message || '';
    if (title.includes('Assignment')) return 'Assignment';
    if (title.includes('Handout')) return 'Resource';
    if (title.includes('Graded')) return 'Evaluation';
    if (title.includes('Exam') || title.includes('Result')) return 'Examination';
    return 'System Alert';
  }
}