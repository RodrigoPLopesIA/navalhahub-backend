package com.nexoralabs.navalhahub.barbershop.repository;

import com.nexoralabs.navalhahub.barbershop.entity.Barbershop;
import com.nexoralabs.navalhahub.barbershop.roles.BarbershopRole;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface BarbershopRepository
        extends JpaRepository<Barbershop, String> {

    boolean existsBySlug(String slug);

    Optional<Barbershop> findBySlug(String slug);

    @Query(
            value = """
            SELECT b
            FROM Barbershop b
            JOIN b.members m
            WHERE m.user.id = :userId
              AND :role MEMBER OF m.roles
              AND m.active = true
        """,
            countQuery = """
            SELECT COUNT(b)
            FROM Barbershop b
            JOIN b.members m
            WHERE m.user.id = :userId
              AND :role MEMBER OF m.roles
              AND m.active = true
        """
    )
    Page<Barbershop> findAllByUserIdAndRole(
            @Param("userId") UUID userId,
            @Param("role") BarbershopRole role,
            Pageable pageable
    );

    @Query("""
        SELECT b
        FROM Barbershop b
        JOIN b.members m
        WHERE b.id = :barbershopId
          AND m.user.id = :userId
          AND :role MEMBER OF m.roles
          AND m.active = true
    """)
    Optional<Barbershop> findByIdAndUserIdAndRole(
            @Param("barbershopId") String barbershopId,
            @Param("userId") UUID userId,
            @Param("role") BarbershopRole role
    );
}