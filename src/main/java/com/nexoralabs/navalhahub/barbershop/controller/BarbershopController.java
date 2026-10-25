package com.nexoralabs.navalhahub.barbershop.controller;

import com.nexoralabs.navalhahub.barbershop.dto.request.BarbershopRequest;
import com.nexoralabs.navalhahub.barbershop.dto.response.BarbershopResponse;
import com.nexoralabs.navalhahub.barbershop.services.BarbershopService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/barbershops")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
public class BarbershopController {

    private final BarbershopService service;

    @PostMapping
    public ResponseEntity<BarbershopResponse> create(
            @Valid @RequestBody BarbershopRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {

        BarbershopResponse response = service.create(
                request,
                getUserId(jwt)
        );

        URI location = URI.create(
                "/api/barbershops/" + response.getId()
        );

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<BarbershopResponse>> findAll(
            Pageable pageable,
            @AuthenticationPrincipal Jwt jwt
    ) {

        return ResponseEntity.ok(
                service.findAllByOwner(getUserId(jwt), pageable)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<BarbershopResponse> findById(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt
    ) {

        return ResponseEntity.ok(
                service.findById(id, getUserId(jwt))
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<BarbershopResponse> update(
            @PathVariable String id,
            @Valid @RequestBody BarbershopRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {

        return ResponseEntity.ok(
                service.update(id, request, getUserId(jwt))
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<BarbershopResponse> updateStatus(
            @PathVariable String id,
            @RequestParam boolean active,
            @AuthenticationPrincipal Jwt jwt
    ) {

        return ResponseEntity.ok(
                service.updateStatus(id, active, getUserId(jwt))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt
    ) {

        service.delete(id, getUserId(jwt));

        return ResponseEntity.noContent().build();
    }

    private UUID getUserId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}