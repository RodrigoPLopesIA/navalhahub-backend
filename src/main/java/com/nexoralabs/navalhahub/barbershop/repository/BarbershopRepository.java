package com.nexoralabs.navalhahub.barbershop.repository;

import com.nexoralabs.navalhahub.barbershop.entity.Barbershop;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BarbershopRepository extends JpaRepository<Barbershop, String> {
}
