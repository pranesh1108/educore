import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../../core/services/auth.service';
import { Role } from '../../../core/models/role.enum';

// Mirrors backend password rule: 8-20 chars, upper, lower, digit, special char
const PASSWORD_PATTERN = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!_\-*./?]).{8,20}$/;

// Mirrors backend name rule: letters, spaces, apostrophes, hyphens, 2-25 chars
const NAME_PATTERN = /^[a-zA-Z .'\-]{2,25}$/;

// Mirrors backend phone rule: 10-digit mobile number starting 6-9
const PHONE_PATTERN = /^[6-9][0-9]{9}$/;

function passwordsMatchValidator(): ValidatorFn {
  return (group: AbstractControl): ValidationErrors | null => {
    const password = group.get('password')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;
    return password && confirmPassword && password !== confirmPassword
      ? { passwordMismatch: true }
      : null;
  };
}

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  private fb = inject(FormBuilder);

  roles = [
    { label: 'Student', value: Role.STUDENT },
    { label: 'Instructor', value: Role.INSTRUCTOR },
    { label: 'Exam Coordinator', value: Role.EXAM_COORDINATOR }
  ];

  loading = false;
  errorMessage = '';
  successMessage = '';

  registerForm = this.fb.group(
    {
      name: ['', [Validators.required, Validators.pattern(NAME_PATTERN)]],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.required, Validators.pattern(PHONE_PATTERN)]],
      role: ['', [Validators.required]],
      password: ['', [Validators.required, Validators.pattern(PASSWORD_PATTERN)]],
      confirmPassword: ['', [Validators.required]]
    },
    { validators: passwordsMatchValidator() }
  );

  constructor(private authService: AuthService, private router: Router) {}

  get f() {
    return this.registerForm.controls;
  }

  onSubmit(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.authService.register({
      name: this.f.name.value!,
      email: this.f.email.value!,
      password: this.f.password.value!,
      role: this.f.role.value!,
      phone: Number(this.f.phone.value)
    }).subscribe({
      next: () => {
        this.loading = false;
        this.successMessage = 'Account created successfully. Redirecting to login...';
        setTimeout(() => this.router.navigate(['/auth/login']), 1500);
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err?.message || 'Registration failed. Please try again.';
      }
    });
  }
}
