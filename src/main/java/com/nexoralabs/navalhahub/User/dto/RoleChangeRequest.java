package com.nexoralabs.navalhahub.User.dto;


import com.nexoralabs.navalhahub.barbershop.roles.BarbershopRole;
import jakarta.validation.constraints.NotNull;

public record RoleChangeRequest(@NotNull BarbershopRole role) {
}
