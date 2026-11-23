package com.nexoralabs.navalhahub.appointments.repository;

import com.nexoralabs.navalhahub.appointments.entity.Appointment;
import com.nexoralabs.navalhahub.appointments.enums.AppointmentStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository
        extends JpaRepository<Appointment, UUID> {

    List<Appointment> findAllByBarbershop_IdOrderByStartAtAsc(
            String barbershopId
    );

    List<Appointment> findAllByBarbershop_IdAndBarber_IdOrderByStartAtAsc(
            String barbershopId,
            String barberId
    );

    Optional<Appointment> findByIdAndBarbershop_Id(
            UUID id,
            String barbershopId
    );

    boolean existsByBarbershop_IdAndBarber_IdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
            String barbershopId,
            String barberId,
            List<AppointmentStatus> statuses,
            OffsetDateTime endAt,
            OffsetDateTime startAt
    );
}