import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService } from '../../../services/admin.service';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './users.component.html',
  styleUrl: './users.component.css'
})
export class UsersComponent implements OnInit {
  users: any[] = [];
  loading = true;
  error = '';

  constructor(private readonly adminService: AdminService) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.adminService.getUsers().subscribe({
      next: (data: any[]) => {
        this.users = data;
        this.loading = false;
      },
      error: (err: any) => {
        console.error('Failed to load users', err);
        this.error = 'Failed to load user list. Please ensure all backend services are running.';
        this.loading = false;
      }
    });
  }
}
