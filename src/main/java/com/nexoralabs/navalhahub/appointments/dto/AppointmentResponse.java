package com.nexoralabs.navalhahub.appointments.dto;

import com.nexoralabs.navalhahub.appointments.enums.AppointmentStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.nexoralabs.navalhahub.appointments.enums.AppointmentStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AppointmentResponse(
        UUID id,
        UUID customerId,
        String customerName,
        String barberMemberId,
        UUID serviceId,
        String serviceName,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        BigDecimal price,
        Integer durationMinutes,
        AppointmentStatus status
) {}