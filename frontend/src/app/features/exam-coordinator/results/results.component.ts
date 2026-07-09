import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { CoordinatorApiService } from '../services/coordinator-api.service';
import { Exam } from '../../student/models/student.model';
import { ExamRoomAllocationStudent } from '../models/coordinator.model';
import { LoaderComponent } from '../../../shared/components/loader/loader.component';

@Component({
  selector: 'app-coordinator-results',
  standalone: true,
  imports: [CommonModule, FormsModule, LoaderComponent],
  templateUrl: './results.component.html',
  styleUrl: './results.component.css'
})
export class ResultsComponent implements OnInit {
  exams: Exam[] = [];
  allocatedStudents: ExamRoomAllocationStudent[] = [];

  loading = true;
  submitting = false;
  studentsLoading = false;
  errorMessage = '';
  successMessage = '';

  // Form Fields - Publish Result
  selectedExamId: number | null = null;
  selectedStudentId: number | null = null;
  score: number = 0;

  constructor(private coordinatorApi: CoordinatorApiService) {}

  ngOnInit(): void {
    this.loadExams();
  }

  loadExams(): void {
    this.loading = true;
    this.errorMessage = '';

    this.coordinatorApi.searchExams().subscribe({
      next: (exams) => {
        // Changed: Removed status filter check because status property does not exist on type Exam
        this.exams = exams || [];
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || err?.message || 'Failed to load exams list.';
        this.loading = false;
      }
    });
  }

  onExamChange(): void {
    if (!this.selectedExamId) {
      this.allocatedStudents = [];
      this.selectedStudentId = null;
      return;
    }

    this.studentsLoading = true;
    this.allocatedStudents = [];
    this.selectedStudentId = null;
    this.errorMessage = '';

    // Fetch all students enrolled in the exam's course directly
    this.coordinatorApi.getEnrolledStudentsForExam(this.selectedExamId).subscribe({
      next: (students) => {
        this.allocatedStudents = students || [];
        this.studentsLoading = false;
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || 'Failed to load student roster for the selected exam.';
        this.studentsLoading = false;
      }
    });
  }

  publishResult(): void {
    if (!this.selectedExamId || !this.selectedStudentId) return;

    const selectedExam = this.exams.find(e => e.examId === this.selectedExamId);
    if (!selectedExam) {
      this.errorMessage = 'Selected exam not found.';
      return;
    }

    this.submitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.coordinatorApi.publishResult({
      examId: this.selectedExamId,
      studentId: this.selectedStudentId,
      courseId: selectedExam.courseId,
      score: this.score
    }).subscribe({
      next: (result) => {
        const studentName = this.allocatedStudents.find(s => s.studentId === this.selectedStudentId)?.studentName || 'Student';
        this.successMessage = `Successfully published result for "${studentName}" — Score: ${result.score}% (${result.result})`;
        this.score = 0;
        this.selectedStudentId = null;
        this.submitting = false;

        // Reload exams list
        this.loadExams();
        // Clear students list too
        this.allocatedStudents = [];
        this.selectedExamId = null;
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || err?.message || 'Failed to publish exam results.';
        this.submitting = false;
      }
    });
  }
}