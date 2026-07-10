import { Routes } from '@angular/router';
import { roleGuard } from '../../core/guards/role.guard';
import { Role } from '../../core/models/role.enum';

export const EXAM_COORDINATOR_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./layout/exam-coordinator-layout.component').then(m => m.ExamCoordinatorLayoutComponent),
    canActivate: [roleGuard([Role.EXAM_COORDINATOR])],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'exams',
        loadComponent: () =>
          import('./exams/exams.component').then(m => m.ExamsComponent)
      },
      {
        path: 'exam-rooms',
        loadComponent: () =>
          import('./exam-rooms/exam-rooms.component').then(m => m.ExamRoomsComponent)
      },
      {
        path: 'results',
        loadComponent: () =>
          import('./results/results.component').then(m => m.ResultsComponent)
      }
    ]
  }
];
