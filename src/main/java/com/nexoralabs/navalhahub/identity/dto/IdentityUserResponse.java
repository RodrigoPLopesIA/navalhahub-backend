package com.nexoralabs.navalhahub.identity.dto;

import lombok.Builder;


@Builder
public record IdentityUserResponse(
        String identityId,
        String username,
        String email,
        String firstName,
        String lastName,
        boolean enabled
) {}

