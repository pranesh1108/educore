// Mirrors com.cts.dto.RegistrarOutputDTO — response for GET /registrar/profile
export interface RegistrarProfile {
  registrarId: number;
  publishedCourseCount: number;
  userId: number;
  name: string;
  email: string;
  role: string;
  status: string;
}

// Mirrors com.cts.enumerate.Prerequisite
export type Prerequisite =
  | 'NONE'
  | 'BASIC_JAVA'
  | 'BASIC_PYTHON'
  | 'BASIC_PROGRAMMING'
  | 'DATA_STRUCTURES'
  | 'ALGORITHMS'
  | 'DATABASE_FUNDAMENTALS'
  | 'WEB_BASICS'
  | 'NETWORKING_BASICS'
  | 'MATHEMATICS'
  | 'STATISTICS'
  | 'MACHINE_LEARNING_BASICS'
  | 'SPRING_BASICS'
  | 'CLOUD_BASICS';

export const PREREQUISITE_OPTIONS: Prerequisite[] = [
  'NONE',
  'BASIC_JAVA',
  'BASIC_PYTHON',
  'BASIC_PROGRAMMING',
  'DATA_STRUCTURES',
  'ALGORITHMS',
  'DATABASE_FUNDAMENTALS',
  'WEB_BASICS',
  'NETWORKING_BASICS',
  'MATHEMATICS',
  'STATISTICS',
  'MACHINE_LEARNING_BASICS',
  'SPRING_BASICS',
  'CLOUD_BASICS'
];

// Mirrors com.cts.dto.CourseContentDTO
export interface CourseContent {
  topic: string;
  description?: string;
}

// Mirrors com.cts.dto.RegistrarCourseCreateDTO — request body for POST /registrar/course
export interface RegistrarCourseCreate {
  title: string;
  description?: string;
  prerequisite?: Prerequisite | '';
  courseContent?: CourseContent[];
  startDate: string;             
  endDate: string;               
  enrollmentDeadlineDate: string; 
  instructorId: number;
}

// Mirrors com.cts.dto.RegistrarCourseResponseDTO
export interface RegistrarCourseResponse {
  courseId: number;
  title: string;
  description: string;
  prerequisite: Prerequisite;
  courseContent: CourseContent[];
  startDate?: string;               
  endDate?: string;                 
  enrollmentDeadlineDate?: string;   
  instructorId: number;
  instructorName: string;
  instructorEmail: string;
  instructorSkill: string;
}

// Mirrors com.cts.dto.StudentFilterOutputDTO — GET /registrar/student/filter
export interface StudentFilterOutput {
  studentId: number;
  name: string;
  email: string;
  fieldOfInterest: string;
  status: string;
  enrolledCourses: string[];
}

// Mirrors com.cts.dto.InstructorFilterOutputDTO — GET /registrar/instructor/filter
export interface InstructorFilterOutput {
  instructorId: number;
  name: string;
  email: string;
  skill: string;
  experience: number;
  status: string;
  assignedCourses: string[];
}

export type FilterRole = 'student' | 'instructor';

export type StudentSortBy = 'name' | 'fieldOfInterest' | 'status';
export type InstructorSortBy = 'name' | 'experience' | 'status';
export type SortDirection = 'asc' | 'desc';

// Query params accepted by GET /registrar/{role}/filter
export interface RegistrarFilterParams {
  name?: string;
  status?: string;
  fieldOfInterest?: string;
  enrolledCourse?: string;
  skill?: string;
  experience?: number;
  sortBy?: string;
  sortDir?: SortDirection;
}
