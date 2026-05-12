import { Routes } from '@angular/router';
import { AdminDashboardComponent } from './pages/dashboard/dashboard.component';
import { ReviewComponent } from './pages/review/review.component';
import { ReportsComponent } from './pages/reports/reports.component';
import { UsersComponent } from './pages/users/users.component';

export const ADMIN_ROUTES: Routes = [
  { path: 'dashboard', component: AdminDashboardComponent },
  { path: 'review/:id', component: ReviewComponent },
  { path: 'reports', component: ReportsComponent },
  { path: 'users', component: UsersComponent }
];
