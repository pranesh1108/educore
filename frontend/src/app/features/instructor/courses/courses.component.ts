import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

import { InstructorApiService } from '../services/instructor-api.service';
import { InstructorCourse } from '../models/instructor.model';
import { Enrollment } from '../../student/models/student.model';
import { LoaderComponent } from '../../../shared/components/loader/loader.component';

@Component({
  selector: 'app-instructor-courses',
  standalone: true,
  imports: [CommonModule, LoaderComponent],
  templateUrl: './courses.component.html',
  styleUrl: './courses.component.css'
})
export class CoursesComponent implements OnInit {
  courses: InstructorCourse[] = [];
  selectedCourse: InstructorCourse | null = null;
  enrolledStudents: Enrollment[] = [];
  
  loading = true;
  studentsLoading = false;
  errorMessage = '';

  constructor(private instructorApi: InstructorApiService) {}

  ngOnInit(): void {
    this.loadCourses();
  }

  loadCourses(): void {
    this.loading = true;
    this.errorMessage = '';

    this.instructorApi.getAssignedCourses().subscribe({
      next: (courses) => {
        this.courses = courses || [];
        if (this.courses.length > 0) {
          this.selectCourse(this.courses[0]);
        } else {
          this.loading = false;
        }
      },
      error: (err) => {
        const errorMsg = err?.error?.message || err?.message || '';

        // ── SUPPRESS EMPTY COURSE ERROR BANNER ──
        if (errorMsg.includes('No courses assigned') || err?.status === 404) {
          this.courses = [];
          this.errorMessage = ''; // Keeps the banner invisible, letting your template's empty state handle it
        } else {
          this.errorMessage = errorMsg || 'Failed to load assigned courses.';
        }
        this.loading = false;
      }
    });
  }

  selectCourse(course: InstructorCourse): void {
    this.selectedCourse = course;
    this.studentsLoading = true;
    this.enrolledStudents = [];
    this.errorMessage = '';

    this.instructorApi.getEnrolledStudents(course.courseId).subscribe({
      next: (students) => {
        this.enrolledStudents = students || [];
        this.studentsLoading = false;
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || err?.message || 'Failed to load enrolled students list.';
        this.studentsLoading = false;
        this.loading = false;
      }
    });
  }
}