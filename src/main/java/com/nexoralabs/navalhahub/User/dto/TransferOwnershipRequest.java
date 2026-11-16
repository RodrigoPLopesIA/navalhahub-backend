package com.nexoralabs.navalhahub.User.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TransferOwnershipRequest(@NotNull UUID newOwnerIdentityId) {
}
