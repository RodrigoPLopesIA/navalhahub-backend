package com.nexoralabs.navalhahub.barber_availability.repository;

import com.nexoralabs.navalhahub.barber_availability.entity.BarberAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BarberAvailabilityRepository
        extends JpaRepository<BarberAvailability, UUID> {

    List<BarberAvailability>
    findAllByBarbershopMember_IdOrderByDayOfWeekAscStartTimeAsc(
            String memberId
    );

    Optional<BarberAvailability>
    findByIdAndBarbershopMember_Id(
            UUID id,
            String memberId
    );

    boolean existsByBarbershopMember_IdAndDayOfWeekAndStartTimeLessThanAndEndTimeGreaterThan(
            String memberId,
            java.time.DayOfWeek dayOfWeek,
            java.time.LocalTime endTime,
            java.time.LocalTime startTime
    );


}

