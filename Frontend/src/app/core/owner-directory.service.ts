import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { catchError, Observable, of, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { InternalUser } from './models';

/**
 * Resolves logical owner IDs through the Users service internal lookup endpoint.
 * Failures are non-fatal: the UI simply falls back to "Owner #id".
 */
@Injectable({ providedIn: 'root' })
export class OwnerDirectory {
  private readonly http = inject(HttpClient);
  private readonly cache = new Map<number, InternalUser>();

  resolve(id: number): Observable<InternalUser | null> {
    const cached = this.cache.get(id);
    if (cached) {
      return of(cached);
    }
    return this.http
      .get<InternalUser>(`${environment.usersApiBaseUrl}/internal/users/${id}`)
      .pipe(
        tap((user) => this.cache.set(id, user)),
        catchError(() => of(null)),
      );
  }
}
