import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

import { InstructorApiService } from '../services/instructor-api.service';
import { Exam } from '../../student/models/student.model';
import { LoaderComponent } from '../../../shared/components/loader/loader.component';

@Component({
  selector: 'app-instructor-exams',
  standalone: true,
  imports: [CommonModule, LoaderComponent],
  templateUrl: './exams.component.html',
  styleUrl: './exams.component.css'
})
export class ExamsComponent implements OnInit {
  exams: Exam[] = [];
  loading = true;
  errorMessage = '';

  constructor(private instructorApi: InstructorApiService) {}

  ngOnInit(): void {
    this.loading = true;
    this.errorMessage = '';

    this.instructorApi.getMyExams().subscribe({
      next: (exams) => {
        this.exams = exams;
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || err?.message || 'Failed to load exams schedule.';
        this.loading = false;
      }
    });
  }
}
