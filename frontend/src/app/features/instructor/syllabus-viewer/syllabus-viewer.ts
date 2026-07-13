import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser'; 
import { InstructorApiService } from '../services/instructor-api.service'; // Adjust path based on your service structure

@Component({
  selector: 'app-instructor-syllabus-viewer',
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
    private instructorApi: InstructorApiService,
    private sanitizer: DomSanitizer
  ) {}

  ngOnInit(): void {
    this.courseId = Number(this.route.snapshot.paramMap.get('id'));
    this.fetchSecureSyllabus();
  }

  fetchSecureSyllabus(): void {
    this.pdfLoading = true;
    // Interceptor attaches the Instructor JWT Token natively to download the PDF blob
    this.instructorApi.getCourseSyllabus(this.courseId).subscribe({
      next: (blob: Blob) => {
        const objectUrl = URL.createObjectURL(blob);
        this.syllabusPdfUrl = this.sanitizer.bypassSecurityTrustResourceUrl(objectUrl);
        this.pdfLoading = false;
      },
      error: (err) => {
        console.error('Failed to stream syllabus asset', err);
        this.pdfError = true;
        this.pdfLoading = false;
      }
    });
  }
}