import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { LoginRequest, LoginResponse } from '../models/auth.model';

const TOKEN_KEY = 'es_token';
const USER_KEY = 'es_username';

/**
 * Token store + login/logout (DESIGN §6.4). Token survives reload via sessionStorage.
 * Replaces the desktop in-memory username + LoggedOut flag (REQUIREMENTS §12).
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly base = `${environment.apiBaseUrl}/auth`;
  readonly username = signal<string | null>(sessionStorage.getItem(USER_KEY));

  constructor(private http: HttpClient) {}

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.base}/login`, credentials).pipe(
      tap((res) => {
        sessionStorage.setItem(TOKEN_KEY, res.token);
        sessionStorage.setItem(USER_KEY, res.username);
        this.username.set(res.username);
      })
    );
  }

  logout(): void {
    // Stateless logout (FR-6, D1): notify the server best-effort, then discard the token.
    this.http.post(`${this.base}/logout`, {}).subscribe({ error: () => {} });
    this.clearSession();
  }

  clearSession(): void {
    sessionStorage.removeItem(TOKEN_KEY);
    sessionStorage.removeItem(USER_KEY);
    this.username.set(null);
  }

  get token(): string | null {
    return sessionStorage.getItem(TOKEN_KEY);
  }

  isAuthenticated(): boolean {
    return this.token !== null;
  }

  currentUsername(): string | null {
    return this.username();
  }
}
