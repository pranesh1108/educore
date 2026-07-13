import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';

import { RegistrarApiService } from '../services/registrar-api.service';
import { LoaderComponent } from '../../../shared/components/loader/loader.component';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';
import {
  InstructorFilterOutput,
  PREREQUISITE_OPTIONS,
  Prerequisite,
  RegistrarCourseCreate,
  RegistrarCourseResponse
} from '../models/registrar.model';

@Component({
  selector: 'app-registrar-courses',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, LoaderComponent, PaginationComponent],
  templateUrl: './courses.component.html',
  styleUrl: './courses.component.css'
})
export class CoursesComponent implements OnInit {

  readonly prerequisiteOptions: Prerequisite[] = PREREQUISITE_OPTIONS;

  courseForm!: FormGroup;
  submitting = false;
  formError = '';
  formSuccess = '';

  instructors: InstructorFilterOutput[] = [];
  loadingInstructors = true;

  courses: RegistrarCourseResponse[] = [];
  filteredCourses: RegistrarCourseResponse[] = [];
  pagedCourses: RegistrarCourseResponse[] = [];
  loadingCourses = true;
  coursesError = '';

  searchTerm = '';
  currentPage = 1;
  readonly pageSize = 6;

  // ── ADDED: State to manage your selected Base64 string context ──
  syllabusBase64String: string | null = null;

  constructor(private fb: FormBuilder, private registrarApi: RegistrarApiService) {
    this.buildForm();
  }

  ngOnInit(): void {
    this.loadInstructors();
    this.loadCourses();
  }

  private buildForm(): void {
    this.courseForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      description: ['', [Validators.maxLength(2000)]],
      prerequisite: ['NONE'],
      startDate: ['', [Validators.required]],
      endDate: ['', [Validators.required]],
      enrollmentDeadlineDate: ['', [Validators.required]],
      instructorId: ['', [Validators.required]]
    });
  }

  get f() {
    return this.courseForm.controls;
  }

  // ── ADDED: File select event reader handler ──
  onFileSelected(event: Event): void {
    const element = event.target as HTMLInputElement;
    const file: File | null = element.files ? element.files[0] : null;

    if (file) {
      if (file.type !== 'application/pdf') {
        alert('Validation Denied: Only PDF documents are accepted for structural course syllabus storage.');
        element.value = ''; // Flush input out
        this.syllabusBase64String = null;
        return;
      }

      const reader = new FileReader();
      reader.onload = () => {
        const rawResult = reader.result as string;
        // Split extracts the pure base64 binary bytes from the raw Data URL data stream metadata header
        this.syllabusBase64String = rawResult.split(',')[1];
      };
      reader.onerror = () => {
        this.formError = 'Error encountered during file conversion loops.';
      };
      reader.readAsDataURL(file);
    }
  }

  private loadInstructors(): void {
    this.loadingInstructors = true;
    this.registrarApi.filterInstructors({  sortBy: 'name', sortDir: 'asc' }).subscribe({
      next: (instructors) => {
        this.instructors = instructors;
        this.loadingInstructors = false;
      },
      error: () => {
        this.loadingInstructors = false;
      }
    });
  }

  private loadCourses(): void {
    this.loadingCourses = true;
    this.coursesError = '';
    this.registrarApi.getAllCourses().subscribe({
      next: (courses) => {
        this.courses = courses;
        this.applyFilter();
        this.loadingCourses = false;
      },
      error: (err) => {
        this.coursesError = err?.message || 'Unable to load the course catalogue.';
        this.loadingCourses = false;
      }
    });
  }

  applyFilter(): void {
    const term = this.searchTerm.trim().toLowerCase();
    this.filteredCourses = term
      ? this.courses.filter(c =>
          c.title.toLowerCase().includes(term) ||
          (c.instructorName ?? '').toLowerCase().includes(term))
      : [...this.courses];

    this.setPage(1);
  }

  setPage(page: number): void {
    this.currentPage = page;
    const start = (page - 1) * this.pageSize;
    this.pagedCourses = this.filteredCourses.slice(start, start + this.pageSize);
  }

  onSubmit(): void {
    this.formError = '';
    this.formSuccess = '';

    if (this.courseForm.invalid) {
      this.courseForm.markAllAsTouched();
      return;
    }

    const raw = this.courseForm.value;
    
    // ── CHANGED: Appended the string content safely into your existing JSON mapping payload model ──
    const payload: any = {
      title: raw.title,
      description: raw.description || undefined,
      prerequisite: raw.prerequisite || undefined,
      startDate: raw.startDate,
      endDate: raw.endDate,
      enrollmentDeadlineDate: raw.enrollmentDeadlineDate,
      instructorId: Number(raw.instructorId),
      syllabusPath: this.syllabusBase64String // Sent across cleanly as string property 
    };

    this.submitting = true;
    this.registrarApi.provisionCourse(payload).subscribe({
      next: (course) => {
        this.submitting = false;
        this.formSuccess = `Course "${course.title}" was provisioned and assigned to ${course.instructorName}.`;
        this.resetForm();
        this.loadCourses();
      },
      error: (err) => {
        this.submitting = false;
        this.formError = err?.message || 'Unable to provision this course. Please try again.';
      }
    });
  }

  resetForm(): void {
    this.courseForm.reset({
      title: '',
      description: '',
      prerequisite: 'NONE',
      startDate: '',
      endDate: '',
      enrollmentDeadlineDate: '',
      instructorId: ''
    });
    
    // ── ADDED: Clear out files references cleanly on reset events ──
    this.syllabusBase64String = null;
    const fileInput = document.getElementById('syllabusFile') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
  }
}