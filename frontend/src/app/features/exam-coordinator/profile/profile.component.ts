import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-coordinator-profile',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit {
  userName = '';
  userEmail = '';
  userRole = '';

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    const user = this.authService.getCurrentUser();
    if (user) {
      this.userName = user.userName;
      this.userEmail = user.email;
      this.userRole = user.role;
    }
  }

  get initials(): string {
    if (!this.userName) return '?';
    return this.userName
      .split(' ')
      .map(p => p.charAt(0))
      .join('')
      .slice(0, 2)
      .toUpperCase();
  }
}
