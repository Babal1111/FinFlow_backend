import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth-guard.service';
import { roleGuard } from './core/guards/role-guard.service';

export const routes: Routes = [
  { path: '', pathMatch: 'full', loadComponent: () => import('./pages/home/home.component').then(m => m.HomeComponent) },
  {
    path: 'auth',
    loadChildren: () => import('./auth/auth.routes').then((m) => m.AUTH_ROUTES)
  },
  {
    path: 'applicant',
    canActivate: [authGuard, roleGuard],
    data: { role: 'APPLICANT' },
    loadChildren: () => import('./applicant/applicant.routes').then((m) => m.APPLICANT_ROUTES)
  },
  {
    path: 'admin',
    canActivate: [authGuard, roleGuard],
    data: { role: 'ADMIN' },
    loadChildren: () => import('./admin/admin.routes').then((m) => m.ADMIN_ROUTES)
  },
  { path: '**', redirectTo: 'auth/login' }
];
