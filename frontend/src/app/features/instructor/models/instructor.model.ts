// src/app/features/instructor/models/instructor.model.ts

import { CourseContent, Prerequisite } from '../../registrar/models/registrar.model';
import { AssignmentFile, CourseMaterial, Enrollment, Exam, Submission } from '../../student/models/student.model';

export type InstructorSkill =
  | 'JAVA'
  | 'PYTHON'
  | 'SPRING_BOOT'
  | 'MACHINE_LEARNING'
  | 'DATA_SCIENCE'
  | 'WEB_DEVELOPMENT'
  | 'DATABASE'
  | 'DEVOPS'
  | 'CLOUD_COMPUTING'
  | 'CYBERSECURITY'
  | 'ARTIFICIAL_INTELLIGENCE'
  | 'MOBILE_DEVELOPMENT'
  | 'ALGORITHMS'
  | 'NETWORKING'
  | 'SOFTWARE_TESTING';

export const INSTRUCTOR_SKILLS: InstructorSkill[] = [
  'JAVA',
  'PYTHON',
  'SPRING_BOOT',
  'MACHINE_LEARNING',
  'DATA_SCIENCE',
  'WEB_DEVELOPMENT',
  'DATABASE',
  'DEVOPS',
  'CLOUD_COMPUTING',
  'CYBERSECURITY',
  'ARTIFICIAL_INTELLIGENCE',
  'MOBILE_DEVELOPMENT',
  'ALGORITHMS',
  'NETWORKING',
  'SOFTWARE_TESTING'
];

export interface InstructorProfile {
  instructorId: number;
  skills: InstructorSkill[];
  experience?: number;
  dateOfBirth?: string;
  userId: number;
  name: string;
  email: string;
  phone?: number;
  role: string;
}

export interface InstructorInput {
  skills: InstructorSkill[];
  experience: number;
  dateOfBirth: string;
}

export interface InstructorCourse {
  courseId: number;
  title: string;
  description: string;
  duration?: string;
  prerequisite: Prerequisite;
  courseContent?: CourseContent[];
  isPublished?: boolean;
  instructorId: number;
  instructorName: string;
  syllabusPath?: string;
  startDate?: string; // <-- ADDED
  endDate?: string;   // <-- ADDED
}

export interface InstructorResourceResponse {
  materials: CourseMaterial[];
  assignmentFiles: AssignmentFile[];
}

export interface GradeInput {
  grade: number;
  feedback: string;
}