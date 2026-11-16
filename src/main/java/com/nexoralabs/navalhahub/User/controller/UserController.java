package com.nexoralabs.navalhahub.User.controller;

import com.nexoralabs.navalhahub.User.dto.RoleChangeRequest;
import com.nexoralabs.navalhahub.User.dto.StatusChangeRequest;
import com.nexoralabs.navalhahub.User.dto.TransferOwnershipRequest;
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

    // ---------------------------------------------------------------
    // Gerenciamento de membros (ADMIN / OWNER)
    // ---------------------------------------------------------------

    // CREATE (ADMIN cria BARBER; OWNER cria BARBER e ADMIN)
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

    // FIND ALL (BARBER nao ve ADMIN/OWNER)
    @GetMapping
    public ResponseEntity<Page<UserResponse>> findAll(
            @PathVariable("barbershopId") String barbershopId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(userService.findAllUsers(barbershopId, pageable));
    }

    // FIND BY ID
    @GetMapping("/{identityId}")
    public ResponseEntity<UserResponse> findById(
            @PathVariable("barbershopId") String barbershopId,
            @PathVariable("identityId") UUID identityId
    ) {
        return ResponseEntity.ok(userService.findUser(barbershopId, identityId));
    }

    // CHANGE ROLE (BARBER <-> ADMIN). OWNER so via transferencia.
    @PatchMapping("/{identityId}/role")
    @PreAuthorize("@tenantAuthorization.canManageUsers()")
    public ResponseEntity<UserResponse> changeRole(
            @PathVariable("barbershopId") String barbershopId,
            @PathVariable("identityId") UUID identityId,
            @Valid @RequestBody RoleChangeRequest request
    ) {
        return ResponseEntity.ok(
                userService.changeRole(barbershopId, identityId, request.role()));
    }

    // ACTIVATE / DEACTIVATE (suspende o acesso sem apagar o vinculo)
    @PatchMapping("/{identityId}/status")
    @PreAuthorize("@tenantAuthorization.canManageUsers()")
    public ResponseEntity<UserResponse> changeStatus(
            @PathVariable("barbershopId") String barbershopId,
            @PathVariable("identityId") UUID identityId,
            @Valid @RequestBody StatusChangeRequest request
    ) {
        return ResponseEntity.ok(
                userService.setActive(barbershopId, identityId, request.active()));
    }

    // REMOVE MEMBER (ADMIN remove BARBER; OWNER remove BARBER e ADMIN)
    @DeleteMapping("/{identityId}")
    @PreAuthorize("@tenantAuthorization.canManageUsers()")
    public ResponseEntity<Void> delete(
            @PathVariable("barbershopId") String barbershopId,
            @PathVariable("identityId") UUID identityId
    ) {
        userService.deleteUser(barbershopId, identityId);
        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------------------------------
    // Perfil proprio (qualquer membro ativo)
    // ---------------------------------------------------------------

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(
            @PathVariable("barbershopId") String barbershopId
    ) {
        return ResponseEntity.ok(userService.findMe(barbershopId));
    }

    // Edita so os dados da identidade; o papel NUNCA e alterado aqui
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMe(
            @PathVariable("barbershopId") String barbershopId,
            @Valid @RequestBody UserRequest request
    ) {
        return ResponseEntity.ok(userService.updateMe(barbershopId, request));
    }

    // Sair da barbearia (OWNER precisa transferir a propriedade antes)
    @DeleteMapping("/me")
    public ResponseEntity<Void> leave(
            @PathVariable("barbershopId") String barbershopId
    ) {
        userService.leave(barbershopId);
        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------------------------------
    // Propriedade (somente OWNER; a checagem fica no service)
    // ---------------------------------------------------------------

    @PostMapping("/transfer-ownership")
    public ResponseEntity<Void> transferOwnership(
            @PathVariable("barbershopId") String barbershopId,
            @Valid @RequestBody TransferOwnershipRequest request
    ) {
        userService.transferOwnership(barbershopId, request);
        return ResponseEntity.noContent().build();
    }
}