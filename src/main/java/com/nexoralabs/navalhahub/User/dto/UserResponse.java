package com.nexoralabs.navalhahub.User.dto;

import com.nexoralabs.navalhahub.barbershop.roles.BarbershopRole;

import java.util.Set;

public record UserResponse(
        String identityId,

        String firstName,

        String lastName,

        String email,

        String username,

        boolean enabled,

        Set<BarbershopRole> roles
) {

    public UserResponse withRoles(Set<BarbershopRole> roles) {
        return new UserResponse(identityId, firstName, lastName, email, username, enabled,  Set.copyOf(roles));
    }

}