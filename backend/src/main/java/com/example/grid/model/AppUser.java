package com.example.grid.model;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * A player. Created the first time someone enters a name on the login screen.
 * Each user has a colour, used to paint every tile they capture.
 */
@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /** Hex colour, e.g. "#ef4444". */
    @Column(nullable = false)
    private String color;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected AppUser() {
        // Required by JPA.
    }

    public AppUser(String name, String color) {
        this.name = name;
        this.color = color;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getColor() { return color; }
    public Instant getCreatedAt() { return createdAt; }
}
