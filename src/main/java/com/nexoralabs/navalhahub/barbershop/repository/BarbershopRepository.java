
package com.nexoralabs.navalhahub.barbershop.repository;

import com.nexoralabs.navalhahub.barbershop.entity.Barbershop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BarbershopRepository extends JpaRepository<Barbershop, String> {

    boolean existsBySlug(String slug);

    Optional<Barbershop> findBySlug(String slug);
}
