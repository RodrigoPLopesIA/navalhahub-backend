package com.nexoralabs.navalhahub.barber_service.repository;

import com.nexoralabs.navalhahub.barber_service.entity.BarberService;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BarberServiceRepository
        extends JpaRepository<BarberService, UUID> {

    List<BarberService>
    findAllByBarbershopMember_IdAndActiveTrue(
            String memberId
    );

    Optional<BarberService>
    findByService_IdAndBarbershopMember_Id(
            UUID serviceId,
            String memberId
    );

    Optional<BarberService>
    findByIdAndBarbershopMember_Id(
            UUID id,
            String memberId
    );


    Optional<BarberService> findByBarbershopMember_IdAndService_IdAndActiveTrue(String barbershopmemberId, UUID serviceId);
}
