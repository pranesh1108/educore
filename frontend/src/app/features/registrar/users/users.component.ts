import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';

import { RegistrarApiService } from '../services/registrar-api.service';
import { LoaderComponent } from '../../../shared/components/loader/loader.component';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';
import {
  FilterRole,
  InstructorFilterOutput,
  StudentFilterOutput
} from '../models/registrar.model';

@Component({
  selector: 'app-registrar-users',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LoaderComponent, PaginationComponent],
  templateUrl: './users.component.html',
  styleUrl: './users.component.css'
})
export class UsersComponent implements OnInit {

  role: FilterRole = 'student';
  filterForm!: FormGroup;

  students: StudentFilterOutput[] = [];
  instructors: InstructorFilterOutput[] = [];
  pagedStudents: StudentFilterOutput[] = [];
  pagedInstructors: InstructorFilterOutput[] = [];

  loading = false;
  errorMessage = '';

  currentPage = 1;
  readonly pageSize = 8;

  constructor(private fb: FormBuilder, private registrarApi: RegistrarApiService) {
    this.filterForm = this.fb.group({
      name: [''],
      status: [''],
      fieldOfInterest: [''],
      enrolledCourse: [''],
      skill: [''],
      experience: [''],
      sortBy: ['name'],
      sortDir: ['asc']
    });
  }

  ngOnInit(): void {
    this.search();
  }

  switchRole(role: FilterRole): void {
    if (this.role === role) {
      return;
    }
    this.role = role;
    this.filterForm.patchValue({ sortBy: 'name' });
    this.search();
  }

  search(): void {
    this.loading = true;
    this.errorMessage = '';
    const raw = this.filterForm.value;

    const params = {
      name: raw.name || undefined,
      status: raw.status || undefined,
      fieldOfInterest: this.role === 'student' ? (raw.fieldOfInterest || undefined) : undefined,
      enrolledCourse: this.role === 'student' ? (raw.enrolledCourse || undefined) : undefined,
      skill: this.role === 'instructor' ? (raw.skill || undefined) : undefined,
      experience: this.role === 'instructor' && raw.experience ? Number(raw.experience) : undefined,
      sortBy: raw.sortBy || 'name',
      sortDir: raw.sortDir || 'asc'
    };

    const request = (this.role === 'student'
      ? this.registrarApi.filterStudents(params)
      : this.registrarApi.filterInstructors(params)) as any;

    request.subscribe({
      next: (results: any[]) => {
        if (this.role === 'student') {
          this.students = results;
        } else {
          this.instructors = results;
        }
        this.loading = false;
        this.setPage(1);
      },
      error: (err: any) => {
        this.errorMessage = err?.message || `Unable to load ${this.role}s.`;
        this.loading = false;
      }
    });
  }

  resetFilters(): void {
    this.filterForm.reset({
      name: '', status: '', fieldOfInterest: '', enrolledCourse: '',
      skill: '', experience: '', sortBy: 'name', sortDir: 'asc'
    });
    this.search();
  }

  get totalResults(): number {
    return this.role === 'student' ? this.students.length : this.instructors.length;
  }

  setPage(page: number): void {
    this.currentPage = page;
    const start = (page - 1) * this.pageSize;
    if (this.role === 'student') {
      this.pagedStudents = this.students.slice(start, start + this.pageSize);
    } else {
      this.pagedInstructors = this.instructors.slice(start, start + this.pageSize);
    }
  }
}
