package com.example.grid.repository;

import com.example.grid.dto.LeaderboardEntry;
import com.example.grid.model.Tile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Data access for tiles. Spring Data JPA generates the implementation for us
 * (Repository pattern) — we only declare the queries we need.
 */
public interface TileRepository extends JpaRepository<Tile, Long> {

    /** Whole board ordered top-left to bottom-right, used for the initial snapshot. */
    List<Tile> findAllByOrderByYAscXAsc();

    long countByOwnerId(Long ownerId);

    /**
     * Leaderboard built directly in the database: how many tiles each owner holds,
     * highest first. Far cheaper than loading every tile into memory.
     */
    @Query("""
        select new com.example.grid.dto.LeaderboardEntry(
            t.ownerId, t.ownerName, t.color, count(t))
        from Tile t
        where t.ownerId is not null
        group by t.ownerId, t.ownerName, t.color
        order by count(t) desc
        """)
    List<LeaderboardEntry> leaderboard();
}
