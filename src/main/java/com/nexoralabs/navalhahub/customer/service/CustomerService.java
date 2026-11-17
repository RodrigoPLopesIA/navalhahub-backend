package com.nexoralabs.navalhahub.customer.service;

import com.nexoralabs.navalhahub.barbershop.repository.BarbershopCustomerRepository;
import com.nexoralabs.navalhahub.barbershop.repository.BarbershopMemberRepository;
import com.nexoralabs.navalhahub.barbershop.repository.BarbershopRepository;
import com.nexoralabs.navalhahub.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final BarbershopCustomerRepository barbershopCustomerRepository;
    private final BarbershopRepository barbershopRepository;
    private final BarbershopMemberRepository barbershopMemberRepository;




}
