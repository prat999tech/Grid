package com.example.grid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Application entry point.
 *
 * @EnableAsync turns on Spring's support for running methods on a background
 * thread pool (used by {@code TileService} so a slow database write never
 * blocks the WebSocket thread that received the click).
 */
@SpringBootApplication
@EnableAsync
public class GridApplication {

    public static void main(String[] args) {
        SpringApplication.run(GridApplication.class, args);
    }
}
