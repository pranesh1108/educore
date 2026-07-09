import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { StudentApiService } from '../services/student-api.service';
import { CourseContent, Enrollment, Submission } from '../models/student.model';
import { LoaderComponent } from '../../../shared/components/loader/loader.component';

@Component({
  selector: 'app-student-assignments',
  standalone: true,
  imports: [CommonModule, FormsModule, LoaderComponent],
  templateUrl: './assignments.component.html',
  styleUrl: './assignments.component.css'
})
export class AssignmentsComponent implements OnInit {
  myEnrollments: Enrollment[] = [];
  selectedEnrollment: Enrollment | null = null;
  courseContent: CourseContent | null = null;
  mySubmissions: Submission[] = [];
  
  loading = true;
  contentLoading = false;
  submittingId: number | null = null;
  errorMessage = '';
  successMessage = '';

  constructor(private studentApi: StudentApiService) {}

  ngOnInit(): void {
    this.loadEnrollmentsAndSubmissions();
  }

  loadEnrollmentsAndSubmissions(): void {
    this.loading = true;
    this.errorMessage = '';

    this.studentApi.getMyCourses().subscribe({
      next: (enrollments) => {
        this.myEnrollments = enrollments || [];
        if (enrollments.length > 0) {
          this.selectCourse(enrollments[0]);
        } else {
          this.loading = false;
        }
      },
      error: (err) => {
        const errorMsg = err?.error?.message || err?.message || '';

        // ── FIX: Intercept empty enrollment notifications and suppress banner ──
        if (errorMsg.includes('not enrolled') || err?.status === 404) {
          this.myEnrollments = [];
          this.errorMessage = ''; // Keeps the banner invisible
        } else {
          this.errorMessage = errorMsg || 'Failed to load enrolled courses.';
        }
        this.loading = false;
      }
    });
  }

  selectCourse(enrol: Enrollment): void {
    this.selectedEnrollment = enrol;
    this.contentLoading = true;
    this.courseContent = null;
    this.errorMessage = '';
    this.successMessage = '';

    this.studentApi.getCourseContent(enrol.courseId).subscribe({
      next: (content) => {
        this.courseContent = content;
        this.loadSubmissions();
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || err?.message || 'Failed to load course content.';
        this.contentLoading = false;
        this.loading = false;
      }
    });
  }

  loadSubmissions(): void {
    this.studentApi.getMySubmissions().subscribe({
      next: (submissions) => {
        this.mySubmissions = submissions;
        this.contentLoading = false;
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || err?.message || 'Failed to load submissions.';
        this.contentLoading = false;
        this.loading = false;
      }
    });
  }

  downloadMaterial(fileId: number, name: string): void {
    this.studentApi.downloadMaterial(fileId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = name;
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: (err) => {
        this.errorMessage = 'Failed to download material file.';
      }
    });
  }

  downloadAssignment(fileId: number, name: string): void {
    this.studentApi.downloadAssignmentFile(fileId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = name;
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: (err) => {
        this.errorMessage = 'Failed to download assignment file.';
      }
    });
  }

  handleFileUpload(event: any, assignmentId: number): void {
    const fileList: FileList = event.target.files;
    if (fileList.length === 0) return;
    
    const file = fileList[0];
    if (file.type !== 'application/pdf') {
      this.errorMessage = 'Only PDF submissions are allowed.';
      return;
    }

    this.submittingId = assignmentId;
    this.errorMessage = '';
    this.successMessage = '';

    this.studentApi.submitAssignment(assignmentId, file).subscribe({
      next: (submission) => {
        this.successMessage = `Successfully submitted your solution: "${submission.fileName}"!`;
        this.loadSubmissions();
        this.submittingId = null;
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || err?.message || 'Submission failed. Please check file size constraints.';
        this.submittingId = null;
      }
    });
  }

  getSubmissionForAssignment(assignmentId: number): Submission | undefined {
    return this.mySubmissions.find(s => s.assignmentId === assignmentId);
  }
}