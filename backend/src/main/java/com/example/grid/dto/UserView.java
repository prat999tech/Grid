package com.example.grid.dto;

import com.example.grid.model.AppUser;

/** Public view of a user returned to the browser after login. */
public record UserView(Long id, String name, String color) {

    public static UserView from(AppUser user) {
        return new UserView(user.getId(), user.getName(), user.getColor());
    }
}
