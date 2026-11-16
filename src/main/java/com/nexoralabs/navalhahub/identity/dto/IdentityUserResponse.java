package com.nexoralabs.navalhahub.identity.dto;

import com.nexoralabs.navalhahub.barbershop.roles.BarbershopRole;
import lombok.Builder;

import java.util.Set;


@Builder
public record IdentityUserResponse(
        String identityId,
        String username,
        String email,
        String firstName,
        String lastName,
        boolean enabled,
        Set<BarbershopRole> roles

) {}

