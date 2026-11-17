package com.nexoralabs.navalhahub.barbershop.controller;

import com.nexoralabs.navalhahub.barbershop.dto.request.BarbershopRequest;
import com.nexoralabs.navalhahub.barbershop.dto.response.BarbershopResponse;
import com.nexoralabs.navalhahub.barbershop.services.BarbershopService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/barbershops")
@RequiredArgsConstructor
public class BarbershopController {

    private final BarbershopService barbershopService;

    // ---------------------------------------------------------------
    // Sem tenant (o TenantMembershipFilter nao e aplicado: so autenticado)
    // ---------------------------------------------------------------

    // CREATE: quem cria vira OWNER
    @PostMapping
    public ResponseEntity<BarbershopResponse> save(
            @Valid @RequestBody BarbershopRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var response = barbershopService.save(request, identityId(jwt));

        var uri = UriComponentsBuilder
                .fromPath("/api/barbershops/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(uri).body(response);
    }

    // LIST: barbearias das quais o usuario autenticado e membro ativo
    @GetMapping
    public ResponseEntity<Page<BarbershopResponse>> findMine(
            Pageable pageable,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(barbershopService.findMine(identityId(jwt), pageable));
    }

    // ---------------------------------------------------------------
    // Com tenant (passam pelo filter: membro validado e papel no contexto)
    // ---------------------------------------------------------------

    // FIND BY ID: qualquer membro
    @GetMapping("/{barbershopId}")
    public ResponseEntity<BarbershopResponse> findById(
            @PathVariable("barbershopId") String barbershopId
    ) {
        return ResponseEntity.ok(barbershopService.findById(barbershopId));
    }

    // UPDATE: ADMIN ou OWNER (checado no service)
    @PutMapping("/{barbershopId}")
    public ResponseEntity<BarbershopResponse> update(
            @PathVariable("barbershopId") String barbershopId,
            @Valid @RequestBody BarbershopRequest request
    ) {
        return ResponseEntity.ok(barbershopService.update(barbershopId, request));
    }

    // DELETE: somente OWNER (checado no service)
    @DeleteMapping("/{barbershopId}")
    public ResponseEntity<Void> delete(
            @PathVariable("barbershopId") String barbershopId
    ) {
        barbershopService.delete(barbershopId);
        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------------------------------

    private String identityId(Jwt jwt) {
        String identityId = jwt.getClaimAsString("identityId");
        if (identityId == null || identityId.isBlank()) {
            throw new AccessDeniedException("Identity ID not found");
        }
        return identityId;
    }
}