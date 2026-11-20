package com.nexoralabs.navalhahub.barbershop.repository;

import com.nexoralabs.navalhahub.barbershop.entity.BarbershopMember;
import com.nexoralabs.navalhahub.barbershop.roles.BarbershopRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    Optional<BarbershopMember> findByIdentityIDAndBarbershop_Id(
            String identityID,
            String barbershopId
    );

    Page<BarbershopMember> findAllByBarbershop_IdAndActiveTrue(
            String barbershopId,
            Pageable pageable
    );

    @Query("""
    SELECT DISTINCT m
    FROM BarbershopMember m
    JOIN m.roles r
    WHERE m.barbershop.id = :barbershopId
      AND m.active = true
      AND (
          :isPrivileged = true
          OR r NOT IN :restrictedRoles
      )
""")
    Page<BarbershopMember> findAllVisibleMembers(
            String barbershopId,
            boolean isPrivileged,
            Collection<BarbershopRole> restrictedRoles,
            Pageable pageable
    );

    @Query(
            value = """
        select m from BarbershopMember m
        join fetch m.barbershop
        where m.identityID = :identityId and m.active = true
        """,
            countQuery = """
        select count(m) from BarbershopMember m
        where m.identityID = :identityId and m.active = true
        """
    )
    Page<BarbershopMember> findActiveByIdentityId(
            @Param("identityId") String identityId,
            Pageable pageable);

    void deleteAllByBarbershop_Id(String barbershopId);

    boolean existsByBarbershop_IdAndIdentityID(
            String barbershopId,
            String identityID
    );
}
