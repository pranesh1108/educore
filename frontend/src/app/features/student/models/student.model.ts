import { Prerequisite } from '../../registrar/models/registrar.model';

export interface StudentProfile {
  studentId: number;
  dateOfBirth?: string;
  educationLevel?: string;
  fieldOfInterest?: string;
  status: string;
  userId: number;
  name: string;
  email: string;
  phone?: number;
  role: string;
}

export interface StudentInput {
  dateOfBirth: string;
  fieldOfInterest: string;
}

export interface Enrollment {
  enrollmentId: number;
  courseId: number;
  courseTitle: string;
  courseDescription: string;
  courseDuration: string;
  instructorName: string;
  studentId: number;
  studentName: string;
  enrollmentNumber: string;
  enrolledAt: string;
  status: string;
}

export interface CourseMaterial {
  fileId: number;
  courseId: number;
  courseTitle: string;
  fileName: string;
  type: 'PDF' | 'TEXT';
  textContent?: string;
  uploadedAt: string;
}

export interface AssignmentFile {
  fileId: number;
  assignmentId: number;
  assignmentTitle: string;
  fileName: string;
  uploadedAt: string;
}

export interface Assignment {
  assignmentId: number;
  title: string;
  instructions?: string;
  totalMarks: number;
  publishedAt: string;
  dueDate: string;
  courseId: number;
  courseTitle: string;
  files: AssignmentFile[];
}

export interface CourseContent {
  materials: CourseMaterial[];
  assignments: Assignment[];
}

export interface Submission {
  submissionId: number;
  studentId: number;
  studentName: string;
  enrollmentNumber: string;
  fileName: string;
  submittedAt: string;
  status: string;
  grade?: number;
  feedback?: string;
  assignmentId: number;
  assignmentTitle: string;
  courseId: number;
  courseTitle: string;
}

export interface Exam {
  examId: number;
  title: string;
  description?: string;
  term: string;
  examDate: string;
  durationMinutes: number;
  totalMarks: number;
  passingMarks: number;
  courseId: number;
  courseTitle: string;
  instructorId: number;
  instructorName: string;
  roomId?: number;
  roomName?: string;
  roomLocation?: string;
  roomNumber?: number;
  createdAt: string;
  // Changed: Removed status property
}

export interface ExamResult {
  resultId: number;
  examId: number;
  examTitle: string;
  studentId: number;
  studentName: string;
  courseId: number;
  courseTitle: string;
  score: number;
  result: 'PASS' | 'FAIL';
  publishedAt: string;
  message?: string;
}
