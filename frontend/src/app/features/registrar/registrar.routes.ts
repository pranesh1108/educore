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
        path: 'courses',
        loadComponent: () =>
          import('./courses/courses.component').then(m => m.CoursesComponent)
      },
      {
        path: 'users',
        loadComponent: () =>
          import('./users/users.component').then(m => m.UsersComponent)
      }
    ]
  }
];
