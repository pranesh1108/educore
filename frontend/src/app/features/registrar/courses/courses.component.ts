import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RegistrarApiService } from '../services/registrar-api.service';
import { LoaderComponent } from '../../../shared/components/loader/loader.component';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';
import { RegistrarCourseResponse } from '../models/registrar.model';

@Component({
  selector: 'app-registrar-courses',
  standalone: true,
  imports: [CommonModule, FormsModule, LoaderComponent, PaginationComponent],
  templateUrl: './courses.component.html',
  styleUrl: './courses.component.css'
})
export class CoursesComponent implements OnInit {
  courses: RegistrarCourseResponse[] = [];
  filteredCourses: RegistrarCourseResponse[] = [];
  pagedCourses: RegistrarCourseResponse[] = [];
  loadingCourses = true;
  coursesError = '';
  searchTerm = '';
  currentPage = 1;
  readonly pageSize = 6;

  constructor(private registrarApi: RegistrarApiService) {}

  ngOnInit(): void {
    this.loadCourses();
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
}