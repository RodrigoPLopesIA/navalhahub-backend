package com.nexoralabs.navalhahub.barber_availability.service;

import com.nexoralabs.navalhahub.barber_availability.dto.BarberAvailabilityRequest;
import com.nexoralabs.navalhahub.barber_availability.dto.BarberAvailabilityResponse;
import com.nexoralabs.navalhahub.barber_availability.entity.BarberAvailability;
import com.nexoralabs.navalhahub.barber_availability.mapper.BarberAvailabilityMapper;
import com.nexoralabs.navalhahub.barber_availability.repository.BarberAvailabilityRepository;
import com.nexoralabs.navalhahub.barbershop.entity.BarbershopMember;
import com.nexoralabs.navalhahub.barbershop_member.repository.BarbershopMemberRepository;
import com.nexoralabs.navalhahub.barbershop.roles.BarbershopRole;
import com.nexoralabs.navalhahub.security.context.TenantContextHolder;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BarberAvailabilityService {

    private final BarberAvailabilityRepository availabilityRepository;
    private final BarbershopMemberRepository memberRepository;
    private final BarberAvailabilityMapper availabilityMapper;

    @Transactional
    public BarberAvailabilityResponse create(
            String barbershopId,
            BarberAvailabilityRequest request) {

        BarbershopMember member = getAuthorizedBarber(barbershopId);

        validateTimeRange(request.startTime(), request.endTime());

        validateOverlap(
                member.getId(),
                request.dayOfWeek(),
                request.startTime(),
                request.endTime(),
                null
        );

        BarberAvailability availability = availabilityMapper.toEntity(request);
        availability.setBarbershopMember(member);
        return availabilityMapper.toResponse(availabilityRepository.save(availability));
    }

    @Transactional(readOnly = true)
    public List<BarberAvailabilityResponse> findAll(
            String barbershopId) {

        BarbershopMember member = getAuthorizedBarber(barbershopId);

        return availabilityRepository
                .findAllByBarbershopMember_IdOrderByDayOfWeekAscStartTimeAsc(
                        member.getId()
                )
                .stream()
                .map(availabilityMapper::toResponse)
                .toList();
    }

    @Transactional
    public BarberAvailabilityResponse update(
            String barbershopId,
            UUID availabilityId,
            BarberAvailabilityRequest request) {

        BarbershopMember member = getAuthorizedBarber(barbershopId);

        BarberAvailability availability = availabilityRepository
                .findByIdAndBarbershopMember_Id(
                        availabilityId,
                        member.getId()
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Barber availability not found"
                ));

        validateTimeRange(request.startTime(), request.endTime());

        validateOverlap(
                member.getId(),
                request.dayOfWeek(),
                request.startTime(),
                request.endTime(),
                availabilityId
        );

        availability.setDayOfWeek(request.dayOfWeek());
        availability.setStartTime(request.startTime());
        availability.setEndTime(request.endTime());

        return availabilityMapper.toResponse(availabilityRepository.save(availability));
    }

    @Transactional
    public void delete(
            String barbershopId,
            UUID availabilityId) {

        BarbershopMember member = getAuthorizedBarber(barbershopId);

        BarberAvailability availability = availabilityRepository
                .findByIdAndBarbershopMember_Id(
                        availabilityId,
                        member.getId()
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Barber availability not found"
                ));

        availabilityRepository.delete(availability);
    }

    private BarbershopMember getAuthorizedBarber(String barbershopId) {

        String identityId = TenantContextHolder.get().identityId();

        BarbershopMember member = memberRepository
                .findByBarbershop_IdAndIdentityID(
                        barbershopId,
                        identityId
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You are not a member of this barbershop"
                ));

        if (!member.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Your membership is inactive"
            );
        }

        if (!member.getRoles().contains(BarbershopRole.BARBER)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only barbers can manage availability"
            );
        }

        return member;
    }

    private void validateTimeRange(
            LocalTime startTime,
            LocalTime endTime) {

        if (!startTime.isBefore(endTime)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Start time must be before end time"
            );
        }
    }

    private void validateOverlap(
            String memberId,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            UUID excludedId) {

        boolean hasOverlap = availabilityRepository
                .findAllByBarbershopMember_IdOrderByDayOfWeekAscStartTimeAsc(
                        memberId
                )
                .stream()
                .filter(item -> !item.getId().equals(excludedId))
                .filter(item -> item.getDayOfWeek() == dayOfWeek)
                .anyMatch(item ->
                        startTime.isBefore(item.getEndTime()) &&
                                endTime.isAfter(item.getStartTime())
                );

        if (hasOverlap) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Availability overlaps with an existing schedule"
            );
        }
    }
}
