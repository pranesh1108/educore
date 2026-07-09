import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment.development';
import { InstructorFilterOutput } from '../../registrar/models/registrar.model';
import { Exam, ExamResult } from '../../student/models/student.model';
import {
  ExamInput,
  ExamRoomInput,
  ExamRoomOutput,
  ExamResultInput,
  PhysicalRoomInput,
  PhysicalRoomOutput,
  ExamRoomAllocationStudent
} from '../models/coordinator.model';

@Injectable({ providedIn: 'root' })
export class CoordinatorApiService {
  private readonly baseUrl = `${environment.apiBaseUrl}/exam-coordinator`;

  constructor(private http: HttpClient) {}

  createExam(input: ExamInput): Observable<Exam> {
    return this.http.post<Exam>(`${this.baseUrl}/exams`, input);
  }

  searchExams(params?: {
    courseId?: number;
    instructorId?: number;
    term?: string;
    status?: string;
  }): Observable<Exam[]> {
    let httpParams = new HttpParams();
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
          httpParams = httpParams.set(key, String(value));
        }
      });
    }
    return this.http.get<Exam[]>(`${this.baseUrl}/exams`, { params: httpParams });
  }

  getExamDetails(examId: number): Observable<Exam> {
    return this.http.get<Exam>(`${this.baseUrl}/exams/${examId}`);
  }

  deleteExam(examId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/exams/${examId}`);
  }

  createAndAllocate(input: ExamRoomInput): Observable<ExamRoomOutput> {
    return this.http.post<ExamRoomOutput>(`${this.baseUrl}/exam-rooms`, input);
  }

  getRoomsForExam(examId: number): Observable<ExamRoomOutput[]> {
    return this.http.get<ExamRoomOutput[]>(`${this.baseUrl}/exam-rooms/exam/${examId}`);
  }

  getEnrolledStudentsForExam(examId: number): Observable<ExamRoomAllocationStudent[]> {
    return this.http.get<ExamRoomAllocationStudent[]>(`${this.baseUrl}/exams/${examId}/enrolled-students`);
  }

  getAllInstructors(): Observable<InstructorFilterOutput[]> {
    return this.http.get<InstructorFilterOutput[]>(`${this.baseUrl}/instructors`);
  }

  createRoom(input: PhysicalRoomInput): Observable<PhysicalRoomOutput> {
    return this.http.post<PhysicalRoomOutput>(`${this.baseUrl}/rooms`, input);
  }

  getAllRooms(status?: string): Observable<PhysicalRoomOutput[]> {
    let httpParams = new HttpParams();
    if (status) {
      httpParams = httpParams.set('status', status);
    }
    return this.http.get<PhysicalRoomOutput[]>(`${this.baseUrl}/rooms`, { params: httpParams });
  }

  publishResult(input: ExamResultInput): Observable<ExamResult> {
    return this.http.post<ExamResult>(`${this.baseUrl}/results`, input);
  }
}
