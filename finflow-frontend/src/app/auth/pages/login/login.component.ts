import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  loading = false;
  form = this.fb.group({
    email: ['', [Validators.required, Validators.email,]],
    password: ['', [Validators.required]]
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly authService: AuthService,
    private readonly router: Router
  ) { }
  
  submit(): void {
    if (this.form.invalid || this.loading) return;
    this.loading = true;
    this.authService.login(this.form.getRawValue() as { email: string; password: string }).subscribe({
      next: (res) => {
        this.authService.setSession(res);
        this.router.navigate([res.role === 'ADMIN' ? '/admin/dashboard' : '/applicant/dashboard']);
      },
      error: () => {
        this.loading = false;
      }
    });
  }
}
