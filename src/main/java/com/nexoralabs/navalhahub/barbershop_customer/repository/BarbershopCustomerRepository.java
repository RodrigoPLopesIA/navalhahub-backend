package com.nexoralabs.navalhahub.barbershop_customer.repository;

import com.nexoralabs.navalhahub.barbershop.entity.BarbershopCustomer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BarbershopCustomerRepository extends JpaRepository<BarbershopCustomer, String> {

    @EntityGraph(attributePaths = "customer")
    Optional<BarbershopCustomer> findByBarbershopIdAndCustomerId(UUID barbershopId, UUID customerId);

    boolean existsByBarbershopIdAndCustomerId(UUID barbershopId, UUID customerId);

    @EntityGraph(attributePaths = "customer")
    Page<BarbershopCustomer> findByBarbershopId(UUID barbershopId, Pageable pageable);

    @EntityGraph(attributePaths = "customer")
    Page<BarbershopCustomer> findByBarbershopIdAndBlocked(UUID barbershopId, boolean blocked, Pageable pageable);

    // :term já chega minúsculo e com %, _ e \ escapados pelo service.
    @EntityGraph(attributePaths = "customer")
    @Query("""
            select bc from BarbershopCustomer bc
            where bc.barbershop.id = :barbershopId
              and (
                    lower(coalesce(bc.name, ''))               like concat('%', :term, '%') escape '\\'
                 or lower(bc.customer.firstName)               like concat('%', :term, '%') escape '\\'
                 or lower(coalesce(bc.customer.lastName, ''))  like concat('%', :term, '%') escape '\\'
              )
            """)
    Page<BarbershopCustomer> search(@Param("barbershopId") UUID barbershopId,
                                    @Param("term") String term,
                                    Pageable pageable);

    @EntityGraph(attributePaths = "customer")
    @Query("""
            select bc from BarbershopCustomer bc
            where bc.barbershop.id = :barbershopId
              and bc.blocked = :blocked
              and (
                    lower(coalesce(bc.name, ''))               like concat('%', :term, '%') escape '\\'
                 or lower(bc.customer.firstName)               like concat('%', :term, '%') escape '\\'
                 or lower(coalesce(bc.customer.lastName, ''))  like concat('%', :term, '%') escape '\\'
              )
            """)
    Page<BarbershopCustomer> searchByBlocked(@Param("barbershopId") UUID barbershopId,
                                             @Param("term") String term,
                                             @Param("blocked") boolean blocked,
                                             Pageable pageable);
    Optional<BarbershopCustomer>
    findByBarbershop_IdAndCustomer_IdAndActiveTrue(
            String barbershopId,
            UUID customerId
    );
}