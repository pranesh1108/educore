import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { NavbarComponent } from '../../../core/layout/navbar/navbar.component';

@Component({
  selector: 'app-registrar-layout',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet, NavbarComponent],
  templateUrl: './registrar-layout.component.html',
  styleUrl: './registrar-layout.component.css'
})
export class RegistrarLayoutComponent {

  readonly navLinks = [
    { path: '/registrar/dashboard', label: 'Dashboard', icon: 'bi-speedometer2' },
    { path: '/registrar/courses', label: 'Courses', icon: 'bi-journal-bookmark' },
    { path: '/registrar/users', label: 'Students & Instructors', icon: 'bi-people' }
  ];
}
