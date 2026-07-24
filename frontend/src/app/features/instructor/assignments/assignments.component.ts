import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { InstructorApiService } from '../services/instructor-api.service';
import { InstructorCourse, InstructorResourceResponse } from '../models/instructor.model';
import { LoaderComponent } from '../../../shared/components/loader/loader.component';

@Component({
  selector: 'app-instructor-assignments',
  standalone: true,
  imports: [CommonModule, FormsModule, LoaderComponent],
  templateUrl: './assignments.component.html',
  styleUrl: './assignments.component.css'
})
export class AssignmentsComponent implements OnInit {
  courses: InstructorCourse[] = [];
  selectedCourse: InstructorCourse | null = null;
  resources: InstructorResourceResponse | null = null;

  loading = true;
  submitting = false;
  resourcesLoading = false;
  errorMessage = '';
  
  materialSuccessMessage = signal('');
  assignmentSuccessMessage = signal('');

  // Form Fields - Material
  materialFile: File | null = null;
  materialTextContent = '';

  // Form Fields - Assignment
  assignmentFile: File | null = null;
  assignmentTitle = '';
  assignmentInstructions = '';
  assignmentTotalMarks = 100;
  assignmentDueDateInput = '';

  constructor(private instructorApi: InstructorApiService) {}

  ngOnInit(): void {
    this.loadCourses();
  }

  loadCourses(): void {
    this.loading = true;
    this.errorMessage = '';

    // Fetch both assigned courses AND published exams in parallel
    forkJoin({
      courses: this.instructorApi.getAssignedCourses().pipe(catchError(() => of([]))),
      exams: this.instructorApi.getMyExams().pipe(catchError(() => of([])))
    }).subscribe({
      next: ({ courses, exams }) => {
        const rawCourses = courses || [];
        const examCourseIds = new Set((exams || []).map(e => e.courseId));

        if (rawCourses.length === 0) {
          this.courses = [];
          this.selectedCourse = null;
          this.loading = false;
          return;
        }

        // Validate each course against Exam publication AND Resource Lock
        const resourceChecks = rawCourses.map(course => {
          // Check 1: If an exam has been published for this course, exclude it immediately
          if (examCourseIds.has(course.courseId)) {
            return of(null);
          }
          // Check 2: Verify resources are not locked/finalized
          return this.instructorApi.getCourseResources(course.courseId).pipe(
            catchError(() => of(null)) // Returns null if course is ended or scores published
          );
        });

        forkJoin(resourceChecks).subscribe({
          next: (results) => {
            // Filter and keep ONLY courses that passed both checks
            this.courses = rawCourses.filter((_, index) => results[index] !== null);

            if (this.courses.length > 0) {
              this.selectCourse(this.courses[0]);
            } else {
              this.selectedCourse = null;
            }
            this.loading = false;
          },
          error: () => {
            this.courses = [];
            this.selectedCourse = null;
            this.loading = false;
          }
        });
      },
      error: (err) => {
        const errorMsg = err?.error?.message || err?.message || '';
        if (errorMsg.includes('No courses assigned') || err?.status === 404) {
          this.courses = [];
          this.errorMessage = '';
        } else {
          this.errorMessage = errorMsg || 'Failed to load assigned tracks.';
        }
        this.loading = false;
      }
    });
  }

  selectCourse(course: InstructorCourse): void {
    if (!course) return;
    this.selectedCourse = course;
    this.resourcesLoading = true;
    this.resources = null;
    this.errorMessage = '';
    this.materialSuccessMessage.set('');
    this.assignmentSuccessMessage.set('');

    this.instructorApi.getCourseResources(course.courseId).subscribe({
      next: (res) => {
        this.resources = res;
        this.resourcesLoading = false;
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || err?.message || 'Failed to load course resources.';
        this.resourcesLoading = false;
        this.loading = false;
      }
    });
  }

  onMaterialFileSelected(event: any): void {
    this.materialFile = event.target.files[0] || null;
  }

  onAssignmentFileSelected(event: any): void {
    this.assignmentFile = event.target.files[0] || null;
  }

  publishMaterial(): void {
    if (!this.selectedCourse || !this.materialFile) return;

    this.submitting = true;
    this.errorMessage = '';
    this.materialSuccessMessage.set('');

    this.instructorApi.publishCourseMaterial(
      this.selectedCourse.courseId,
      this.materialFile,
      this.materialTextContent
    ).subscribe({
      next: () => {
        this.materialSuccessMessage.set('Handout document uploaded successfully!');
        this.materialFile = null;
        this.materialTextContent = '';
        const fileInput = document.getElementById('materialFile') as HTMLInputElement;
        if (fileInput) fileInput.value = '';
        
        this.selectCourse(this.selectedCourse!);
        this.submitting = false;

        setTimeout(() => {
          this.materialSuccessMessage.set('');
        }, 4000);
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || err?.message || 'Failed to publish material.';
        this.submitting = false;
      }
    });
  }

  publishAssignment(): void {
    if (!this.selectedCourse || !this.assignmentFile || !this.assignmentDueDateInput) return;

    this.submitting = true;
    this.errorMessage = '';
    this.assignmentSuccessMessage.set('');

    const formattedDueDate = this.assignmentDueDateInput.replace('T', ' ');

    this.instructorApi.publishAssignment(
      this.selectedCourse.courseId,
      this.assignmentTitle,
      this.assignmentInstructions,
      this.assignmentTotalMarks,
      formattedDueDate,
      this.assignmentFile
    ).subscribe({
      next: () => {
        this.assignmentSuccessMessage.set('Evaluation task published successfully!');
        this.assignmentFile = null;
        this.assignmentTitle = '';
        this.assignmentInstructions = '';
        this.assignmentTotalMarks = 100;
        this.assignmentDueDateInput = '';
        const fileInput = document.getElementById('assignmentFile') as HTMLInputElement;
        if (fileInput) fileInput.value = '';
        
        this.selectCourse(this.selectedCourse!);
        this.submitting = false;

        setTimeout(() => {
          this.assignmentSuccessMessage.set('');
        }, 4000);
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || err?.message || 'Failed to publish assignment.';
        this.submitting = false;
      }
    });
  }
}