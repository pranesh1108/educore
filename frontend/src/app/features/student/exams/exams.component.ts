import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

import { StudentApiService } from '../services/student-api.service';
import { Exam } from '../models/student.model';
import { LoaderComponent } from '../../../shared/components/loader/loader.component';

@Component({
  selector: 'app-student-exams',
  standalone: true,
  imports: [CommonModule, LoaderComponent],
  templateUrl: './exams.component.html',
  styleUrl: './exams.component.css'
})
export class ExamsComponent implements OnInit {
  exams: Exam[] = [];
  loading = true;
  errorMessage = '';

  constructor(private studentApi: StudentApiService) {}

  ngOnInit(): void {
    this.loadExams();
  }

  loadExams(): void {
    this.loading = true;
    this.errorMessage = '';

    this.studentApi.getMyExams().subscribe({
      next: (exams) => {
        this.exams = exams || [];
        this.loading = false;
      },
      error: (err) => {
        const errorMsg = err?.error?.message || err?.message || '';

        // Catch empty exam exceptions and suppress the red alert banner
        if (errorMsg.includes('No exams found') || err?.status === 404) {
          this.exams = [];
          this.errorMessage = ''; // Force the banner to hide
        } else {
          // Keep actual system infrastructure errors visible
          this.errorMessage = errorMsg || 'Failed to load exams list.';
        }
        this.loading = false;
      }
    });
  }
}