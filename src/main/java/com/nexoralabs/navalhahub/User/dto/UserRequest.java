package com.nexoralabs.navalhahub.User.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.nexoralabs.navalhahub.barbershop.roles.BarbershopRole;

public record UserRequest(

        @NotBlank
        @Size(max = 120)
        String firstName,

        @NotBlank
        @Size(max = 120)
        String lastName,

        @NotBlank
        @Email
        String email,

        @NotBlank
        String username,

        @NotBlank
        @Size(min = 8)
        String password,

        @NotNull
        boolean enabled,

        @NotNull
        BarbershopRole role

) {}
