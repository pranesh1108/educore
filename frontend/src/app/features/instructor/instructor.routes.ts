import { Routes } from '@angular/router';
import { roleGuard } from '../../core/guards/role.guard';
import { Role } from '../../core/models/role.enum';

export const INSTRUCTOR_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./layout/instructor-layout.component').then(m => m.InstructorLayoutComponent),
    canActivate: [roleGuard([Role.INSTRUCTOR])],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'courses',
        loadComponent: () =>
          import('./courses/courses.component').then(m => m.CoursesComponent)
      },
      {
        path: 'assignments',
        loadComponent: () =>
          import('./assignments/assignments.component').then(m => m.AssignmentsComponent)
      },
      {
        path: 'grading',
        loadComponent: () =>
          import('./grading/grading.component').then(m => m.GradingComponent)
      },
      {
        path: 'exams',
        loadComponent: () =>
          import('./exams/exams.component').then(m => m.ExamsComponent)
      },
      {
        path: 'profile',
        loadComponent: () =>
          import('./profile/profile.component').then(m => m.ProfileComponent)
      }
    ]
  }
];
