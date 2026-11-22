package com.nexoralabs.navalhahub.barbershop_services.service;

import com.nexoralabs.navalhahub.barbershop_services.dto.BarbershopServiceRequest;
import com.nexoralabs.navalhahub.barbershop_services.dto.BarbershopServiceResponse;
import com.nexoralabs.navalhahub.barbershop_services.entities.BarbershopServices;
import com.nexoralabs.navalhahub.barbershop_services.mapper.BarbershopServicesMapper;
import com.nexoralabs.navalhahub.barbershop_services.repository.BarbershopServicesRepository;

import com.nexoralabs.navalhahub.barbershop.entity.BarbershopMember;
import com.nexoralabs.navalhahub.barbershop_member.repository.BarbershopMemberRepository;
import com.nexoralabs.navalhahub.barbershop.roles.BarbershopRole;

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
public class BarbershopServicesService {

    private final BarbershopServicesRepository serviceRepository;
    private final BarbershopMemberRepository memberRepository;
    private final BarbershopServicesMapper serviceMapper;

    @Transactional
    public BarbershopServiceResponse create(
            String barbershopId,
            BarbershopServiceRequest request) {

        BarbershopMember requester =
                getAuthorizedMember(barbershopId, true);

        if (serviceRepository.existsByBarbershop_IdAndNameIgnoreCase(
                barbershopId, request.name())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A service with this name already exists"
            );
        }

        BarbershopServices service =
                serviceMapper.toEntity(request);

        service.setBarbershop(requester.getBarbershop());

        return serviceMapper.toResponse(
                serviceRepository.save(service)
        );
    }

    @Transactional(readOnly = true)
    public List<BarbershopServiceResponse> findAll(
            String barbershopId) {

        getAuthorizedMember(barbershopId, false);

        return serviceRepository
                .findAllByBarbershop_IdAndActiveTrueOrderByNameAsc(
                        barbershopId
                )
                .stream()
                .map(serviceMapper::toResponse)
                .toList();
    }

    @Transactional
    public BarbershopServiceResponse update(
            String barbershopId,
            UUID serviceId,
            BarbershopServiceRequest request) {

        getAuthorizedMember(barbershopId, true);

        BarbershopServices service = serviceRepository
                .findByIdAndBarbershop_Id(serviceId, barbershopId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Service not found"
                ));

        if (!service.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot update an inactive service"
            );
        }

        if (serviceRepository
                .existsByBarbershop_IdAndNameIgnoreCaseAndIdNot(
                        barbershopId,
                        request.name(),
                        serviceId
                )) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A service with this name already exists"
            );
        }

        serviceMapper.updateEntity(request, service);

        return serviceMapper.toResponse(
                serviceRepository.save(service)
        );
    }

    @Transactional
    public void deactivate(
            String barbershopId,
            UUID serviceId) {

        getAuthorizedMember(barbershopId, true);

        BarbershopServices service = serviceRepository
                .findByIdAndBarbershop_Id(serviceId, barbershopId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Service not found"
                ));

        service.setActive(false);

        serviceRepository.save(service);
    }

    private BarbershopMember getAuthorizedMember(
            String barbershopId,
            boolean adminRequired) {

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

        if (adminRequired) {

            boolean isOwnerOrAdmin =
                    member.getRoles().contains(BarbershopRole.OWNER) ||
                            member.getRoles().contains(BarbershopRole.ADMIN);

            if (!isOwnerOrAdmin) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Only owners and admins can manage services"
                );
            }
        }

        return member;
    }
}

