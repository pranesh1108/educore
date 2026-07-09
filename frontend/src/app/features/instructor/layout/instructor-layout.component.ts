import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { NavbarComponent } from '../../../core/layout/navbar/navbar.component';

@Component({
  selector: 'app-instructor-layout',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet, NavbarComponent],
  templateUrl: './instructor-layout.component.html',
  styleUrl: './instructor-layout.component.css'
})
export class InstructorLayoutComponent {
  readonly navLinks = [
    { path: '/instructor/dashboard', label: 'Dashboard', icon: 'bi-speedometer2' },
    { path: '/instructor/courses', label: 'Assigned Courses', icon: 'bi-journal-bookmark' },
    { path: '/instructor/assignments', label: 'Publish Resources', icon: 'bi-file-earmark-arrow-up' },
    { path: '/instructor/grading', label: 'Grading & Submissions', icon: 'bi-check2-square' },
    { path: '/instructor/exams', label: 'Assigned Exams', icon: 'bi-calendar-event' },
    { path: '/instructor/profile', label: 'My Profile', icon: 'bi-person-circle' }
  ];
}
