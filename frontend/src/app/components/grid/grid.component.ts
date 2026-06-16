import { Component, ElementRef, inject, signal, viewChild } from '@angular/core';
import { GameStore } from '../../core/game-store.service';
import { TileView } from '../../core/models';

/**
 * The board itself.
 *
 * Rendering: a CSS grid of {@code cols x rows} tiles, each coloured by its owner.
 * Live changes flow in through GameStore signals, so the template re-renders
 * only the tiles that actually changed.
 *
 * Interaction:
 *  - Click / tap a tile  -> capture it (store.claim).
 *  - Mouse wheel         -> zoom.
 *  - Drag the board      -> pan (we tell a click from a drag using a small
 *                           movement threshold, so panning never mis-fires a claim).
 */
@Component({
  selector: 'app-grid',
  standalone: true,
  templateUrl: './grid.component.html',
  styleUrl: './grid.component.css',
})
export class GridComponent {
  readonly store = inject(GameStore);

  // Zoom / pan state.
  readonly scale = signal(1);
  readonly panX = signal(0);
  readonly panY = signal(0);

  private readonly viewport = viewChild<ElementRef<HTMLElement>>('viewport');

  // Drag tracking.
  private pointerDown = false;
  private moved = false;
  private startX = 0;
  private startY = 0;
  private originX = 0;
  private originY = 0;

  trackById = (_: number, tile: TileView) => tile.id;

  isMine(tile: TileView): boolean {
    return tile.ownerId !== null && tile.ownerId === this.store.user()?.id;
  }

  // ----- click vs drag -----
  onPointerDown(event: PointerEvent): void {
    this.pointerDown = true;
    this.moved = false;
    this.startX = event.clientX;
    this.startY = event.clientY;
    this.originX = this.panX();
    this.originY = this.panY();
  }

  onPointerMove(event: PointerEvent): void {
    if (!this.pointerDown) return;
    const dx = event.clientX - this.startX;
    const dy = event.clientY - this.startY;
    if (!this.moved && Math.hypot(dx, dy) > 5) {
      this.moved = true; // it's a pan, not a click
    }
    if (this.moved) {
      this.panX.set(this.originX + dx);
      this.panY.set(this.originY + dy);
    }
  }

  onPointerUp(tile: TileView): void {
    this.pointerDown = false;
    if (!this.moved) {
      this.store.claim(tile); // a genuine click
    }
  }

  endPan(): void {
    this.pointerDown = false;
  }

  // ----- zoom -----
  onWheel(event: WheelEvent): void {
    event.preventDefault();
    const factor = event.deltaY < 0 ? 1.1 : 0.9;
    this.setScale(this.scale() * factor);
  }

  zoomIn(): void {
    this.setScale(this.scale() * 1.2);
  }

  zoomOut(): void {
    this.setScale(this.scale() * 0.8);
  }

  reset(): void {
    this.scale.set(1);
    this.panX.set(0);
    this.panY.set(0);
  }

  private setScale(value: number): void {
    this.scale.set(Math.min(3, Math.max(0.4, value)));
  }
}
