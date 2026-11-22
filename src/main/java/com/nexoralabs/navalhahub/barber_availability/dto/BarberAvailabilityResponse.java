package com.nexoralabs.navalhahub.barber_availability.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

public record BarberAvailabilityResponse(
        UUID id,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        boolean active
) {}

