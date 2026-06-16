import { Component, inject } from '@angular/core';
import { GameStore } from './core/game-store.service';
import { User } from './core/models';
import { LoginComponent } from './components/login/login.component';
import { ToolbarComponent } from './components/toolbar/toolbar.component';
import { GridComponent } from './components/grid/grid.component';
import { LeaderboardComponent } from './components/leaderboard/leaderboard.component';

/**
 * Root component. Shows the login screen until a user joins, then swaps to the
 * live board. It owns no game logic itself — that all lives in GameStore.
 */
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [LoginComponent, ToolbarComponent, GridComponent, LeaderboardComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
})
export class AppComponent {
  readonly store = inject(GameStore);

  onLoggedIn(user: User): void {
    this.store.start(user);
  }
}
