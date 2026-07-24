import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit {
  private fb = inject(FormBuilder);

  loading = false;
  errorMessage = '';
  ngOnInit(): void {
    localStorage.clear();
  }

  loginForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]]
  });

  constructor(private authService: AuthService) {}

  get f() {
    return this.loginForm.controls;
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.authService.login({
      email: this.f.email.value!,
      password: this.f.password.value!
    }).subscribe({
      next: () => {
        this.loading = false;
        this.authService.redirectToDashboard();
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err?.message || 'Login failed. Please check your credentials.';
      }
    });
  }
}
