import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';

import { LoaderComponent } from '../../../shared/components/loader/loader.component';
import { InstructorApiService } from '../services/instructor-api.service'; 
import { INSTRUCTOR_SKILLS, InstructorSkill } from '../models/instructor.model';

@Component({
  selector: 'app-instructor-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, LoaderComponent],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit {
  loading = true;
  saving = false;
  errorMessage = '';
  successMessage = '';
  
  initials = 'I';
  profile: any = null; 
  availableSkills: InstructorSkill[] = INSTRUCTOR_SKILLS;
  savedSkills: InstructorSkill[] = [];    // Controls the Left Side summary
  selectedSkills: InstructorSkill[] = [];

  profileForm!: FormGroup;

  // ── Injecting the service API helper class cleanly inside the constructor context ──
  constructor(private fb: FormBuilder, private instructorApi: InstructorApiService) {
    this.buildForm();
  }

  ngOnInit(): void {
    this.loadProfileData();
  }

  private buildForm(): void {
    this.profileForm = this.fb.group({
      dateOfBirth: ['', [Validators.required]],
      experience: [0, [Validators.required, Validators.min(0)]]
    });
  }

  private loadProfileData(): void {
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    // ── LIVE FETCH API CALL ──
    this.instructorApi.getProfile().subscribe({
      next: (data) => {
        this.profile = data;
        if (data.name) {
          this.initials = data.name.charAt(0).toUpperCase();
        }
        
        // Populate the active tracking selection storage indices out of database values
        this.savedSkills = data.skills ? [...data.skills] : [];
        this.selectedSkills = data.skills ? [...data.skills] : [];
        
        this.profileForm.patchValue({
          dateOfBirth: data.dateOfBirth || '',
          experience: data.experience || 0
        });
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || err?.message || 'Unable to populate your professional settings records.';
        this.loading = false;
      }
    });
  }

 // ── CHANGED: Parameter updated from 'string' to 'InstructorSkill' ──
  isSelected(skill: InstructorSkill): boolean {
    return this.selectedSkills.includes(skill);
  }

  // ── CHANGED: Parameter updated from 'string' to 'InstructorSkill' ──
  toggleSkill(skill: InstructorSkill): void {
    if (this.isSelected(skill)) {
      this.selectedSkills = this.selectedSkills.filter(s => s !== skill);
    } else {
      this.selectedSkills.push(skill);
    }
  }

  // ── LIVE UPDATE API CALL ──
  onSave(): void {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }

    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';

    const payload = {
      dateOfBirth: this.profileForm.value.dateOfBirth,
      experience: Number(this.profileForm.value.experience),
      skills: this.selectedSkills
    };

    this.instructorApi.updateProfile(payload).subscribe({
      next: (updatedProfile) => {
        this.saving = false;
        this.successMessage = 'Your teaching credentials and skillsets have been successfully updated!';
        
        // Update local memory objects immediately to synchronize on-screen rendering structures
        this.profile = updatedProfile;
        if (updatedProfile.skills) {
          this.savedSkills = [...updatedProfile.skills];
          this.selectedSkills = [...updatedProfile.skills];
        }
        // Auto-fade success alert window wrapper box elements after 4 seconds
        setTimeout(() => this.successMessage = '', 4000);
      },
      error: (err) => {
        this.saving = false;
        this.errorMessage = err?.error?.message || err?.message || 'Failed to save changes onto database registry clusters.';
      }
    });
  }
}