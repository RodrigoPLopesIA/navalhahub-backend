package com.nexoralabs.navalhahub.barber_availability.mapper;

import com.nexoralabs.navalhahub.barber_availability.dto.BarberAvailabilityRequest;
import com.nexoralabs.navalhahub.barber_availability.dto.BarberAvailabilityResponse;
import com.nexoralabs.navalhahub.barber_availability.entity.BarberAvailability;

import org.springframework.stereotype.Component;

@Component
public class BarberAvailabilityMapper {

    public BarberAvailability toEntity(
            BarberAvailabilityRequest request) {

        BarberAvailability availability = new BarberAvailability();

        availability.setDayOfWeek(request.dayOfWeek());
        availability.setStartTime(request.startTime());
        availability.setEndTime(request.endTime());
        availability.setActive(true);

        return availability;
    }

    public BarberAvailabilityResponse toResponse(
            BarberAvailability availability) {

        return new BarberAvailabilityResponse(
                availability.getId(),
                availability.getDayOfWeek(),
                availability.getStartTime(),
                availability.getEndTime(),
                availability.isActive()
        );
    }

    public void updateEntity(
            BarberAvailabilityRequest request,
            BarberAvailability availability) {

        availability.setDayOfWeek(request.dayOfWeek());
        availability.setStartTime(request.startTime());
        availability.setEndTime(request.endTime());
    }
}

