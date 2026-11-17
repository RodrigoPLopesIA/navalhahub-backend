package com.nexoralabs.navalhahub.customer.repository;

import com.nexoralabs.navalhahub.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByPhoneNumberCountryCodeAndPhoneNumberNumber(String countryCode, String number);
}