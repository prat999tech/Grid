package com.example.grid.service;

import com.example.grid.model.AppUser;
import com.example.grid.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** Creates and looks up players. */
@Service
public class UserService {

    /** A friendly, high-contrast palette so different players are easy to tell apart. */
    private static final List<String> PALETTE = List.of(
            "#ef4444", "#f97316", "#f59e0b", "#eab308", "#84cc16", "#22c55e",
            "#10b981", "#14b8a6", "#06b6d4", "#3b82f6", "#6366f1", "#8b5cf6",
            "#a855f7", "#d946ef", "#ec4899", "#f43f5e");

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Logs a player in. If the name already exists we reuse that account (so the
     * same person keeps their colour); otherwise we create a new one. A blank
     * colour gets a random one from the palette.
     */
    @Transactional
    public AppUser register(String name, String requestedColor) {
        String cleanName = name.trim();
        return userRepository.findByNameIgnoreCase(cleanName)
                .orElseGet(() -> userRepository.save(
                        new AppUser(cleanName, pickColor(requestedColor))));
    }

    private String pickColor(String requestedColor) {
        if (requestedColor != null && requestedColor.matches("#[0-9a-fA-F]{6}")) {
            return requestedColor;
        }
        return PALETTE.get(ThreadLocalRandom.current().nextInt(PALETTE.size()));
    }
}
