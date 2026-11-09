package com.nexoralabs.navalhahub.User.dto;

import com.nexoralabs.navalhahub.barbershop.roles.BarbershopRole;

public record UserResponse(
        String identityId,

        String firstName,

        String lastName,

        String email,

        String username,

        boolean enabled,

        BarbershopRole role
) {

}