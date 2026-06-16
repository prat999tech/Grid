import { Injectable, computed, inject, signal } from '@angular/core';
import { ApiService } from './api.service';
import { RealtimeService } from './realtime.service';
import { ErrorMessage, LeaderboardEntry, TileView, User } from './models';

/** Must match grid.cooldown-ms on the backend (used for client-side feedback). */
const COOLDOWN_MS = 800;

/**
 * Single source of truth for the running game, built on Angular signals.
 *
 * Components read these signals and re-render automatically; they never touch
 * the socket or HTTP directly. This keeps UI components dumb and the data flow
 * easy to follow.
 */
@Injectable({ providedIn: 'root' })
export class GameStore {
  private readonly api = inject(ApiService);
  private readonly realtime = inject(RealtimeService);

  // ----- reactive state -----
  readonly user = signal<User | null>(null);
  readonly tiles = signal<TileView[]>([]);
  readonly cols = signal(0);
  readonly rows = signal(0);
  readonly leaderboard = signal<LeaderboardEntry[]>([]);
  readonly online = signal(0);
  readonly connected = signal(false);
  readonly toast = signal<ErrorMessage | null>(null);
  readonly cooldownUntil = signal(0);
  /** Tile ids that changed in the last moment — drives the "pop" animation. */
  readonly recentlyChanged = signal<Set<number>>(new Set());

  /** How many tiles the current player owns (derived automatically). */
  readonly myTiles = computed(() => {
    const me = this.user();
    if (!me) return 0;
    return this.tiles().filter((t) => t.ownerId === me.id).length;
  });

  /** Fast lookup from tile id to its position in the tiles array. */
  private indexById = new Map<number, number>();

  /** Called once after login: load the board, then go live. */
  start(user: User): void {
    this.user.set(user);

    this.api.getGrid().subscribe((snapshot) => {
      this.cols.set(snapshot.cols);
      this.rows.set(snapshot.rows);
      this.indexById = new Map(snapshot.tiles.map((t, i) => [t.id, i]));
      this.tiles.set(snapshot.tiles);
    });
    this.api.getLeaderboard().subscribe((lb) => this.leaderboard.set(lb));

    this.wireRealtime();
    this.realtime.connect();
  }

  /** Send a capture for a tile, respecting the local cooldown. */
  claim(tile: TileView): void {
    const me = this.user();
    if (!me) return;

    if (Date.now() < this.cooldownUntil()) {
      this.showToast({ code: 'COOLDOWN', message: 'Cooling down…' });
      return;
    }
    if (tile.ownerId === me.id) {
      return; // already mine — nothing to do
    }

    this.cooldownUntil.set(Date.now() + COOLDOWN_MS);
    this.realtime.claim(tile.id, me.id);
  }

  remainingCooldownMs(): number {
    return Math.max(0, this.cooldownUntil() - Date.now());
  }

  private wireRealtime(): void {
    this.realtime.connected$.subscribe((c) => this.connected.set(c));
    this.realtime.presence$.subscribe((p) => this.online.set(p.online));
    this.realtime.leaderboard$.subscribe((lb) => this.leaderboard.set(lb));
    this.realtime.errors$.subscribe((e) => this.showToast(e));
    this.realtime.tileUpdates$.subscribe((t) => this.applyTileUpdate(t));
  }

  /** Apply a single live tile change, ignoring anything older than what we have. */
  private applyTileUpdate(updated: TileView): void {
    const idx = this.indexById.get(updated.id);
    if (idx === undefined) return;

    const current = this.tiles();
    // Out-of-order safety: never overwrite newer state with an older message.
    if (current[idx].version > updated.version) return;

    const copy = current.slice();
    copy[idx] = updated;
    this.tiles.set(copy);
    this.markChanged(updated.id);
  }

  /** Briefly flag a tile as "just changed" so the grid can animate it. */
  private markChanged(id: number): void {
    const next = new Set(this.recentlyChanged());
    next.add(id);
    this.recentlyChanged.set(next);
    setTimeout(() => {
      const after = new Set(this.recentlyChanged());
      after.delete(id);
      this.recentlyChanged.set(after);
    }, 550);
  }

  private showToast(error: ErrorMessage): void {
    this.toast.set(error);
    setTimeout(() => this.toast.set(null), 2000);
  }
}
