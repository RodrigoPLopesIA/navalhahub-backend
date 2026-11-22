package com.nexoralabs.navalhahub.barber_service.service;

import com.nexoralabs.navalhahub.barber_service.dto.BarberServiceResponse;
import com.nexoralabs.navalhahub.barber_service.entity.BarberService;
import com.nexoralabs.navalhahub.barber_service.mapper.BarberServiceMapper;
import com.nexoralabs.navalhahub.barber_service.repository.BarberServiceRepository;

import com.nexoralabs.navalhahub.barbershop.entity.BarbershopMember;
import com.nexoralabs.navalhahub.barbershop_member.repository.BarbershopMemberRepository;
import com.nexoralabs.navalhahub.barbershop.roles.BarbershopRole;

import com.nexoralabs.navalhahub.barbershop_services.entities.BarbershopServices;
import com.nexoralabs.navalhahub.barbershop_services.repository.BarbershopServicesRepository;

import com.nexoralabs.navalhahub.security.context.TenantContextHolder;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BarberServiceService {

    private final BarberServiceRepository barberServiceRepository;
    private final BarbershopMemberRepository memberRepository;
    private final BarbershopServicesRepository serviceRepository;
    private final BarberServiceMapper barberServiceMapper;

    @Transactional
    public BarberServiceResponse assign(
            String barbershopId,
            UUID serviceId,
            String targetMemberId) {

        BarbershopMember requester =
                getAuthorizedAdmin(barbershopId);

        BarbershopMember barber = memberRepository
                .findByIdentityIDAndBarbershop_Id(
                        targetMemberId,
                        barbershopId
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Barber not found"
                ));

        if (!barber.isActive() ||
                !barber.getRoles().contains(BarbershopRole.BARBER)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Target member is not an active barber"
            );
        }

        BarbershopServices service = serviceRepository
                .findByIdAndBarbershop_Id(
                        serviceId,
                        barbershopId
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Service not found"
                ));

        if (!service.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot assign an inactive service"
            );
        }

        var existing = barberServiceRepository
                .findByService_IdAndBarbershopMember_Id(
                        serviceId,
                        barber.getId()
                );

        if (existing.isPresent()) {

            BarberService association = existing.get();

            if (association.isActive()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "This service is already assigned to the barber"
                );
            }

            association.setActive(true);

            return barberServiceMapper.toResponse(
                    barberServiceRepository.save(association)
            );
        }

        BarberService association = new BarberService();

        association.setBarbershopMember(barber);
        association.setService(service);
        association.setActive(true);

        return barberServiceMapper.toResponse(
                barberServiceRepository.save(association)
        );
    }

    @Transactional(readOnly = true)
    public List<BarberServiceResponse> findAllByBarber(
            String barbershopId,
            String targetMemberId) {

        BarbershopMember requester =
                getAuthorizedMember(barbershopId);

        BarbershopMember barber = memberRepository
                .findByIdentityIDAndBarbershop_Id(
                        targetMemberId,
                        barbershopId
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Barber not found"
                ));

        boolean isOwnerOrAdmin =
                requester.getRoles().contains(BarbershopRole.OWNER) ||
                        requester.getRoles().contains(BarbershopRole.ADMIN);

        boolean isSelf =
                requester.getId().equals(barber.getId());

        if (!isOwnerOrAdmin && !isSelf) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only view your own assigned services"
            );
        }

        if (!barber.isActive() ||
                !barber.getRoles().contains(BarbershopRole.BARBER)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Target member is not an active barber"
            );
        }

        return barberServiceRepository
                .findAllByBarbershopMember_IdAndActiveTrue(
                        barber.getId()
                )
                .stream()
                .filter(item -> item.getService().isActive())
                .map(barberServiceMapper::toResponse)
                .toList();
    }

    @Transactional
    public void unassign(
            String barbershopId,
            UUID serviceId,
            String targetMemberId) {

        getAuthorizedAdmin(barbershopId);

        BarberService association = barberServiceRepository
                .findByService_IdAndBarbershopMember_Id(
                        serviceId,
                        targetMemberId
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Barber service association not found"
                ));

        if (!association.getService()
                .getBarbershop().getId().equals(barbershopId)) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Barber service association not found"
            );
        }

        association.setActive(false);

        barberServiceRepository.save(association);
    }

    private BarbershopMember getAuthorizedAdmin(
            String barbershopId) {

        BarbershopMember member =
                getAuthorizedMember(barbershopId);

        boolean isOwnerOrAdmin =
                member.getRoles().contains(BarbershopRole.OWNER) ||
                        member.getRoles().contains(BarbershopRole.ADMIN);

        if (!isOwnerOrAdmin) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only owners and admins can manage barber services"
            );
        }

        return member;
    }

    private BarbershopMember getAuthorizedMember(
            String barbershopId) {

        String identityId =
                TenantContextHolder.get().identityId();

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

        return member;
    }
}

