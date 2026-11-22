package com.nexoralabs.navalhahub.barbershop_services.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.nexoralabs.navalhahub.barbershop_services.entities.BarbershopServices;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BarbershopServicesRepository
        extends JpaRepository<BarbershopServices, UUID> {

    List<BarbershopServices>
    findAllByBarbershop_IdAndActiveTrueOrderByNameAsc(
            String barbershopId
    );

    Optional<BarbershopServices> findByIdAndBarbershop_Id(
            UUID id,
            String barbershopId
    );

    boolean existsByBarbershop_IdAndNameIgnoreCase(
            String barbershopId,
            String name
    );

    boolean existsByBarbershop_IdAndNameIgnoreCaseAndIdNot(
            String barbershopId,
            String name,
            UUID id
    );
}

