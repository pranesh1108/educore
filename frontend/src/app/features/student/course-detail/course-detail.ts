import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser'; 
import { StudentApiService } from '../services/student-api.service';
import { LoaderComponent } from '../../../shared/components/loader/loader.component';

@Component({
  selector: 'app-course-detail',
  standalone: true,
  // We don't need SafeUrlPipe here anymore!
  imports: [CommonModule, RouterModule, LoaderComponent],
  templateUrl: './course-detail.html',
  styleUrl: './course-detail.css'
})
export class CourseDetailComponent implements OnInit {
  courseId!: number;
  course: any = null;
  loading = true;
  submitting = false;
  errorMessage = '';
  successMessage = '';

  // Variables for the secure PDF
  syllabusPdfUrl: SafeResourceUrl | null = null;
  pdfLoading = false;
  pdfError = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private studentApi: StudentApiService,
    private sanitizer: DomSanitizer // Injected to make the PDF URL safe
  ) {}

  ngOnInit(): void {
    this.courseId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadCourseOverview();
  }

  loadCourseOverview(): void {
    this.loading = true;
    this.errorMessage = '';

    this.studentApi.getCoursesCatalogue({ title: undefined, topic: undefined }).subscribe({
      next: (response) => {
        const matchingCourse = (response.content || []).find((c: any) => c.courseId === this.courseId);
        
        if (matchingCourse) {
          this.course = matchingCourse;
          // If the backend says a syllabus exists, download it securely!
          if (this.course.syllabusPath) {
            this.fetchSecureSyllabus();
          }
        } else {
          this.errorMessage = 'The requested course track properties could not be resolved.';
        }
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || 'Unable to successfully parse backend catalogue structures.';
        this.loading = false;
      }
    });
  }

  // This downloads the PDF using your JWT Token, bypassing the 401 error
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

  registerAndEnroll(): void {
    this.submitting = true;
    this.errorMessage = '';
    
    this.studentApi.enrollInCourse(this.courseId).subscribe({
      next: () => {
        this.successMessage = 'Successfully registered! Loading student layout command center...';
        setTimeout(() => {
          this.router.navigate(['/student/courses']);
        }, 1500);
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || 'Enrollment transaction processing failed.';
        this.submitting = false;
      }
    });
  }
}