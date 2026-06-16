package com.example.grid.model;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * One block on the board.
 *
 * <p>The pair (x, y) is unique. A tile is "unclaimed" when {@code ownerId} is null.
 *
 * <p>{@code @Version} enables JPA optimistic locking. Each successful update bumps
 * the version. We also send this number to the browser so clients can ignore an
 * out-of-order (stale) update and always converge to the newest state.
 */
@Entity
@Table(
    name = "tiles",
    uniqueConstraints = @UniqueConstraint(columnNames = {"x", "y"}),
    indexes = @Index(name = "idx_tile_owner", columnList = "owner_id")
)
public class Tile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private int x;

    @Column(nullable = false, updatable = false)
    private int y;

    @Column(name = "owner_id")
    private Long ownerId;

    @Column(name = "owner_name")
    private String ownerName;

    /** Hex colour like "#4f46e5", copied from the owner so clients can paint instantly. */
    @Column(name = "color")
    private String color;

    @Column(name = "claimed_at")
    private Instant claimedAt;

    @Version
    @Column(nullable = false)
    private long version;

    protected Tile() {
        // Required by JPA.
    }

    public Tile(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /** Assigns this tile to a user. Centralised here so the rules are in one place. */
    public void captureBy(Long ownerId, String ownerName, String color, Instant when) {
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.color = color;
        this.claimedAt = when;
    }

    public Long getId() { return id; }
    public int getX() { return x; }
    public int getY() { return y; }
    public Long getOwnerId() { return ownerId; }
    public String getOwnerName() { return ownerName; }
    public String getColor() { return color; }
    public Instant getClaimedAt() { return claimedAt; }
    public long getVersion() { return version; }
}
