
package com.nexoralabs.navalhahub.appointments.service;

import com.nexoralabs.navalhahub.appointments.dto.AppointmentRequest;
import com.nexoralabs.navalhahub.appointments.dto.AppointmentResponse;
import com.nexoralabs.navalhahub.appointments.entity.Appointment;
import com.nexoralabs.navalhahub.appointments.enums.AppointmentStatus;
import com.nexoralabs.navalhahub.appointments.mapper.AppointmentMapper;
import com.nexoralabs.navalhahub.appointments.repository.AppointmentRepository;
import com.nexoralabs.navalhahub.barber_availability.entity.BarberAvailability;
import com.nexoralabs.navalhahub.barber_availability.repository.BarberAvailabilityRepository;
import com.nexoralabs.navalhahub.barber_service.entity.BarberService;
import com.nexoralabs.navalhahub.barber_service.repository.BarberServiceRepository;
import com.nexoralabs.navalhahub.barbershop.entity.BarbershopMember;
import com.nexoralabs.navalhahub.barbershop.roles.BarbershopRole;
import com.nexoralabs.navalhahub.barbershop_customer.repository.BarbershopCustomerRepository;
import com.nexoralabs.navalhahub.barbershop_member.repository.BarbershopMemberRepository;
import com.nexoralabs.navalhahub.barbershop_services.entities.BarbershopServices;
import com.nexoralabs.navalhahub.barbershop_services.repository.BarbershopServicesRepository;
import com.nexoralabs.navalhahub.customer.entity.Customer;
import com.nexoralabs.navalhahub.customer.repository.CustomerRepository;
import com.nexoralabs.navalhahub.security.context.TenantContextHolder;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final BarbershopMemberRepository memberRepository;
    private final CustomerRepository customerRepository;
    private final BarbershopCustomerRepository barbershopCustomerRepository;
    private final BarbershopServicesRepository servicesRepository;
    private final BarberServiceRepository barberServiceRepository;
    private final BarberAvailabilityRepository availabilityRepository;
    private final AppointmentMapper appointmentMapper;

    private static final List<AppointmentStatus> BUSY_STATUSES = List.of(
            AppointmentStatus.PENDING,
            AppointmentStatus.CONFIRMED
    );

    @Transactional
    public AppointmentResponse create(
            String barbershopId,
            AppointmentRequest request
    ) {

        BarbershopMember requester = getRequester(barbershopId);

        BarbershopMember barber = memberRepository
                .findByIdentityIDAndBarbershop_Id(
                        request.barberMemberId(),
                        barbershopId
                )
                .orElseThrow(() ->
                        new EntityNotFoundException("Barber not found")
                );

        validateRequesterCanBookFor(requester, barber);

        if (!barber.isActive()
                || !barber.getRoles().contains(BarbershopRole.BARBER)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid or inactive barber"
            );
        }

        Customer customer = customerRepository
                .findByIdAndActiveTrue(request.customerId())
                .orElseThrow(() ->
                        new EntityNotFoundException("Customer not found")
                );

        barbershopCustomerRepository
                .findByBarbershop_IdAndCustomer_IdAndBlockedFalse(
                        barbershopId,
                        customer.getId()
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Customer is not registered in this barbershop"
                        )
                );

        BarbershopServices service = servicesRepository
                .findByIdAndBarbershop_IdAndActiveTrue(
                        request.serviceId(),
                        barbershopId
                )
                .orElseThrow(() ->
                        new EntityNotFoundException("Service not found")
                );

        BarberService barberService = barberServiceRepository
                .findByBarbershopMember_IdAndService_IdAndActiveTrue(
                        barber.getId(),
                        service.getId()
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "This barber does not provide the selected service"
                        )
                );

        if (!barberService.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Barber service is inactive"
            );
        }

        OffsetDateTime startAt = request.startAt();

        if (!startAt.isAfter(OffsetDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Appointment must be scheduled in the future"
            );
        }

        OffsetDateTime endAt = startAt.plusMinutes(
                service.getDurationMinutes()
        );

        validateAvailability(barber, startAt, endAt);

        validateNoConflict(
                barbershopId,
                barber.getId(),
                startAt,
                endAt
        );

        Appointment appointment = new Appointment();

        appointment.setBarbershop(barber.getBarbershop());
        appointment.setCustomer(customer);
        appointment.setBarber(barber);
        appointment.setService(service);
        appointment.setStartAt(startAt);
        appointment.setEndAt(endAt);
        appointment.setPrice(service.getPrice());
        appointment.setDurationMinutes(service.getDurationMinutes());
        appointment.setStatus(AppointmentStatus.PENDING);

        Appointment savedAppointment = appointmentRepository.save(appointment);

        return appointmentMapper.toResponse(savedAppointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> findAll(String barbershopId) {

        BarbershopMember requester = getRequester(barbershopId);

        List<Appointment> appointments;

        if (isOwnerOrAdmin(requester)) {
            appointments = appointmentRepository
                    .findAllByBarbershop_IdOrderByStartAtAsc(barbershopId);

        } else if (requester.getRoles().contains(BarbershopRole.BARBER)) {
            appointments = appointmentRepository
                    .findAllByBarbershop_IdAndBarber_IdOrderByStartAtAsc(
                            barbershopId,
                            requester.getId()
                    );

        } else {
            throw new AccessDeniedException(
                    "You are not allowed to view appointments"
            );
        }

        return appointments.stream()
                .map(appointmentMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AppointmentResponse findById(
            String barbershopId,
            UUID appointmentId
    ) {

        BarbershopMember requester = getRequester(barbershopId);

        Appointment appointment = getAppointment(
                barbershopId,
                appointmentId
        );

        validateAppointmentAccess(requester, appointment);

        return appointmentMapper.toResponse(appointment);
    }

    @Transactional
    public void cancel(
            String barbershopId,
            UUID appointmentId
    ) {

        BarbershopMember requester = getRequester(barbershopId);

        Appointment appointment = getAppointment(
                barbershopId,
                appointmentId
        );

        validateAppointmentAccess(requester, appointment);

        if (appointment.getStatus() == AppointmentStatus.CANCELLED
                || appointment.getStatus() == AppointmentStatus.COMPLETED
                || appointment.getStatus() == AppointmentStatus.NO_SHOW) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Appointment cannot be cancelled"
            );
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
    }

    @Transactional
    public AppointmentResponse updateStatus(
            String barbershopId,
            UUID appointmentId,
            AppointmentStatus newStatus
    ) {

        BarbershopMember requester = getRequester(barbershopId);

        Appointment appointment = getAppointment(
                barbershopId,
                appointmentId
        );

        validateAppointmentAccess(requester, appointment);

        if (newStatus == AppointmentStatus.PENDING
                || newStatus == AppointmentStatus.CANCELLED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Use the appropriate operation for this status"
            );
        }

        validateStatusTransition(appointment.getStatus(), newStatus);

        appointment.setStatus(newStatus);

        return appointmentMapper.toResponse(appointment);
    }

    private void validateAvailability(
            BarbershopMember barber,
            OffsetDateTime startAt,
            OffsetDateTime endAt
    ) {

        if (!startAt.toLocalDate().equals(endAt.toLocalDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Appointments cannot cross midnight"
            );
        }

        DayOfWeek dayOfWeek = startAt.getDayOfWeek();

        LocalTime startTime = startAt.toLocalTime();
        LocalTime endTime = endAt.toLocalTime();

        List<BarberAvailability> availabilities =
                availabilityRepository
                        .findAllByBarbershopMember_IdAndDayOfWeekAndActiveTrue(
                                barber.getId(),
                                dayOfWeek
                        );

        boolean available = availabilities.stream().anyMatch(
                availability ->
                        !startTime.isBefore(availability.getStartTime())
                                && !endTime.isAfter(availability.getEndTime())
        );

        if (!available) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Barber is not available at the selected time"
            );
        }
    }

    private void validateNoConflict(
            String barbershopId,
            String barberId,
            OffsetDateTime startAt,
            OffsetDateTime endAt
    ) {

        boolean hasConflict = appointmentRepository
                .existsByBarbershop_IdAndBarber_IdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
                        barbershopId,
                        barberId,
                        BUSY_STATUSES,
                        endAt,
                        startAt
                );

        if (hasConflict) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Barber already has an appointment at this time"
            );
        }
    }

    private BarbershopMember getRequester(String barbershopId) {

        var context = TenantContextHolder
                .verifyBarberShopIdContext(barbershopId);

        return memberRepository
                .findByBarbershop_IdAndIdentityID(
                        barbershopId,
                        context.identityId()
                )
                .filter(BarbershopMember::isActive)
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "Active barbershop membership required"
                        )
                );
    }

    private void validateRequesterCanBookFor(
            BarbershopMember requester,
            BarbershopMember targetBarber
    ) {

        if (isOwnerOrAdmin(requester)) {
            return;
        }

        if (requester.getRoles().contains(BarbershopRole.BARBER)
                && requester.getId().equals(targetBarber.getId())) {
            return;
        }

        throw new AccessDeniedException(
                "You cannot create appointments for this barber"
        );
    }

    private boolean isOwnerOrAdmin(BarbershopMember member) {
        return member.getRoles().contains(BarbershopRole.OWNER)
                || member.getRoles().contains(BarbershopRole.ADMIN);
    }

    private void validateAppointmentAccess(
            BarbershopMember requester,
            Appointment appointment
    ) {

        if (isOwnerOrAdmin(requester)) {
            return;
        }

        if (requester.getRoles().contains(BarbershopRole.BARBER)
                && requester.getId().equals(appointment.getBarber().getId())) {
            return;
        }

        throw new AccessDeniedException(
                "You cannot access this appointment"
        );
    }

    private void validateStatusTransition(
            AppointmentStatus current,
            AppointmentStatus next
    ) {

        boolean valid = switch (current) {
            case PENDING ->
                    next == AppointmentStatus.CONFIRMED;
            case CONFIRMED ->
                    next == AppointmentStatus.COMPLETED
                            || next == AppointmentStatus.NO_SHOW;
            default -> false;
        };

        if (!valid) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid appointment status transition"
            );
        }
    }

    private Appointment getAppointment(
            String barbershopId,
            UUID appointmentId
    ) {

        return appointmentRepository
                .findByIdAndBarbershop_Id(
                        appointmentId,
                        barbershopId
                )
                .orElseThrow(() ->
                        new EntityNotFoundException("Appointment not found")
                );
    }
}
