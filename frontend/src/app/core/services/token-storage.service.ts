import { Injectable } from '@angular/core';
import { LoginResponse } from '../models/login.model';

const TOKEN_KEY = 'educore_token';
const ROLE_KEY = 'educore_role';
const USER_KEY = 'educore_user';

@Injectable({ providedIn: 'root' })
export class TokenStorageService {

  saveSession(user: LoginResponse): void {
    localStorage.setItem(TOKEN_KEY, user.token);
    localStorage.setItem(ROLE_KEY, user.role);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  getRole(): string | null {
    return localStorage.getItem(ROLE_KEY);
  }

  getUser(): LoginResponse | null {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) as LoginResponse : null;
  }

  clear(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(ROLE_KEY);
    localStorage.removeItem(USER_KEY);
  }
}
