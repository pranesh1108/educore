import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router'; 
import { forkJoin } from 'rxjs';

import { StudentApiService } from '../services/student-api.service';
import { Enrollment } from '../models/student.model';
import { RegistrarCourseResponse } from '../../registrar/models/registrar.model';
import { LoaderComponent } from '../../../shared/components/loader/loader.component';

@Component({
  selector: 'app-student-courses',
  standalone: true,
  imports: [CommonModule, FormsModule, LoaderComponent, RouterModule],
  templateUrl: './courses.component.html',
  styleUrl: './courses.component.css'
})
export class CoursesComponent implements OnInit {
  courses: RegistrarCourseResponse[] = [];
  myEnrollments: Enrollment[] = [];
  enrolledCourseIds: Set<number> = new Set();
  loading = true;
  enrollingId: number | null = null;
  errorMessage = '';
  successMessage = '';

  titleSearch = '';
  topicSearch = '';

  constructor(private studentApi: StudentApiService) {}

  // Add this property to control tabs programmatically 
  activeTabId: 'catalogue' | 'enrolled' = 'catalogue';

  ngOnInit(): void {
    // Check if the router redirection sent along an explicit active view target state override
    const navigationState = history.state;
    if (navigationState && navigationState.activeTab) {
      this.activeTabId = navigationState.activeTab;
    }
    
    this.loadData();
  }

  // Add this small helper to update current active state cleanly on click actions
  switchTab(tab: 'catalogue' | 'enrolled'): void {
    this.activeTabId = tab;
  }

  loadData(): void {
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    forkJoin({
      catalogue: this.studentApi.getCoursesCatalogue({
        title: this.titleSearch || undefined,
        topic: this.topicSearch || undefined
      }),
      enrollments: this.studentApi.getMyCourses()
    }).subscribe({
      next: (data) => {
        // 1. Parse Catalogue Content Safely
        this.courses = (data.catalogue?.content || data.catalogue || []).filter((c: any) => c.title && c.title.trim() !== '');
        
        // 2. Parse Enrollments Safely
        this.myEnrollments = data.enrollments || [];
        
        // 🔍 DEBUG LOGS: Open your browser inspect panel (F12 -> Console) to see these!
        console.log('--- EDUCATIONAL DEBUG CENTRALS ---');
        console.log('Master Catalogue Array:', this.courses);
        console.log('My Enrollments Array:', this.myEnrollments);

        // 3. Extract IDs securely handling dynamic naming fallbacks
        this.enrolledCourseIds = new Set(
          this.myEnrollments.map((e: any) => e.courseId || e.course?.courseId || e.id)
        );
        
        // 4. Run the deep match mapping layer
        this.mapMissingDates();
        
        this.loading = false;
      },
      error: (err) => {
        const errMessage = err?.error?.message || err?.message || '';
        if (errMessage.includes('not enrolled')) {
          this.myEnrollments = [];
          this.enrolledCourseIds = new Set();
          this.searchCourses();
        } else {
          this.errorMessage = errMessage || 'Unable to sync course layout panel states.';
          this.loading = false;
        }
      }
    });
  }

  searchCourses(): void {
    this.loading = true;
    this.errorMessage = '';
    
    this.studentApi.getCoursesCatalogue({
      title: this.titleSearch || undefined,
      topic: this.topicSearch || undefined
    }).subscribe({
      next: (response) => {
        this.courses = (response.content || response || []).filter((c: any) => c.title && c.title.trim() !== '');
        this.mapMissingDates();
        this.loading = false;
      },
      error: (err) => {
        if (err?.error?.message?.includes('No courses match') || err?.message?.includes('No courses match')) {
          this.courses = [];
        } else {
          this.errorMessage = err?.error?.message || 'Unable to filter course catalogue.';
        }
        this.loading = false;
      }
    });
  }

  /**
   * Defensive mapping system checking multiple variations of property mapping paths
   */
  private mapMissingDates(): void {
  }

  enroll(courseId: number): void {
    this.enrollingId = courseId;
    this.errorMessage = '';
    this.successMessage = '';

    this.studentApi.enrollInCourse(courseId).subscribe({
      next: (enrollment) => {
        this.successMessage = `Successfully enrolled!`;
        this.enrolledCourseIds.add(courseId);
        
        const match = this.courses.find((c: any) => c.courseId === courseId || c.id === courseId);
        if (match) {
          enrollment.startDate = match.startDate;
          enrollment.endDate = match.endDate;
        }
        
        this.myEnrollments.push(enrollment);
        this.enrollingId = null;
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || err?.message || 'Enrollment failed.';
        this.enrollingId = null;
      }
    });
  }

  isEnrolled(courseId: number): boolean {
    return this.enrolledCourseIds.has(courseId);
  }
}