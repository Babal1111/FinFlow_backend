import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ApplicationResponse } from '../../../models/application.model';
import { AdminService } from '../../../services/admin.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})

export class AdminDashboardComponent {
  loading = true;
  applications: ApplicationResponse[] = [];

  // Pagination State
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;

  constructor(private readonly adminService: AdminService) {
    this.loadApplications();
  }

  loadApplications() {
    this.loading = true;
    this.adminService.getApplications(this.currentPage, this.pageSize).subscribe({
      next: (data) => {
        this.applications = data.content || [];
        this.totalPages = data.totalPages;
        this.totalElements = data.totalElements;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  nextPage() {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadApplications();
    }
  }

  prevPage() {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadApplications();
    }
  }

  getCount(status: string): number {
    return this.applications.filter(a => a.status === status).length;
  }
}
