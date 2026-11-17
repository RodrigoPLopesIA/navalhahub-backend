package com.nexoralabs.navalhahub.customer.repository;

import com.nexoralabs.navalhahub.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, String> {
}
