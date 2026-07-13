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

  dateOfBirth = '';
  
  // Track selected academic topics using a Set collection structure
  selectedInterests: Set<string> = new Set<string>();

  // Mirroring the exact Enum options layout structure matching your operational needs
  readonly interestOptions: string[] = [
    'JAVA', 'PYTHON', 'SPRING_BOOT', 'MACHINE_LEARNING', 'DATA_SCIENCE', 
    'WEB_DEVELOPMENT', 'DATABASE', 'DEVOPS', 'CLOUD_COMPUTING', 
    'CYBERSECURITY', 'ARTIFICIAL_INTELLIGENCE', 'MOBILE_DEVELOPMENT', 
    'ALGORITHMS', 'NETWORKING', 'SOFTWARE_TESTING'
  ];

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
        this.selectedInterests.clear();
        
        // Handle incoming comma-separated strings or pre-parsed collections safely
        if (profile.fieldOfInterest) {
          const items = typeof profile.fieldOfInterest === 'string'
            ? profile.fieldOfInterest.split(',')
            : profile.fieldOfInterest;
            
          items.forEach((item: string) => {
            const normalized = item.trim().toUpperCase().replace(' ', '_');
            if (this.interestOptions.includes(normalized)) {
              this.selectedInterests.add(normalized);
            }
          });
        }
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || err?.message || 'Unable to load profile.';
        this.loading = false;
      }
    });
  }

  toggleInterest(interest: string): void {
    if (this.selectedInterests.has(interest)) {
      this.selectedInterests.delete(interest);
    } else {
      this.selectedInterests.add(interest);
    }
  }

  saveProfile(): void {
    if (!this.dateOfBirth || this.selectedInterests.size === 0) return;
    
    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';

    // Convert the tracking collection back to a standard payload delivery format string
    const interestsPayload = Array.from(this.selectedInterests).join(',');

    this.studentApi.updateProfile({
      dateOfBirth: this.dateOfBirth,
      fieldOfInterest: interestsPayload
    }).subscribe({
      next: (updatedProfile) => {
        this.profile = {
          ...updatedProfile,
          name: this.profile?.name || updatedProfile.name,
          email: this.profile?.email || updatedProfile.email
        };
        this.successMessage = 'Profile updated successfully!';
        this.saving = false;
        
        setTimeout(() => {
          this.successMessage = '';
        }, 3000);
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
      .filter(p => p.length > 0)
      .map(p => p.charAt(0))
      .join('')
      .slice(0, 2)
      .toUpperCase();
  }
}