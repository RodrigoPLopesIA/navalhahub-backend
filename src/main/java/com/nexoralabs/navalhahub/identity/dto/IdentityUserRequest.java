package com.nexoralabs.navalhahub.identity.dto;

import lombok.Builder;

@Builder
public record IdentityUserRequest(
        String username,
        String email,
        String firstName,
        String lastName,
        String password,
        boolean enabled
) {}
