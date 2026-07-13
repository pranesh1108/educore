import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser'; 
import { StudentApiService } from '../services/student-api.service';
import { LoaderComponent } from '../../../shared/components/loader/loader.component';
import { forkJoin } from 'rxjs'; 

@Component({
  selector: 'app-course-detail',
  standalone: true,
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

  syllabusPdfUrl: SafeResourceUrl | null = null;
  pdfLoading = false;
  pdfError = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private studentApi: StudentApiService,
    private sanitizer: DomSanitizer
  ) {}

  ngOnInit(): void {
    this.courseId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadCourseOverview();
  }

  loadCourseOverview(): void {
    this.loading = true;
    this.errorMessage = '';

    // ── USE forkJoin TO FETCH CATALOGUE AND ENROLLMENTS TOGETHER ──
    forkJoin({
      catalogue: this.studentApi.getCoursesCatalogue({ title: undefined, topic: undefined }),
      myCourses: this.studentApi.getMyCourses()
    }).subscribe({
      next: (data) => {
        const matchingCourse = (data.catalogue?.content || []).find((c: any) => c.courseId === this.courseId);
        
        if (matchingCourse) {
          this.course = matchingCourse;
          
          if (this.course.syllabusPath) {
            this.fetchSecureSyllabus();
          }

          // 🔍 CHECK IF ALREADY ENROLLED
          const userEnrollments = data.myCourses || [];
          const alreadyEnrolled = userEnrollments.some((e: any) => 
            (e.courseId === this.courseId) || (e.course?.courseId === this.courseId)
          );

          if (alreadyEnrolled) {
            // Set success state so template hides/disables the button instantly
            this.successMessage = 'Enrollment done successfully';
            this.submitting = true; 
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
    if (this.submitting) return;
    
    this.submitting = true;
    this.errorMessage = '';
    this.successMessage = '';
    
    this.studentApi.enrollInCourse(this.courseId).subscribe({
      next: (response) => {
        this.successMessage = 'Enrollment done successfully';
        this.errorMessage = '';
        this.submitting = true;

        setTimeout(() => {
          this.router.navigate(['/student/courses'], { 
            state: { activeTab: 'enrolled' } 
          });
        }, 2000);
      },
      error: (err) => {
        console.error('Database transaction registration failure details:', err);
        this.errorMessage = err?.error?.message || err?.message || 'Enrollment transaction processing failed.';
        this.submitting = false;
      }
    });
  }
}