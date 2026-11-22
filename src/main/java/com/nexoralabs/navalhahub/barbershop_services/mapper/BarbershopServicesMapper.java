package com.nexoralabs.navalhahub.barbershop_services.mapper;


import com.nexoralabs.navalhahub.barbershop_services.dto.BarbershopServiceRequest;
import com.nexoralabs.navalhahub.barbershop_services.dto.BarbershopServiceResponse;
import com.nexoralabs.navalhahub.barbershop_services.entities.BarbershopServices;

import org.springframework.stereotype.Component;

@Component
public class BarbershopServicesMapper {

    public BarbershopServices toEntity(
            BarbershopServiceRequest request) {

        BarbershopServices service = new BarbershopServices();

        service.setName(request.name());
        service.setDescription(request.description());
        service.setPrice(request.price());
        service.setDurationMinutes(request.durationMinutes());
        service.setActive(true);

        return service;
    }

    public BarbershopServiceResponse toResponse(
            BarbershopServices service) {

        return new BarbershopServiceResponse(
                service.getId(),
                service.getName(),
                service.getDescription(),
                service.getPrice(),
                service.getDurationMinutes(),
                service.isActive()
        );
    }

    public void updateEntity(
            BarbershopServiceRequest request,
            BarbershopServices service) {

        service.setName(request.name());
        service.setDescription(request.description());
        service.setPrice(request.price());
        service.setDurationMinutes(request.durationMinutes());
    }
}
