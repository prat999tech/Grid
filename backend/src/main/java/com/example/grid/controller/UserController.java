package com.example.grid.controller;

import com.example.grid.dto.RegisterUserRequest;
import com.example.grid.dto.UserView;
import com.example.grid.model.AppUser;
import com.example.grid.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/** Login / sign-up endpoint. */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserView register(@Valid @RequestBody RegisterUserRequest request) {
        AppUser user = userService.register(request.name(), request.color());
        return UserView.from(user);
    }
}
