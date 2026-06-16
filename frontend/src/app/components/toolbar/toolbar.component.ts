import { Component, inject } from '@angular/core';
import { GameStore } from '../../core/game-store.service';

/** Top bar: who you are, how many tiles you own, who's online, connection state. */
@Component({
  selector: 'app-toolbar',
  standalone: true,
  templateUrl: './toolbar.component.html',
  styleUrl: './toolbar.component.css',
})
export class ToolbarComponent {
  readonly store = inject(GameStore);
}
