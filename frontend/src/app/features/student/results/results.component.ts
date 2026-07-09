import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

import { StudentApiService } from '../services/student-api.service';
import { ExamResult } from '../models/student.model';
import { LoaderComponent } from '../../../shared/components/loader/loader.component';

@Component({
  selector: 'app-student-results',
  standalone: true,
  imports: [CommonModule, LoaderComponent],
  templateUrl: './results.component.html',
  styleUrl: './results.component.css'
})
export class ResultsComponent implements OnInit {
  results: ExamResult[] = [];
  loading = true;
  errorMessage = '';

  constructor(private studentApi: StudentApiService) {}

  ngOnInit(): void {
    this.loading = true;
    this.errorMessage = '';

    this.studentApi.getProfile().subscribe({
      next: (profile) => {
        const studentId = profile.studentId;
        this.studentApi.getMyResults(studentId).subscribe({
          next: (results) => {
            this.results = results || [];
            this.loading = false;
          },
          error: (err) => {
            const errorMsg = err?.error?.message || err?.message || '';

            // ── FIX: Intercept empty score records and suppress the error banner ──
            if (errorMsg.includes('No exam results') || err?.status === 404) {
              this.results = [];
              this.errorMessage = ''; // Force the red banner to hide
            } else {
              this.errorMessage = errorMsg || 'Failed to load results scorecard.';
            }
            this.loading = false;
          }
        });
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || err?.message || 'Failed to load student profile.';
        this.loading = false;
      }
    });
  }
}