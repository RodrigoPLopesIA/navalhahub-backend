package com.nexoralabs.navalhahub.appointments.dto;

import com.nexoralabs.navalhahub.appointments.enums.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

public record AppointmentStatusRequest(
        @NotNull AppointmentStatus status
) {}
