import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

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

    this.instructorApi.getAssignedCourses().subscribe({
      next: (courses) => {
        this.courses = courses || [];
        this.loading = false;
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
        // 1. Trigger System UI Alert Window Fallback
        alert('Success: material is published!');

        // 2. Set Signal state values
        this.materialSuccessMessage.set('material is published');
        
        this.materialFile = null;
        this.materialTextContent = '';
        const fileInput = document.getElementById('materialFile') as HTMLInputElement;
        if (fileInput) fileInput.value = '';
        
        this.selectCourse(this.selectedCourse!);
        this.submitting = false;

        setTimeout(() => {
          this.materialSuccessMessage.set('');
        }, 3000);
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
        // 1. Trigger System UI Alert Window Fallback
        alert('Success: assignment is published!');

        // 2. Set Signal state values
        this.assignmentSuccessMessage.set('assignment is published');
        
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
        }, 3000);
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || err?.message || 'Failed to publish assignment.';
        this.submitting = false;
      }
    });
  }
}