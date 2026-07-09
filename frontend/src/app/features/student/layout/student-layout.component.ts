import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { NavbarComponent } from '../../../core/layout/navbar/navbar.component';

@Component({
  selector: 'app-student-layout',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet, NavbarComponent],
  templateUrl: './student-layout.component.html',
  styleUrl: './student-layout.component.css'
})
export class StudentLayoutComponent {
  readonly navLinks = [
    { path: '/student/dashboard', label: 'Dashboard', icon: 'bi-speedometer2' },
    { path: '/student/courses', label: 'Browse Courses', icon: 'bi-journal-bookmark' },
    { path: '/student/assignments', label: 'My Studies', icon: 'bi-book' },
    { path: '/student/exams', label: 'Exams & Schedule', icon: 'bi-calendar-event' },
    { path: '/student/results', label: 'My Results', icon: 'bi-patch-check' },
    { path: '/student/profile', label: 'My Profile', icon: 'bi-person-circle' }
  ];
}
