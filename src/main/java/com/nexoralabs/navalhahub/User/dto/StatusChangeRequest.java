package com.nexoralabs.navalhahub.User.dto;


import jakarta.validation.constraints.NotNull;

public record StatusChangeRequest(@NotNull Boolean active) {
}
