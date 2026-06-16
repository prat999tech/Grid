import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { GridSnapshot, LeaderboardEntry, User } from './models';

/**
 * Plain REST calls to the backend (used for login and the initial board load).
 * Live updates are handled separately by RealtimeService.
 */
@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiBase;

  login(name: string, color?: string): Observable<User> {
    return this.http.post<User>(`${this.base}/users`, { name, color });
  }

  getGrid(): Observable<GridSnapshot> {
    return this.http.get<GridSnapshot>(`${this.base}/grid`);
  }

  getLeaderboard(): Observable<LeaderboardEntry[]> {
    return this.http.get<LeaderboardEntry[]>(`${this.base}/leaderboard`);
  }
}
