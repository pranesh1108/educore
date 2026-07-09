import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { TokenStorageService } from '../services/token-storage.service';
import { Role } from '../models/role.enum';

// Usage in routes: canActivate: [roleGuard([Role.STUDENT])]
export const roleGuard = (allowedRoles: Role[]): CanActivateFn => {
  return () => {
    const router = inject(Router);
    const tokenStorage = inject(TokenStorageService);
    const userRole = tokenStorage.getRole() as Role | null;

    if (!userRole || !allowedRoles.includes(userRole)) {
      router.navigate(['/auth/login']);
      return false;
    }

    return true;
  };
};
