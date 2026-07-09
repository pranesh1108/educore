import { Exam, ExamResult } from '../../student/models/student.model';

export interface ExamInput {
  title: string;
  description?: string;
  term: 'SPRING_2026' | 'FALL_2026' | 'WINTER_2026' | 'SUMMER_2026';
  examDate: string; // yyyy-MM-dd HH:mm
  durationMinutes: number;
  totalMarks: number;
  passingMarks: number;
  courseId: number;
  instructorId: number;
}

export interface ExamRoomInput {
  physicalRoomId: number;
  examId: number;
  roomNumber?: number;
}

export interface ExamRoomAllocationStudent {
  allocationId?: number;
  studentId: number;
  studentName: string;
  email: string;
}

export interface ExamRoomOutput {
  roomId: number;
  roomName: string;
  location: string;
  capacity: number;
  roomNumber: number;
  examId: number;
  examTitle: string;
  examDate: string;
  studentsAllocated: number;
  students: ExamRoomAllocationStudent[];
  createdAt: string;
  term: string;
}

export interface PhysicalRoomInput {
  roomName: string;
  location: string;
  capacity: number;
}

export interface PhysicalRoomOutput {
  roomId: number;
  roomName: string;
  location: string;
  capacity: number;
  status: 'AVAILABLE' | 'OCCUPIED';
  assignedExamId?: number;
  assignedFrom?: string;
  assignedUntil?: string;
  createdAt: string;
}

export interface ExamResultInput {
  examId: number;
  studentId: number;
  courseId: number;
  score: number;
}
