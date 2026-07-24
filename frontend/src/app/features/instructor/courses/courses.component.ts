import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { InstructorApiService } from '../services/instructor-api.service';
import { InstructorCourse } from '../models/instructor.model';
import { Enrollment } from '../../student/models/student.model';
import { LoaderComponent } from '../../../shared/components/loader/loader.component';

@Component({
  selector: 'app-instructor-courses',
  standalone: true,
  imports: [CommonModule, FormsModule, LoaderComponent, RouterModule],
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
        if (errorMsg.includes('No courses assigned') || err?.status === 404) {
          this.courses = [];
          this.errorMessage = '';
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
        const subErrorMsg = err?.error?.message || err?.message || '';
        if (err?.status === 404 || subErrorMsg.includes('No details available') || subErrorMsg.includes('not found')) {
          this.enrolledStudents = [];
          this.errorMessage = '';
        } else {
          this.errorMessage = subErrorMsg || 'Failed to load enrolled students list.';
        }
        this.studentsLoading = false;
        this.loading = false;
      }
    });
  }

  onCourseChange(event: Event): void {
    const selectElem = event.target as HTMLSelectElement;
    const courseId = Number(selectElem.value);
    const found = this.courses.find(c => c.courseId === courseId);
    if (found) {
      this.selectCourse(found);
    }
  }
}