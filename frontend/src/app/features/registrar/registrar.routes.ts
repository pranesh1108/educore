import { Routes } from '@angular/router';
import { roleGuard } from '../../core/guards/role.guard';
import { Role } from '../../core/models/role.enum';

export const REGISTRAR_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./layout/registrar-layout.component').then(m => m.RegistrarLayoutComponent),
    canActivate: [roleGuard([Role.REGISTRAR])],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'provision-course',
        loadComponent: () =>
          import('./provision-course/provision-course.component/provision-course.component').then(m => m.ProvisionCourseComponent)
      },
      {
        path: 'course-catalog',
        loadComponent: () =>
          import('./courses/courses.component').then(m => m.CoursesComponent)
      },
      {
        path: 'courses',
        redirectTo: 'course-catalog',
        pathMatch: 'full'
      },
      {
        path: 'users',
        loadComponent: () =>
          import('./users/users.component').then(m => m.UsersComponent)
      },
      {
        path: 'audit-logs',
        loadComponent: () =>
          import('../shared-features/audit-log/audit-log.component').then(m => m.AuditLogComponent)
      }
    ]
  }
];