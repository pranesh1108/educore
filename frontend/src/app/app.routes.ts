import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'auth/login', pathMatch: 'full' },
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth.routes').then(m => m.AUTH_ROUTES)
  },
  {
    path: 'dashboard',
    loadComponent: () =>
      import('./core/layout/main-layout/main-layout.component').then(m => m.MainLayoutComponent),
    canActivate: [authGuard]
  },
  {
    path: 'student',
    loadChildren: () => import('./features/student/student.routes').then(m => m.STUDENT_ROUTES)
  },
  {
    path: 'instructor',
    loadChildren: () => import('./features/instructor/instructor.routes').then(m => m.INSTRUCTOR_ROUTES)
  },
  {
    path: 'exam-coordinator',
    loadChildren: () => import('./features/exam-coordinator/exam-coordinator.routes').then(m => m.EXAM_COORDINATOR_ROUTES)
  },
  {
    path: 'registrar',
    loadChildren: () => import('./features/registrar/registrar.routes').then(m => m.REGISTRAR_ROUTES)
  },
  { path: '**', redirectTo: 'auth/login' }
];
