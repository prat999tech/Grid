package com.example.grid.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Body of POST /api/users. Colour is optional — we assign one if it is blank. */
public record RegisterUserRequest(
        @NotBlank @Size(max = 20) String name,
        String color
) {
}
