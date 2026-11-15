import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs';
import { API_URL } from './config';

export interface SessionUser {
  id: number;
  fullName: string;
  username: string;
  role: 'DUENO' | 'EMPLEADA';
}

export interface LoginResponse {
  token: string;
  tokenType: string;
  expiresInSeconds: number;
  user: SessionUser;
}

const TOKEN_KEY = 'it_token';
const USER_KEY = 'it_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);

  readonly token = signal<string | null>(localStorage.getItem(TOKEN_KEY));
  readonly user = signal<SessionUser | null>(this.readUser());
  readonly isLoggedIn = computed(() => this.token() !== null);

  login(username: string, password: string) {
    return this.http.post<LoginResponse>(`${API_URL}/auth/login`, { username, password }).pipe(
      tap((res) => {
        localStorage.setItem(TOKEN_KEY, res.token);
        localStorage.setItem(USER_KEY, JSON.stringify(res.user));
        this.token.set(res.token);
        this.user.set(res.user);
      }),
    );
  }

  logout() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.token.set(null);
    this.user.set(null);
  }

  private readUser(): SessionUser | null {
    try {
      return JSON.parse(localStorage.getItem(USER_KEY) ?? 'null');
    } catch {
      return null;
    }
  }
}