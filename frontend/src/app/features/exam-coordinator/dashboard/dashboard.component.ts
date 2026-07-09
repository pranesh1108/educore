import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

import { CoordinatorApiService } from '../services/coordinator-api.service';
import { AuthService } from '../../../core/services/auth.service';
import { LoaderComponent } from '../../../shared/components/loader/loader.component';

@Component({
  selector: 'app-coordinator-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, LoaderComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  userName = '';
  userEmail = '';
  examsCount = 0;
  roomsCount = 0;
  loading = true;
  errorMessage = '';

  constructor(
    private coordinatorApi: CoordinatorApiService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const user = this.authService.getCurrentUser();
    if (user) {
      this.userName = user.userName;
      this.userEmail = user.email;
    }
    this.loadStats();
  }

  private loadStats(): void {
    this.loading = true;
    this.errorMessage = '';

    this.coordinatorApi.searchExams().subscribe({
      next: (exams) => {
        this.examsCount = exams.length;
        this.coordinatorApi.getAllRooms().subscribe({
          next: (rooms) => {
            this.roomsCount = rooms.length;
            this.loading = false;
          },
          error: (err) => {
            this.errorMessage = err?.message || 'Failed to load physical rooms.';
            this.loading = false;
          }
        });
      },
      error: (err) => {
        this.errorMessage = err?.message || 'Failed to load exams list.';
        this.loading = false;
      }
    });
  }
}
