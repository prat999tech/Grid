package com.example.grid.config;

import com.example.grid.model.Tile;
import com.example.grid.repository.TileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates the empty board on first startup. If the tiles table already has rows
 * (the app was started before), it does nothing, so existing captures survive a
 * restart.
 */
@Component
public class GridInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(GridInitializer.class);

    private final TileRepository tileRepository;
    private final int cols;
    private final int rows;

    public GridInitializer(TileRepository tileRepository,
                           @Value("${grid.cols}") int cols,
                           @Value("${grid.rows}") int rows) {
        this.tileRepository = tileRepository;
        this.cols = cols;
        this.rows = rows;
    }

    @Override
    public void run(String... args) {
        if (tileRepository.count() > 0) {
            log.info("Board already exists ({} tiles) — skipping seed.", tileRepository.count());
            return;
        }
        List<Tile> tiles = new ArrayList<>(cols * rows);
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                tiles.add(new Tile(x, y));
            }
        }
        tileRepository.saveAll(tiles);
        log.info("Seeded empty board: {} x {} = {} tiles.", cols, rows, tiles.size());
    }
}
