import { Routes } from '@angular/router';
import { roleGuard } from '../../core/guards/role.guard';
import { Role } from '../../core/models/role.enum';

export const STUDENT_ROUTES: Routes = [
  
  {
    path: '',
    loadComponent: () =>
      import('./layout/student-layout.component').then(m => m.StudentLayoutComponent),
    canActivate: [roleGuard([Role.STUDENT])],
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
      
      // ── ADDED: Route parameter path mapping rule for the dynamic single view detail page ──
      {
        path: 'courses/:id',
        loadComponent: () =>
          import('./course-detail/course-detail').then(m => m.CourseDetailComponent)
      },
      
      {
        path: 'assignments',
        loadComponent: () =>
          import('./assignments/assignments.component').then(m => m.AssignmentsComponent)
      },
      {
        path: 'exams',
        loadComponent: () =>
          import('./exams/exams.component').then(m => m.ExamsComponent)
      },
      {
        path: 'results',
        loadComponent: () =>
          import('./results/results.component').then(m => m.ResultsComponent)
      },
      {
        path: 'profile',
        loadComponent: () =>
          import('./profile/profile.component').then(m => m.ProfileComponent)
      }
    ]
  }
];