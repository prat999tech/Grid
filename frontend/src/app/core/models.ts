/** Shared TypeScript types that mirror the backend DTOs. */

export interface User {
  id: number;
  name: string;
  color: string;
}

export interface TileView {
  id: number;
  x: number;
  y: number;
  ownerId: number | null;
  ownerName: string | null;
  color: string | null;
  claimedAt: string | null;
  /** Increases on every change; used to ignore stale/out-of-order updates. */
  version: number;
}

export interface GridSnapshot {
  cols: number;
  rows: number;
  tiles: TileView[];
}

export interface LeaderboardEntry {
  userId: number;
  name: string;
  color: string;
  tiles: number;
}

export interface PresenceMessage {
  online: number;
}

export interface ErrorMessage {
  code: string;
  message: string;
}
