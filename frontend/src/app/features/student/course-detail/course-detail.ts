import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { StudentApiService } from '../services/student-api.service';
import { LoaderComponent } from '../../../shared/components/loader/loader.component';
import { SafePipe } from './safe.pipe';

@Component({
  selector: 'app-course-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, LoaderComponent, SafePipe],
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

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private studentApi: StudentApiService
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