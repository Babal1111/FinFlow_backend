import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  role: 'ADMIN' | 'APPLICANT' | null = null;
  showShell = false;
  userEmail = '';

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router
  ) {
    this.refreshShellState();
    this.router.events.pipe(filter((event) => event instanceof NavigationEnd)).subscribe(() => {
      this.refreshShellState();
    });
  }

  get homePath(): string {
    return this.role === 'ADMIN' ? '/admin/dashboard' : '/applicant/dashboard';
  }

  logout(): void {
    this.authService.clearToken();
    this.role = null;
    this.showShell = false;
    this.router.navigate(['/auth/login']);
  }

  private refreshShellState(): void {
    if (!this.authService.hasValidToken()) {
      this.role = null;
      this.showShell = false;
      this.userEmail = '';
      return;
    }
    const role = this.authService.getRoleFromToken();
    this.role = role === 'ADMIN' ? 'ADMIN' : role === 'APPLICANT' ? 'APPLICANT' : null;
    this.showShell = !!this.role && !this.router.url.startsWith('/auth') && this.router.url !== '/';
    // Try to get email from token
    this.userEmail = this.authService.getEmailFromToken() || (this.role === 'ADMIN' ? 'admin@finflow.in' : '');
  }
}
