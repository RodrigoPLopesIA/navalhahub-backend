package com.nexoralabs.navalhahub.barbershop_services.dto;


import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record BarbershopServiceRequest(

        @NotBlank
        @Size(max = 100)
        String name,

        @Size(max = 500)
        String description,

        @NotNull
        @DecimalMin("0.01")
        BigDecimal price,

        @NotNull
        @Min(1)
        @Max(1440)
        Integer durationMinutes

) {}

