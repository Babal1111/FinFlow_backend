import { Injectable } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuardService {
  constructor(private readonly authService: AuthService) {}

  canActivate(): boolean {
    return this.authService.hasValidToken();
  }
}

export const authGuard: CanActivateFn = () => {
  const service = inject(AuthGuardService);
  const router = inject(Router);
  if (service.canActivate()) {
    return true;
  }
  router.navigate(['/auth/login']);
  return false;
};
