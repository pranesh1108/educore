import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router'; 

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

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.studentApi.getMyCourses().subscribe({
      next: (enrollments) => {
        this.myEnrollments = enrollments || [];
        this.enrolledCourseIds = new Set(this.myEnrollments.map(e => e.courseId));
        this.searchCourses();
      },
      error: (err) => {
        if (err?.error?.message?.includes('not enrolled') || err?.message?.includes('not enrolled')) {
          this.myEnrollments = [];
          this.enrolledCourseIds = new Set();
          this.searchCourses();
        } else {
          this.errorMessage = err?.error?.message || err?.message || 'Unable to load your courses.';
          this.loading = false;
        }
      }
    });
  }

  searchCourses(): void {
    this.errorMessage = '';
    this.studentApi.getCoursesCatalogue({
      title: this.titleSearch || undefined,
      topic: this.topicSearch || undefined
    }).subscribe({
      next: (response) => {
        this.courses = (response.content || []).filter((c: any) => c.title && c.title.trim() !== '');
        this.loading = false;
      },
      error: (err) => {
        if (err?.error?.message?.includes('No courses match') || err?.message?.includes('No courses match')) {
          this.courses = [];
          this.errorMessage = '';
        } else {
          this.errorMessage = err?.error?.message || err?.message || 'Unable to load course catalogue.';
        }
        this.loading = false;
      }
    });
  }

  enroll(courseId: number): void {
    this.enrollingId = courseId;
    this.errorMessage = '';
    this.successMessage = '';

    this.studentApi.enrollInCourse(courseId).subscribe({
      next: (enrollment) => {
        this.successMessage = `Successfully enrolled in "${enrollment.courseTitle}"!`;
        this.enrolledCourseIds.add(courseId);
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