import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthResponse, LoginRequest, SignupRequest } from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly baseUrl = `${environment.apiUrl}/auth`;

  constructor(private readonly http: HttpClient) {}

  signup(payload: SignupRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/signup`, payload);
  }

  login(payload: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, payload);
  }

  setSession(res: AuthResponse): void {
    const normalized = this.normalizeToken(res.token);
    if (!normalized) {
      this.clearToken();
      return;
    }
    localStorage.setItem('finflow_token', normalized);
    localStorage.setItem('finflow_role', res.role);
    localStorage.setItem('finflow_userId', res.userId.toString());
  }

  clearToken(): void {
    localStorage.removeItem('finflow_token');
    localStorage.removeItem('finflow_role');
    localStorage.removeItem('finflow_userId');
  }

  getToken(): string | null {
    const rawToken = localStorage.getItem('finflow_token');
    return this.normalizeToken(rawToken);
  }

  getRoleFromToken(): string | null {
    const payload = this.getPayload();
    return payload ? payload['role'] : null;
  }

  getEmailFromToken(): string | null {
    const payload = this.getPayload();
    return payload ? (payload['sub'] ?? payload['email']) : null;
  }

  isTokenExpired(): boolean {
    const payload = this.getPayload();
    if (!payload || !payload['exp']) {
      return true;
    }
    const nowInSeconds = Math.floor(Date.now() / 1000);
    return payload['exp'] <= nowInSeconds;
  }

  hasValidToken(): boolean {
    const token = this.getToken();
    if (!token) {
      return false;
    }
    if (this.isTokenExpired()) {
      this.clearToken();
      return false;
    }
    return true;
  }

  private getPayload(): Record<string, any> | null {
    const token = this.getToken();
    if (!token) {
      return null;
    }
    const parts = token.split('.');
    if (parts.length < 2) {
      return null;
    }
    try {
      const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
      const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=');
      const payload = JSON.parse(atob(padded));
      return payload && typeof payload === 'object' ? payload : null;
    } catch {
      return null;
    }
  }

  private normalizeToken(token: string | null | undefined): string | null {
    if (!token) {
      return null;
    }
    const trimmed = token.trim();
    if (!trimmed || trimmed === 'undefined' || trimmed === 'null') {
      return null;
    }
    return trimmed.startsWith('Bearer ') ? trimmed.slice(7).trim() : trimmed;
  }
}
