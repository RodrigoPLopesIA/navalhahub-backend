package com.nexoralabs.navalhahub.barber_service.mapper;

import com.nexoralabs.navalhahub.barber_service.dto.BarberServiceResponse;
import com.nexoralabs.navalhahub.barber_service.entity.BarberService;

import org.springframework.stereotype.Component;

@Component
public class BarberServiceMapper {

    public BarberServiceResponse toResponse(
            BarberService barberService) {

        return new BarberServiceResponse(
                barberService.getId(),
                barberService.getBarbershopMember().getId(),
                barberService.getService().getId(),
                barberService.getService().getName(),
                barberService.getService().getPrice(),
                barberService.getService().getDurationMinutes(),
                barberService.isActive()
        );
    }
}
