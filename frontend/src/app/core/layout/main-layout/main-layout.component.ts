import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { AuthService } from '../../services/auth.service';
import { Role } from '../../models/role.enum';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [CommonModule, NavbarComponent],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.css'
})
export class MainLayoutComponent implements OnInit {

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    const role = this.authService.getRole();
    if (role === Role.STUDENT) {
      this.router.navigate(['/student']);
    } else if (role === Role.INSTRUCTOR) {
      this.router.navigate(['/instructor']);
    } else if (role === Role.REGISTRAR) {
      this.router.navigate(['/registrar']);
    } else if (role === Role.EXAM_COORDINATOR) {
      this.router.navigate(['/exam-coordinator']);
    } else {
      this.router.navigate(['/auth/login']);
    }
  }

  get currentUser() {
    return this.authService.getCurrentUser();
  }
}
