import { Component, inject } from '@angular/core';
import { GameStore } from '../../core/game-store.service';

/** Live standings, pushed from the server after every capture. */
@Component({
  selector: 'app-leaderboard',
  standalone: true,
  templateUrl: './leaderboard.component.html',
  styleUrl: './leaderboard.component.css',
})
export class LeaderboardComponent {
  readonly store = inject(GameStore);

  isMe(userId: number): boolean {
    return userId === this.store.user()?.id;
  }
}
