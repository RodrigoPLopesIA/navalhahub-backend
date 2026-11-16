package com.nexoralabs.navalhahub.security.context;

import com.nexoralabs.navalhahub.barbershop.roles.BarbershopRole;

import java.util.Set;

public record TenantContext(
        String identityId,
        String barbershopId,
        Set<BarbershopRole> roles
) {
    public TenantContext {
        roles = Set.copyOf(roles);
    }
}