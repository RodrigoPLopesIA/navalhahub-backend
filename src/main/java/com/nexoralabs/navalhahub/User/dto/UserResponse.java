package com.nexoralabs.navalhahub.User.dto;

import com.nexoralabs.navalhahub.barbershop.roles.BarbershopRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UserResponse(
        String identityId,

        String firstName,

        String lastName,

        String email,

        String username,

        String password,

        boolean enabled,

        BarbershopRole role
) {

}