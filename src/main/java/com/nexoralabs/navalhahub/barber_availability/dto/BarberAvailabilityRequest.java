package com.nexoralabs.navalhahub.barber_availability.dto;

import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;

public record BarberAvailabilityRequest(

        @NotNull
        DayOfWeek dayOfWeek,

        @NotNull
        LocalTime startTime,

        @NotNull
        LocalTime endTime

) {}
