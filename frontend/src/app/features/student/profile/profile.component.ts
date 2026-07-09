import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { StudentApiService } from '../services/student-api.service';
import { StudentProfile } from '../models/student.model';
import { LoaderComponent } from '../../../shared/components/loader/loader.component';

@Component({
  selector: 'app-student-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, LoaderComponent],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit {
  profile: StudentProfile | null = null;
  loading = true;
  saving = false;
  errorMessage = '';
  successMessage = '';

  // Form fields
  dateOfBirth = '';
  fieldOfInterest = '';

  constructor(private studentApi: StudentApiService) {}

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.studentApi.getProfile().subscribe({
      next: (profile) => {
        this.profile = profile;
        this.dateOfBirth = profile.dateOfBirth || '';
        this.fieldOfInterest = profile.fieldOfInterest || '';
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || err?.message || 'Unable to load profile.';
        this.loading = false;
      }
    });
  }

  saveProfile(): void {
    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.studentApi.updateProfile({
      dateOfBirth: this.dateOfBirth,
      fieldOfInterest: this.fieldOfInterest
    }).subscribe({
      next: (profile) => {
        this.profile = profile;
        this.successMessage = 'Profile updated successfully!';
        this.saving = false;
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || err?.message || 'Unable to update profile.';
        this.saving = false;
      }
    });
  }

  get initials(): string {
    if (!this.profile?.name) return '?';
    return this.profile.name
      .split(' ')
      .map(p => p.charAt(0))
      .join('')
      .slice(0, 2)
      .toUpperCase();
  }
}
