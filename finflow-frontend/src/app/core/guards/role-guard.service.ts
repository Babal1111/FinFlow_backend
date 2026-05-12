import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class RoleGuardService {
  constructor(private readonly authService: AuthService) {}

  hasRole(expectedRole: string): boolean {
    return this.authService.getRoleFromToken() === expectedRole;
  }
}

export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const service = inject(RoleGuardService);
  const router = inject(Router);
  const expectedRole = route.data['role'] as string;
  if (service.hasRole(expectedRole)) {
    return true;
  }
  router.navigate(['/auth/login']);
  return false;
};
