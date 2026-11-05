package com.nexoralabs.navalhahub.barbershop.repository;

import com.nexoralabs.navalhahub.barbershop.entity.BarbershopMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BarbershopMemberRepository
        extends JpaRepository<BarbershopMember, String> {

    Optional<BarbershopMember>
    findByIdentityIDAndBarbershopIdAndActiveTrue(
            String identityID,
            String barbershopId
    );
    @Query("""
        SELECT DISTINCT m
        FROM BarbershopMember m
        LEFT JOIN FETCH m.roles
        WHERE m.identityID = :identityId
          AND m.barbershop.id = :barbershopId
          AND m.active = true
    """)
    Optional<BarbershopMember> findActiveMembership(
            @Param("identityId") String identityId,
            @Param("barbershopId") String barbershopId
    );

    boolean existsByIdentityIDAndBarbershop_Id(
            String identityId,
            String barbershopId
    );
}
