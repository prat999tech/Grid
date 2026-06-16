package com.example.grid.repository;

import com.example.grid.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Data access for users. */
public interface UserRepository extends JpaRepository<AppUser, Long> {

    /** Lets a returning player keep the same identity/colour by reusing their name. */
    Optional<AppUser> findByNameIgnoreCase(String name);
}
