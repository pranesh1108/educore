import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

import { LoaderComponent } from '../../../shared/components/loader/loader.component';

@Component({
  selector: 'app-registrar-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, LoaderComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {

  loading = true;
  errorMessage = '';

  constructor() {}

  ngOnInit(): void {
    
    this.loading = false;
    this.errorMessage = '';
  }
}