package com.nexoralabs.navalhahub.identity.dto;

import java.util.UUID;

public record IdentityUserResponse(
        String identityId,
        String username,
        String email,
        String firstName,
        String lastName,
        boolean enabled
) {}

