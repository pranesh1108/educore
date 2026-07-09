import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { InstructorApiService } from '../services/instructor-api.service';
import { InstructorCourse } from '../models/instructor.model';
import { Submission } from '../../student/models/student.model';
import { LoaderComponent } from '../../../shared/components/loader/loader.component';

@Component({
  selector: 'app-instructor-grading',
  standalone: true,
  imports: [CommonModule, FormsModule, LoaderComponent],
  templateUrl: './grading.component.html',
  styleUrl: './grading.component.css'
})
export class GradingComponent implements OnInit {
  courses: InstructorCourse[] = [];
  selectedCourse: InstructorCourse | null = null;
  submissions: Submission[] = [];

  loading = true;
  submissionsLoading = false;
  gradingSubmission: Submission | null = null;
  savingGrade = false;
  errorMessage = '';
  successMessage = '';

  // Form Fields - Grading
  scoreInput = 0;
  feedbackInput = '';

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

        // ── FIX: Suppress empty assigned course error banner on page init ──
        if (errorMsg.includes('No courses assigned') || err?.status === 404) {
          this.courses = [];
          this.errorMessage = ''; // Force banner to stay invisible
        } else {
          this.errorMessage = errorMsg || 'Failed to load assigned courses.';
        }
        this.loading = false;
      }
    });
  }

  selectCourse(course: InstructorCourse): void {
    this.selectedCourse = course;
    this.submissionsLoading = true;
    this.submissions = [];
    this.gradingSubmission = null;
    this.errorMessage = '';
    this.successMessage = '';

    this.instructorApi.getSubmissions(course.courseId).subscribe({
      next: (subs) => {
        this.submissions = subs || [];
        this.submissionsLoading = false;
        this.loading = false;
      },
      error: (err) => {
        const errorMsg = err?.error?.message || err?.message || '';
        
        if (errorMsg.includes('No submissions found') || err?.status === 404) {
          this.submissions = []; 
          this.errorMessage = ''; 
        } else {
          this.errorMessage = errorMsg || 'Failed to load student submissions.';
        }
        
        this.submissionsLoading = false;
        this.loading = false;
      }
    });
  }

  downloadSubmission(subId: number, name: string): void {
    this.errorMessage = '';
    this.instructorApi.downloadSubmissionFile(subId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = name;
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: (err) => {
        this.errorMessage = 'Failed to download student solution file.';
      }
    });
  }

  openGradingForm(sub: Submission): void {
    this.gradingSubmission = sub;
    this.scoreInput = sub.grade || 0;
    this.feedbackInput = sub.feedback || '';
    this.errorMessage = '';
    this.successMessage = '';
  }

  closeGradingForm(): void {
    this.gradingSubmission = null;
  }

  submitGrade(): void {
    if (!this.gradingSubmission) return;

    this.savingGrade = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.instructorApi.gradeSubmission(this.gradingSubmission.submissionId, {
      grade: this.scoreInput,
      feedback: this.feedbackInput
    }).subscribe({
      next: (updatedSub) => {
        this.successMessage = `Successfully graded submission for ${updatedSub.studentName}!`;
        const idx = this.submissions.findIndex(s => s.submissionId === updatedSub.submissionId);
        if (idx !== -1) {
          this.submissions[idx] = updatedSub;
        }
        this.gradingSubmission = null;
        this.savingGrade = false;
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || err?.message || 'Failed to submit grade.';
        this.savingGrade = false;
      }
    });
  }
}