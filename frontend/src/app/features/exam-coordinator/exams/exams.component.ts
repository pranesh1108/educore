import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { CoordinatorApiService } from '../services/coordinator-api.service';
import { StudentApiService } from '../../student/services/student-api.service';
import { Exam } from '../../student/models/student.model';
import { InstructorFilterOutput, RegistrarCourseResponse } from '../../registrar/models/registrar.model';
import { LoaderComponent } from '../../../shared/components/loader/loader.component';

@Component({
  selector: 'app-coordinator-exams',
  standalone: true,
  imports: [CommonModule, FormsModule, LoaderComponent],
  templateUrl: './exams.component.html',
  styleUrl: './exams.component.css'
})
export class ExamsComponent implements OnInit {
  exams: Exam[] = [];
  instructors: InstructorFilterOutput[] = [];
  courses: RegistrarCourseResponse[] = [];

  loading = true;
  submitting = false;
  errorMessage = '';
  successMessage = '';

  // Form Fields - Create Exam
  title = '';
  description = '';
  examDateInput = ''; // yyyy-MM-ddTHH:mm
  durationMinutes = 180;
  totalMarks = 100;
  passingMarks = 40;
  selectedCourseId: number | null = null;
  selectedInstructorId: number | null = null;

  // Search Filters
  filterCourseId: number | null = null;
  filterInstructorId: number | null = null;
  filterStatus = '';

  constructor(
    private coordinatorApi: CoordinatorApiService,
    private studentApi: StudentApiService
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.errorMessage = '';

    this.coordinatorApi.getAllInstructors().subscribe({
      next: (instructors) => {
        this.instructors = instructors;
        
        // Fetch all courses from the shared catalog
        this.studentApi.getCoursesCatalogue().subscribe({
          next: (coursesPage) => {
            this.courses = coursesPage.content || [];
            this.searchExams();
          },
          error: (err) => {
            const courseErrorMsg = err?.error?.message || err?.message || '';
            
            // ── INTERCEPT THE NO COURSES MATCH ERROR BANNER ──
            if (courseErrorMsg.includes('No courses match') || err?.status === 404) {
              this.courses = [];
              this.errorMessage = ''; // Suppress the error banner completely
              this.searchExams();      // Continue loading exams safely
            } else {
              this.errorMessage = courseErrorMsg || 'Failed to load course list.';
              this.loading = false;
            }
          }
        });
      },
      error: (err) => {
        this.errorMessage = err?.message || 'Failed to load instructors list.';
        this.loading = false;
      }
    });
  }
  
  searchExams(): void {
    this.errorMessage = '';
    this.coordinatorApi.searchExams({
      courseId: this.filterCourseId || undefined,
      instructorId: this.filterInstructorId || undefined
    }).subscribe({
      next: (exams) => {
        this.exams = exams;
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || err?.message || 'Failed to load exams list.';
        this.loading = false;
      }
    });
  }

  createExam(): void {
    if (!this.selectedCourseId || !this.selectedInstructorId || !this.examDateInput) return;

    this.submitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    // Format local datetime input "yyyy-MM-ddTHH:mm" -> "yyyy-MM-dd HH:mm"
    const formattedDate = this.examDateInput.replace('T', ' ');

    this.coordinatorApi.createExam({
      title: this.title,
      description: this.description || undefined,
      examDate: formattedDate,
      durationMinutes: this.durationMinutes,
      totalMarks: this.totalMarks,
      passingMarks: this.passingMarks,
      courseId: this.selectedCourseId,
      instructorId: this.selectedInstructorId
    }).subscribe({
      next: (newExam) => {
        this.successMessage = `Successfully created exam track: "${newExam.title}"!`;
        this.title = '';
        this.description = '';
        this.examDateInput = '';
        this.selectedCourseId = null;
        this.selectedInstructorId = null;
        this.searchExams();
        this.submitting = false;
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || err?.message || 'Failed to create exam track.';
        this.submitting = false;
      }
    });
  }

  deleteExam(examId: number): void {
    if (!confirm('Are you sure you want to delete this exam track?')) return;

    this.errorMessage = '';
    this.successMessage = '';

    this.coordinatorApi.deleteExam(examId).subscribe({
      next: () => {
        this.successMessage = 'Exam configuration record deleted successfully.';
        this.searchExams();
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || err?.message || 'Failed to delete exam. Note: Exams cannot be deleted if less than 24 hours remain.';
      }
    });
  }
}