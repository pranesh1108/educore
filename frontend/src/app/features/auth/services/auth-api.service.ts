import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment.development';
import { LoginRequest, LoginResponse } from '../../../core/models/login.model';
import { RegisterRequest, RegisterResponse } from '../../../core/models/user.model';

@Injectable({ providedIn: 'root' })
export class AuthApiService {

  private readonly baseUrl = `${environment.apiBaseUrl}/user`;

  constructor(private http: HttpClient) {}

  login(payload: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.baseUrl}/login`, payload);
  }

  register(payload: RegisterRequest): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(`${this.baseUrl}/register`, payload);
  }
}
