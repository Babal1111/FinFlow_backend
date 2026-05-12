import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './signup.component.html',
  styleUrl: './signup.component.css'
})
export class SignupComponent {
  loading = false;
  showPassword = false;
  form = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    confirmPassword: ['', [Validators.required]]
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly authService: AuthService,
    private readonly router: Router
  ) { }

  submit(): void {
    if (this.form.invalid || this.loading) return;
    const { name, email, password, confirmPassword } = this.form.getRawValue();
    if (password !== confirmPassword) return;
    this.loading = true;
    this.authService.signup({ name: name!, email: email!, password: password!, role: 'APPLICANT' }).subscribe({
      next: () => this.router.navigate(['/auth/login']),
      error: () => {
        this.loading = false;
      }
    });
  }
}
