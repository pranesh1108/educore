import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

import { StudentApiService } from '../services/student-api.service';
import { StudentProfile } from '../models/student.model';
import { LoaderComponent } from '../../../shared/components/loader/loader.component';

@Component({
  selector: 'app-student-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, LoaderComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  profile: StudentProfile | null = null;
  enrolledCount = 0;
  loading = true;
  errorMessage = '';

  constructor(private studentApi: StudentApiService) {}

  ngOnInit(): void {
    this.loadProfileAndEnrolledCourses();
  }

  private loadProfileAndEnrolledCourses(): void {
    this.loading = true;
    this.errorMessage = '';

    this.studentApi.getProfile().subscribe({
      next: (profile) => {
        this.profile = profile;
        this.studentApi.getMyCourses().subscribe({
          next: (courses) => {
            this.enrolledCount = courses ? courses.length : 0;
            this.loading = false;
          },
          error: (err) => {
            const errorMsg = err?.error?.message || err?.message || '';

            // ── FIX: Intercept empty enrollment notifications and suppress banner ──
            if (errorMsg.includes('not enrolled') || err?.status === 404) {
              this.enrolledCount = 0;
              this.errorMessage = ''; // Keeps the banner invisible
            } else {
              this.errorMessage = errorMsg || 'Unable to load your enrolled courses.';
            }
            this.loading = false;
          }
        });
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || err?.message || 'Unable to load your student profile.';
        this.loading = false;
      }
    });
  }
}