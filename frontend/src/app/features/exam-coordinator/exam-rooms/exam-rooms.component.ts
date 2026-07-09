import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { CoordinatorApiService } from '../services/coordinator-api.service';
import { Exam } from '../../student/models/student.model';
import { ExamRoomOutput, PhysicalRoomOutput } from '../models/coordinator.model';
import { LoaderComponent } from '../../../shared/components/loader/loader.component';

@Component({
  selector: 'app-coordinator-exam-rooms',
  standalone: true,
  imports: [CommonModule, FormsModule, LoaderComponent],
  templateUrl: './exam-rooms.component.html',
  styleUrl: './exam-rooms.component.css'
})
export class ExamRoomsComponent implements OnInit {
  exams: Exam[] = [];
  physicalRooms: PhysicalRoomOutput[] = [];
  allocations: ExamRoomOutput[] = [];
  selectedExamAllocations: ExamRoomOutput[] = [];
  selectedExamId: number | null = null;

  loading = true;
  submitting = false;
  allocationsLoading = false;
  errorMessage = '';
  successMessage = '';

  // Form Fields - Create Room
  roomName = '';
  location = '';
  capacity = 40;

  // Form Fields - Allocate Exam Room
  allocateExamId: number | null = null;
  allocateRoomId: number | null = null;

  constructor(private coordinatorApi: CoordinatorApiService) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.errorMessage = '';

    this.coordinatorApi.getAllRooms().subscribe({
      next: (rooms) => {
        this.physicalRooms = rooms;
        this.coordinatorApi.searchExams().subscribe({
          next: (exams) => {
            this.exams = exams;
            this.loading = false;
          },
          error: (err) => {
            this.errorMessage = err?.message || 'Failed to load exams list.';
            this.loading = false;
          }
        });
      },
      error: (err) => {
        this.errorMessage = err?.message || 'Failed to load physical rooms.';
        this.loading = false;
      }
    });
  }

  loadAllocationsForExam(): void {
    if (!this.selectedExamId) {
      this.selectedExamAllocations = [];
      return;
    }

    this.allocationsLoading = true;
    this.errorMessage = '';
    this.selectedExamAllocations = [];

    this.coordinatorApi.getRoomsForExam(this.selectedExamId).subscribe({
      next: (allocs) => {
        this.selectedExamAllocations = allocs;
        this.allocationsLoading = false;
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || err?.message || 'Failed to load room allocations for the selected exam.';
        this.allocationsLoading = false;
      }
    });
  }

  createRoom(): void {
    if (!this.roomName || !this.location || !this.capacity) return;

    this.submitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.coordinatorApi.createRoom({
      roomName: this.roomName,
      location: this.location,
      capacity: this.capacity
    }).subscribe({
      next: (newRoom) => {
        this.successMessage = `Successfully provisioned physical room: "${newRoom.roomName}"!`;
        this.roomName = '';
        this.location = '';
        this.capacity = 40;
        this.loadData(); // reload rooms dropdown
        this.submitting = false;
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || err?.message || 'Failed to create room.';
        this.submitting = false;
      }
    });
  }

  allocateRoom(): void {
    if (!this.allocateExamId || !this.allocateRoomId) return;

    this.submitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.coordinatorApi.createAndAllocate({
      physicalRoomId: this.allocateRoomId,
      examId: this.allocateExamId,
      roomNumber: 1
    }).subscribe({
      next: (allocation) => {
        this.successMessage = `Successfully allocated room "${allocation.roomName}" for exam "${allocation.examTitle}"! Distributed ${allocation.studentsAllocated} students.`;
        this.allocateExamId = null;
        this.allocateRoomId = null;
        this.selectedExamId = allocation.examId;
        this.loadData(); // reload rooms dropdown (since status is updated)
        this.loadAllocationsForExam(); // reload allocations view
        this.submitting = false;
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || err?.message || 'Allocation failed. Ensure the room has enough capacity and the exam status is ACTIVE.';
        this.submitting = false;
      }
    });
  }

  getAvailableRooms(): PhysicalRoomOutput[] {
    return this.physicalRooms.filter(r => r.status === 'AVAILABLE');
  }
}
