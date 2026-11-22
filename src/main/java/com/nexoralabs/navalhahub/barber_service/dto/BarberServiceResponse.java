package com.nexoralabs.navalhahub.barber_service.dto;
import java.math.BigDecimal;
import java.util.UUID;

public record BarberServiceResponse(
        UUID id,
        String barbershopMemberId,
        UUID serviceId,
        String serviceName,
        BigDecimal price,
        Integer durationMinutes,
        boolean active
) {}

