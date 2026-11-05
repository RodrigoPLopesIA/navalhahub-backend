
package com.nexoralabs.navalhahub.User.service;

import com.nexoralabs.navalhahub.User.dto.UserRequest;
import com.nexoralabs.navalhahub.User.dto.UserResponse;
import com.nexoralabs.navalhahub.barbershop.repository.BarbershopMemberRepository;
import com.nexoralabs.navalhahub.barbershop.repository.BarbershopRepository;
import com.nexoralabs.navalhahub.identity.mapper.IdentityUserMapper;
import com.nexoralabs.navalhahub.identity.service.IdentityService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.UUID;

import com.nexoralabs.navalhahub.barbershop.entity.Barbershop;
import com.nexoralabs.navalhahub.barbershop.entity.BarbershopMember;
import com.nexoralabs.navalhahub.barbershop.roles.BarbershopRole;
import com.nexoralabs.navalhahub.identity.dto.IdentityUserResponse;
import com.nexoralabs.navalhahub.security.context.TenantContextHolder;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class UserService {

    private final IdentityService identityService;
    private final IdentityUserMapper identityUserMapper;

    private final BarbershopRepository barbershopRepository;
    private final BarbershopMemberRepository memberRepository;

    @Transactional
    public UserResponse createUser(
            String barbershopId,
            UserRequest userRequest
    ) {

        var context = TenantContextHolder.get();

        if (context == null ||
                !context.barbershopId().equals(barbershopId)) {

            throw new AccessDeniedException(
                    "Invalid barbershop context"
            );
        }

        validateRoleAssignment(
                userRequest.role(),
                context.roles()
        );

        if (memberRepository.existsByIdentityIDAndBarbershop_Id(
                "", barbershopId)) {
            throw new IllegalStateException(
                    "User already belongs to this barbershop"
            );
        }

        // 1. Criar identidade no Keycloak.
        var identityRequest = identityUserMapper
                .toIdentityUserRequest(userRequest);

        IdentityUserResponse identityResponse =
                identityService.createUser(identityRequest);

        try {

            // 2. Buscar a barbearia.
            Barbershop barbershop = barbershopRepository
                    .findById(barbershopId)
                    .orElseThrow(() ->
                            new EntityNotFoundException(
                                    "Barbershop not found"
                            )
                    );

            // 3. Criar vínculo.
            BarbershopMember member = BarbershopMember.builder()
                    .identityID(
                            identityResponse.identityId()
                    )
                    .barbershop(barbershop)
                    .roles(new HashSet<>())
                    .active(true)
                    .build();

            member.getRoles().add(userRequest.role());

            memberRepository.saveAndFlush(member);

            // 4. Retornar usuário.
            return identityUserMapper.toUserResponse(
                    identityResponse
            );

        } catch (RuntimeException exception) {

            // Compensação: excluir identidade criada.
            try {
                identityService.deleteUser(
                        UUID.fromString(identityResponse.identityId())
                );
            } catch (RuntimeException cleanupException) {
                exception.addSuppressed(cleanupException);
            }

            throw exception;
        }
    }

    private void validateRoleAssignment(
            BarbershopRole requestedRole,
            java.util.Set<BarbershopRole> currentRoles
    ) {

        boolean isOwner = currentRoles.contains(
                BarbershopRole.OWNER
        );

        boolean isAdmin = currentRoles.contains(
                BarbershopRole.ADMIN
        );

        if (requestedRole == BarbershopRole.OWNER) {
            throw new AccessDeniedException(
                    "OWNER cannot be assigned through user creation"
            );
        }

        if (isAdmin && !isOwner &&
                requestedRole != BarbershopRole.BARBER) {
            throw new AccessDeniedException(
                    "ADMIN can only create BARBER users"
            );
        }
    }
}
