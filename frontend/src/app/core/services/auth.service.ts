import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

import { AuthApiService } from '../../features/auth/services/auth-api.service';
import { TokenStorageService } from './token-storage.service';
import { LoginRequest, LoginResponse } from '../models/login.model';
import { RegisterRequest, RegisterResponse } from '../models/user.model';
import { Role } from '../models/role.enum';

@Injectable({ providedIn: 'root' })
export class AuthService {

  constructor(
    private authApi: AuthApiService,
    private tokenStorage: TokenStorageService,
    private router: Router
  ) {}

  login(payload: LoginRequest): Observable<LoginResponse> {
    return this.authApi.login(payload).pipe(
      tap((res) => this.tokenStorage.saveSession(res))
    );
  }

  register(payload: RegisterRequest): Observable<RegisterResponse> {
    return this.authApi.register(payload);
  }

  logout(): void {
    this.tokenStorage.clear();
    this.router.navigate(['/auth/login']);
  }

  isLoggedIn(): boolean {
    return !!this.tokenStorage.getToken();
  }

  getRole(): string | null {
    return this.tokenStorage.getRole();
  }

  getCurrentUser(): LoginResponse | null {
    return this.tokenStorage.getUser();
  }

  redirectToDashboard(): void {
    switch (this.getRole()) {
      case Role.STUDENT:
        this.router.navigate(['/student']);
        break;
      case Role.INSTRUCTOR:
        this.router.navigate(['/instructor']);
        break;
      case Role.REGISTRAR:
        this.router.navigate(['/registrar']);
        break;
      case Role.EXAM_COORDINATOR:
        this.router.navigate(['/exam-coordinator']);
        break;
      default:
        this.router.navigate(['/auth/login']);
    }
  }
}
