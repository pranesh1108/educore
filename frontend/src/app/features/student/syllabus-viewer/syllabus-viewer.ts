import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser'; 
import { StudentApiService } from '../services/student-api.service';

@Component({
  selector: 'app-syllabus-viewer',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './syllabus-viewer.html'
})
export class SyllabusViewerComponent implements OnInit {
  courseId!: number;
  syllabusPdfUrl: SafeResourceUrl | null = null;
  pdfLoading = true;
  pdfError = false;

  constructor(
    private route: ActivatedRoute,
    private studentApi: StudentApiService,
    private sanitizer: DomSanitizer
  ) {}

  ngOnInit(): void {
    this.courseId = Number(this.route.snapshot.paramMap.get('id'));
    this.fetchSecureSyllabus();
  }

  fetchSecureSyllabus(): void {
    this.pdfLoading = true;
    this.studentApi.getCourseSyllabus(this.courseId).subscribe({
      next: (blob: Blob) => {
        const objectUrl = URL.createObjectURL(blob);
        this.syllabusPdfUrl = this.sanitizer.bypassSecurityTrustResourceUrl(objectUrl);
        this.pdfLoading = false;
      },
      error: (err) => {
        console.error('Failed to load syllabus PDF', err);
        this.pdfError = true;
        this.pdfLoading = false;
      }
    });
  }
}