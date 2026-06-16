import { Injectable } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Subject } from 'rxjs';
import { environment } from '../../environments/environment';
import { ErrorMessage, LeaderboardEntry, PresenceMessage, TileView } from './models';

/**
 * The real-time layer on the browser side.
 *
 * Wraps a STOMP-over-SockJS client and exposes incoming server messages as
 * RxJS streams. Components/stores subscribe to these streams instead of talking
 * to the socket directly (keeps socket details in one place — SRP).
 *
 * Subscriptions:
 *   /topic/tiles        -> a tile changed (broadcast to everyone)
 *   /topic/leaderboard  -> refreshed standings
 *   /topic/presence     -> online player count
 *   /user/queue/errors  -> private message to just this user (e.g. cooldown)
 *
 * Sending:
 *   /app/claim          -> "I clicked tile X"
 */
@Injectable({ providedIn: 'root' })
export class RealtimeService {
  private client?: Client;

  readonly tileUpdates$ = new Subject<TileView>();
  readonly leaderboard$ = new Subject<LeaderboardEntry[]>();
  readonly presence$ = new Subject<PresenceMessage>();
  readonly errors$ = new Subject<ErrorMessage>();
  readonly connected$ = new Subject<boolean>();

  connect(): void {
    if (this.client?.active) {
      return;
    }

    this.client = new Client({
      // SockJS handles the actual WebSocket (with fallbacks).
      webSocketFactory: () => new SockJS(environment.wsUrl) as any,
      // Auto-reconnect after 3s if the connection drops — important for real-time UX.
      reconnectDelay: 3000,
      onConnect: () => {
        this.connected$.next(true);

        this.client!.subscribe('/topic/tiles', (msg: IMessage) =>
          this.tileUpdates$.next(JSON.parse(msg.body) as TileView),
        );
        this.client!.subscribe('/topic/leaderboard', (msg: IMessage) =>
          this.leaderboard$.next(JSON.parse(msg.body) as LeaderboardEntry[]),
        );
        this.client!.subscribe('/topic/presence', (msg: IMessage) =>
          this.presence$.next(JSON.parse(msg.body) as PresenceMessage),
        );
        this.client!.subscribe('/user/queue/errors', (msg: IMessage) =>
          this.errors$.next(JSON.parse(msg.body) as ErrorMessage),
        );
      },
      onWebSocketClose: () => this.connected$.next(false),
    });

    this.client.activate();
  }

  /** Tell the server this user clicked a tile. */
  claim(tileId: number, userId: number): void {
    if (!this.client?.connected) {
      return;
    }
    this.client.publish({
      destination: '/app/claim',
      body: JSON.stringify({ tileId, userId }),
    });
  }
}
