package com.nexoralabs.navalhahub.User.controller;

import com.nexoralabs.navalhahub.User.dto.UserRequest;
import com.nexoralabs.navalhahub.User.dto.UserResponse;
import com.nexoralabs.navalhahub.User.service.UserService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/barbershops/{barbershopId}/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("@tenantAuthorization.canManageUsers()")
    public ResponseEntity<UserResponse> create(
            @PathVariable String barbershopId,
            @Valid @RequestBody UserRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        userService.createUser(
                                barbershopId,
                                request
                        )
                );
    }
}