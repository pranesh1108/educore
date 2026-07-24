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
  activeTabId: 'catalogue' | 'enrolled' = 'catalogue';

  constructor(private studentApi: StudentApiService) {}

  // Getter: Returns ONLY courses that the student has NOT enrolled in yet
  get availableCatalogueCourses(): RegistrarCourseResponse[] {
    return this.courses.filter(c => !this.isEnrolled(c.courseId));
  }

  ngOnInit(): void {
    const navigationState = history.state;
    if (navigationState && navigationState.activeTab) {
      this.activeTabId = navigationState.activeTab;
    }
    this.loadData();
  }

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
        this.courses = (data.catalogue?.content || data.catalogue || []).filter((c: any) => c.title && c.title.trim() !== '');
        this.myEnrollments = data.enrollments || [];

        this.enrolledCourseIds = new Set(
          this.myEnrollments.map((e: any) => e.courseId || e.course?.courseId || e.id)
        );

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

  isEnrolled(courseId: number): boolean {
    return this.enrolledCourseIds.has(courseId);
  }
}