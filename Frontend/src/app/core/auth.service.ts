import { computed, inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthResponse, SessionUser, initialsOf } from './models';

interface StoredSession {
  accessToken: string;
  refreshToken: string;
  expiresAt: number;
  user: SessionUser;
}

const STORAGE_KEY = 'mecanico.passport.session';

/**
 * Authentication is delegated to the Users microservice, which issues the JWT
 * that this portal and the Vehicle Passport API both accept.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private stored = this.readSession();
  private readonly _user = signal<SessionUser | null>(this.stored?.user ?? null);
  private readonly _token = signal<string | null>(this.stored?.accessToken ?? null);

  readonly user = this._user.asReadonly();
  readonly token = this._token.asReadonly();
  readonly isAuthenticated = computed(() => Boolean(this._token() && this._user()));
  readonly isAdmin = computed(() => this._user()?.role === 'ADMIN');
  readonly isManager = computed(
    () => this._user()?.role === 'ADMIN' || this._user()?.role === 'FLEET_MANAGER',
  );
  readonly initials = computed(() => initialsOf(this._user()));

  constructor() {
    if (this.stored && this.stored.expiresAt > 0 && this.stored.expiresAt <= Date.now()) {
      this.clearSession();
    }
  }

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${environment.usersApiBaseUrl}/auth/login`, { email, password })
      .pipe(tap((response) => this.persist(response)));
  }

  refreshProfile(): Observable<SessionUser> {
    return this.http
      .get<SessionUser>(`${environment.usersApiBaseUrl}/users/me`)
      .pipe(tap((user) => this.patchUser(user)));
  }

  patchUser(user: SessionUser): void {
    this._user.set(user);
    if (this.stored) {
      this.stored = { ...this.stored, user };
      localStorage.setItem(STORAGE_KEY, JSON.stringify(this.stored));
    }
  }

  logout(): void {
    const refreshToken = this.stored?.refreshToken;
    if (refreshToken) {
      this.http
        .post(`${environment.usersApiBaseUrl}/auth/logout`, { refreshToken })
        .subscribe({ error: () => undefined });
    }
    this.clearSession();
    void this.router.navigate(['/login']);
  }

  clearSession(): void {
    localStorage.removeItem(STORAGE_KEY);
    this.stored = null;
    this._token.set(null);
    this._user.set(null);
  }

  private persist(response: AuthResponse): void {
    const session: StoredSession = {
      accessToken: response.accessToken,
      refreshToken: response.refreshToken,
      expiresAt: Date.now() + response.expiresIn * 1000,
      user: response.user,
    };
    localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
    this.stored = session;
    this._token.set(session.accessToken);
    this._user.set(session.user);
  }

  private readSession(): StoredSession | null {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) {
        return null;
      }
      const parsed = JSON.parse(raw) as StoredSession;
      return parsed?.accessToken && parsed?.user ? parsed : null;
    } catch {
      return null;
    }
  }
}
