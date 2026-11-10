
package com.nexoralabs.navalhahub.User.controller;

import com.nexoralabs.navalhahub.User.dto.UserRequest;
import com.nexoralabs.navalhahub.User.dto.UserResponse;
import com.nexoralabs.navalhahub.User.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/barbershops/{barbershopId}/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    // CREATE
    @PostMapping
    @PreAuthorize("@tenantAuthorization.canManageUsers()")
    public ResponseEntity<UserResponse> create(
            @PathVariable("barbershopId") String barbershopId,
            @Valid @RequestBody UserRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.createUser(barbershopId, request));
    }

    // FIND ALL
    @GetMapping
    public ResponseEntity<Page<UserResponse>> findAll(
             @PathVariable("barbershopId") String barbershopId,
            Pageable pageable
    ) {

        return ResponseEntity.ok(
                userService.findAllUsers(barbershopId, pageable)
        );
    }

    // FIND BY ID
    @GetMapping("/{identityId}")
    public ResponseEntity<UserResponse> findById(
             @PathVariable("barbershopId") String barbershopId,
            @PathVariable("identityId") UUID identityId
    ) {

        return ResponseEntity.ok(
                userService.findUser(barbershopId, identityId)
        );
    }

    // UPDATE
    @PutMapping("/{identityId}")
    @PreAuthorize("@tenantAuthorization.canManageUsers()")
    public ResponseEntity<UserResponse> update(
            @PathVariable("barbershopId") String barbershopId,
            @PathVariable("identityId") UUID identityId,
            @Valid @RequestBody UserRequest request
    ) {

        return ResponseEntity.ok(
                userService.updateUser(
                        barbershopId,
                        identityId,
                        request
                )
        );
    }

    // DELETE
    @DeleteMapping("/{identityId}")
    @PreAuthorize("@tenantAuthorization.canManageUsers()")
    public ResponseEntity<Void> delete(
             @PathVariable("barbershopId") String barbershopId,
            @PathVariable("identityId") UUID identityId
    ) {

        userService.deleteUser(barbershopId, identityId);

        return ResponseEntity.noContent().build();
    }
}
