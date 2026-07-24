import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RegistrarApiService } from '../../services/registrar-api.service';
import { InstructorFilterOutput, Prerequisite, PREREQUISITE_OPTIONS } from '../../models/registrar.model';

@Component({
  selector: 'app-registrar-provision-course',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './provision-course.component.html'
})
export class ProvisionCourseComponent implements OnInit {
  readonly prerequisiteOptions: Prerequisite[] = PREREQUISITE_OPTIONS;
  courseForm!: FormGroup;
  submitting = false;
  formError = '';
  formSuccess = '';
  instructors: InstructorFilterOutput[] = [];
  loadingInstructors = true;
  syllabusBase64String: string | null = null;

  constructor(private fb: FormBuilder, private registrarApi: RegistrarApiService) {
    this.buildForm();
  }

  ngOnInit(): void {
    this.loadInstructors();
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

  onFileSelected(event: Event): void {
    const element = event.target as HTMLInputElement;
    const file: File | null = element.files ? element.files[0] : null;
    if (file) {
      if (file.type !== 'application/pdf') {
        alert('Validation Denied: Only PDF documents are accepted for structural course syllabus storage.');
        element.value = '';
        this.syllabusBase64String = null;
        return;
      }
      const reader = new FileReader();
      reader.onload = () => {
        const rawResult = reader.result as string;
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
    this.registrarApi.filterInstructors({ sortBy: 'name', sortDir: 'asc' }).subscribe({
      next: (instructors) => {
        this.instructors = instructors;
        this.loadingInstructors = false;
      },
      error: () => {
        this.loadingInstructors = false;
      }
    });
  }

  onSubmit(): void {
    this.formError = '';
    this.formSuccess = '';
    if (this.courseForm.invalid) {
      this.courseForm.markAllAsTouched();
      return;
    }
    const raw = this.courseForm.value;
    const payload: any = {
      title: raw.title,
      description: raw.description || undefined,
      prerequisite: raw.prerequisite || undefined,
      startDate: raw.startDate,
      endDate: raw.endDate,
      enrollmentDeadlineDate: raw.enrollmentDeadlineDate,
      instructorId: Number(raw.instructorId),
      syllabusPath: this.syllabusBase64String
    };
    this.submitting = true;
    this.registrarApi.provisionCourse(payload).subscribe({
      next: (course) => {
        this.submitting = false;
        this.formSuccess = `Course "${course.title}" was provisioned and assigned to ${course.instructorName}.`;
        this.resetForm();
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
    this.syllabusBase64String = null;
    const fileInput = document.getElementById('syllabusFile') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
  }
}