import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { NavbarComponent } from '../../../core/layout/navbar/navbar.component';

@Component({
  selector: 'app-exam-coordinator-layout',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet, NavbarComponent],
  templateUrl: './exam-coordinator-layout.component.html',
  styleUrl: './exam-coordinator-layout.component.css'
})
export class ExamCoordinatorLayoutComponent {
  readonly navLinks = [
    { path: '/exam-coordinator/dashboard', label: 'Dashboard', icon: 'bi-speedometer2' },
    { path: '/exam-coordinator/exams', label: 'Manage Exams', icon: 'bi-calendar-event' },
    { path: '/exam-coordinator/exam-rooms', label: 'Venues & Rooms', icon: 'bi-building' },
    { path: '/exam-coordinator/results', label: 'Publish Results', icon: 'bi-check2-all' },
    { path: '/exam-coordinator/profile', label: 'My Profile', icon: 'bi-person-circle' }
  ];
}
