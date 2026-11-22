package com.nexoralabs.navalhahub.appointments.dto;

import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AppointmentRequest(

        @NotNull
        UUID customerId,

        @NotNull
        String barberMemberId,

        @NotNull
        UUID serviceId,

        @NotNull
        OffsetDateTime startAt

) {}
