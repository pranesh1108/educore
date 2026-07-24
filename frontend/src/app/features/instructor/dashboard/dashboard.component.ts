import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

import { InstructorApiService } from '../services/instructor-api.service';
import { InstructorProfile } from '../models/instructor.model';
import { LoaderComponent } from '../../../shared/components/loader/loader.component';

@Component({
  selector: 'app-instructor-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, LoaderComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  profile: InstructorProfile | null = null;
  coursesCount = 0;
  examsCount = 0;
  loading = true;
  errorMessage = '';

  constructor(private instructorApi: InstructorApiService) {}

  ngOnInit(): void {
    this.loadData();
  }

  private loadData(): void {
    this.loading = true;
    this.errorMessage = '';

    this.instructorApi.getProfile().subscribe({
      next: (profile) => {
        this.profile = profile;

        this.instructorApi.getAssignedCourses().subscribe({
          next: (courses) => {
            this.coursesCount = courses ? courses.length : 0;
            this.fetchExams();
          },
          error: (err) => {
            const errorMsg = err?.error?.message || err?.message || '';

            if (errorMsg.includes('No courses assigned') || err?.status === 404) {
              this.coursesCount = 0;
              this.fetchExams();
            } else {
              this.errorMessage = errorMsg || 'Unable to load assigned courses.';
              this.loading = false;
            }
          }
        });
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || err?.message || 'Unable to load instructor profile.';
        this.loading = false;
      }
    });
  }

  // Extracted helper method to keep the nesting cleaner and readable
  private fetchExams(): void {
    this.instructorApi.getMyExams().subscribe({
      next: (exams) => {
        this.examsCount = exams ? exams.length : 0;
        this.loading = false;
      },
      error: (err) => {
        const errorMsg = err?.error?.message || err?.message || '';
        
        // Handle fallback empty checks for the exams endpoint similarly if needed
        if (errorMsg.includes('No exams assigned') || err?.status === 404) {
          this.examsCount = 0;
        } else {
          this.errorMessage = errorMsg || 'Unable to load exams list.';
        }
        this.loading = false;
      }
    });
  }
}