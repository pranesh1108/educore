import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment.development';
import { RegistrarCourseResponse } from '../../registrar/models/registrar.model';
import {
  Assignment,
  CourseContent,
  Enrollment,
  Exam,
  ExamResult,
  StudentInput,
  StudentProfile,
  Submission
} from '../models/student.model';

@Injectable({ providedIn: 'root' })
export class StudentApiService {
  private readonly baseUrl = `${environment.apiBaseUrl}/student`;
  private readonly coursesUrl = `${environment.apiBaseUrl}/courses`;
  private readonly resultsUrl = `${environment.apiBaseUrl}/results`;

  constructor(private http: HttpClient) {}

  getProfile(): Observable<StudentProfile> {
    return this.http.get<StudentProfile>(`${this.baseUrl}/profile`);
  }

  updateProfile(input: StudentInput): Observable<StudentProfile> {
    return this.http.put<StudentProfile>(`${this.baseUrl}/profile/update`, input);
  }

  // Get shared course catalogue
  getCoursesCatalogue(params?: { title?: string; topic?: string }): Observable<any> {
    let queryParams: any = {};
    if (params?.title) queryParams.title = params.title;
    if (params?.topic) queryParams.topic = params.topic;
    return this.http.get<any>(`${this.coursesUrl}/all`, { params: queryParams });
  }

  enrollInCourse(courseId: number): Observable<Enrollment> {
    return this.http.post<Enrollment>(`${this.baseUrl}/course/${courseId}/enroll`, {});
  }

  getMyCourses(): Observable<Enrollment[]> {
    return this.http.get<Enrollment[]>(`${this.baseUrl}/my-courses`);
  }

  getCourseContent(courseId: number): Observable<CourseContent> {
    return this.http.get<CourseContent>(`${this.baseUrl}/course/${courseId}/content`);
  }

  downloadMaterial(fileId: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/material/${fileId}/download`, { responseType: 'blob' });
  }

  downloadAssignmentFile(fileId: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/assignment-file/${fileId}/download`, { responseType: 'blob' });
  }

  submitAssignment(assignmentId: number, file: File): Observable<Submission> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<Submission>(`${this.baseUrl}/assignment/${assignmentId}/submit`, formData);
  }

  getMySubmissions(): Observable<Submission[]> {
    return this.http.get<Submission[]>(`${this.baseUrl}/my-submissions`);
  }

  getMyExams(): Observable<Exam[]> {
    return this.http.get<Exam[]>(`${this.baseUrl}/my-exams`);
  }

  getMyResults(studentId: number): Observable<ExamResult[]> {
    return this.http.get<ExamResult[]>(`${this.resultsUrl}/student/${studentId}`);
  }
}
