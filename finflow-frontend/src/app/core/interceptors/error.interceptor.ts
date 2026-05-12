import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { ToastService } from '../services/toast.service';
import { AuthService } from '../../services/auth.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const toast = inject(ToastService);
  const authService = inject(AuthService);

  return next(req).pipe(
    catchError((error) => {
      if (error.status === 401) {
        authService.clearToken();
        router.navigate(['/auth/login']);
      } else if (error.status === 403) {
        toast.error('Access denied.');
      } else if (error.status >= 500) {
        toast.error('Something went wrong. Please try again.');
      }
      return throwError(() => error);
    })
  );
};
