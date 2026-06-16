package com.example.grid.dto;

import com.example.grid.model.Tile;

/**
 * What the browser sees for one tile. Used both in the initial snapshot and as
 * the live "a tile changed" message. We never expose JPA entities directly —
 * this keeps the API stable and hides database internals (DTO pattern).
 *
 * <p>{@code version} lets the client drop stale/out-of-order updates.
 */
public record TileView(
        Long id,
        int x,
        int y,
        Long ownerId,
        String ownerName,
        String color,
        String claimedAt,
        long version
) {
    /** Maps an entity to its view. One conversion point, reused everywhere. */
    public static TileView from(Tile tile) {
        return new TileView(
                tile.getId(),
                tile.getX(),
                tile.getY(),
                tile.getOwnerId(),
                tile.getOwnerName(),
                tile.getColor(),
                tile.getClaimedAt() == null ? null : tile.getClaimedAt().toString(),
                tile.getVersion()
        );
    }
}
