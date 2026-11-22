package com.nexoralabs.navalhahub.appointments.mapper;



import com.nexoralabs.navalhahub.appointments.dto.AppointmentResponse;
import com.nexoralabs.navalhahub.appointments.entity.Appointment;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {

    public AppointmentResponse toResponse(Appointment appointment) {

        return new AppointmentResponse(
                appointment.getId(),
                appointment.getCustomer().getId(),
                appointment.getCustomer().getFirstName()
                        + " " + appointment.getCustomer().getLastName(),
                appointment.getBarber().getId(),
                appointment.getService().getId(),
                appointment.getService().getName(),
                appointment.getStartAt(),
                appointment.getEndAt(),
                appointment.getPrice(),
                appointment.getDurationMinutes(),
                appointment.getStatus()
        );
    }
}

