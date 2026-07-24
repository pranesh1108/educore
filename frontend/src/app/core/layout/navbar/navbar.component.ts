import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

// Import the Global Notifications Component
import { NotificationsComponent } from '../../../features/shared-features/notifications/notifications.component';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [
    CommonModule, 
    NotificationsComponent // <-- Register standalone notifications component
  ],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  get currentUser() {
    return this.authService.getCurrentUser();
  }

  goToDashboard(): void {
    this.router.navigateByUrl('/dashboard');
  }

  logout(): void {
    this.authService.logout();
  }
}