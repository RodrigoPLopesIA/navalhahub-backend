package com.nexoralabs.navalhahub.barbershop_services.dto;


import java.math.BigDecimal;
import java.util.UUID;

public record BarbershopServiceResponse(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        Integer durationMinutes,
        boolean active
) {}

