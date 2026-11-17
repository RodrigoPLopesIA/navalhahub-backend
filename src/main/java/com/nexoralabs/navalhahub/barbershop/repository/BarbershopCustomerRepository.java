package com.nexoralabs.navalhahub.barbershop.repository;

import com.nexoralabs.navalhahub.barbershop.entity.BarbershopCustomer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BarbershopCustomerRepository extends JpaRepository<BarbershopCustomer, String> {
}
