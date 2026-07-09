import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment.development';
import {
  InstructorFilterOutput,
  RegistrarCourseCreate,
  RegistrarCourseResponse,
  RegistrarFilterParams,
  RegistrarProfile,
  StudentFilterOutput
} from '../models/registrar.model';

@Injectable({ providedIn: 'root' })
export class RegistrarApiService {

  private readonly baseUrl = `${environment.apiBaseUrl}/registrar`;

  constructor(private http: HttpClient) {}

  // GET /registrar/profile
  getProfile(): Observable<RegistrarProfile> {
    return this.http.get<RegistrarProfile>(`${this.baseUrl}/profile`);
  }

  // GET /registrar/course
  getAllCourses(): Observable<RegistrarCourseResponse[]> {
    return this.http.get<RegistrarCourseResponse[]>(`${this.baseUrl}/course`);
  }

  // POST /registrar/course
  provisionCourse(payload: RegistrarCourseCreate): Observable<RegistrarCourseResponse> {
    return this.http.post<RegistrarCourseResponse>(`${this.baseUrl}/course`, payload);
  }

  // GET /registrar/student/filter
  filterStudents(params: RegistrarFilterParams): Observable<StudentFilterOutput[]> {
    return this.http.get<StudentFilterOutput[]>(`${this.baseUrl}/student/filter`, {
      params: this.buildParams(params)
    });
  }

  // GET /registrar/instructor/filter
  filterInstructors(params: RegistrarFilterParams): Observable<InstructorFilterOutput[]> {
    return this.http.get<InstructorFilterOutput[]>(`${this.baseUrl}/instructor/filter`, {
      params: this.buildParams(params)
    });
  }

  private buildParams(params: RegistrarFilterParams): HttpParams {
    let httpParams = new HttpParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        httpParams = httpParams.set(key, String(value));
      }
    });
    return httpParams;
  }
}
