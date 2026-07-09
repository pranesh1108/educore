import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment.development';
import { Assignment, CourseMaterial, Enrollment, Exam, Submission } from '../../student/models/student.model';
import {
  GradeInput,
  InstructorCourse,
  InstructorInput,
  InstructorProfile,
  InstructorResourceResponse
} from '../models/instructor.model';

@Injectable({ providedIn: 'root' })
export class InstructorApiService {
  private readonly baseUrl = `${environment.apiBaseUrl}/instructor`;

  constructor(private http: HttpClient) {}

  getProfile(): Observable<InstructorProfile> {
    return this.http.get<InstructorProfile>(`${this.baseUrl}/profile`);
  }

  updateProfile(input: InstructorInput): Observable<InstructorProfile> {
    return this.http.put<InstructorProfile>(`${this.baseUrl}/profile/update`, input);
  }

  getAssignedCourses(): Observable<InstructorCourse[]> {
    return this.http.get<InstructorCourse[]>(`${this.baseUrl}/my-courses`);
  }

  getEnrolledStudents(courseId: number): Observable<Enrollment[]> {
    return this.http.get<Enrollment[]>(`${this.baseUrl}/course/${courseId}/enrolled-students`);
  }

  publishCourseMaterial(courseId: number, file: File, textContent?: string): Observable<CourseMaterial> {
    const formData = new FormData();
    formData.append('file', file);
    if (textContent) {
      formData.append('textContent', textContent);
    }
    return this.http.post<CourseMaterial>(`${this.baseUrl}/course/${courseId}/material`, formData);
  }

  getCourseResources(courseId: number): Observable<InstructorResourceResponse> {
    return this.http.get<InstructorResourceResponse>(`${this.baseUrl}/course/${courseId}/resources`);
  }

  publishAssignment(
    courseId: number,
    title: string,
    instructions: string,
    totalMarks: number,
    dueDate: string,
    file: File
  ): Observable<Assignment> {
    const formData = new FormData();
    formData.append('courseId', String(courseId));
    formData.append('title', title);
    if (instructions) {
      formData.append('instructions', instructions);
    }
    formData.append('totalMarks', String(totalMarks));
    formData.append('dueDate', dueDate); // Expects "yyyy-MM-dd HH:mm"
    formData.append('file', file);
    return this.http.post<Assignment>(`${this.baseUrl}/assignment/publish`, formData);
  }

  getSubmissions(courseId: number): Observable<Submission[]> {
    return this.http.get<Submission[]>(`${this.baseUrl}/course/${courseId}/submissions`);
  }

  downloadSubmissionFile(submissionId: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/submission/${submissionId}/download`, { responseType: 'blob' });
  }

  gradeSubmission(submissionId: number, gradeInput: GradeInput): Observable<Submission> {
    return this.http.put<Submission>(`${this.baseUrl}/submission/${submissionId}/grade`, gradeInput);
  }

  getMyExams(): Observable<Exam[]> {
    return this.http.get<Exam[]>(`${this.baseUrl}/my-exams`);
  }
}
