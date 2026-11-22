package com.nexoralabs.navalhahub.customer.dto;

import com.nexoralabs.navalhahub.customer.dto.PhoneNumberDto;

import java.time.Instant;
import java.util.UUID;

/**
 * Visão do cliente PARA UMA BARBEARIA. Não expõe e-mail nem sobrenome do cadastro global,
 * porque esse cadastro é compartilhado entre barbearias e pode ter sido criado por outra.
 */
public record CustomerResponse(
        UUID id,
        String name,
        PhoneNumberDto phoneNumber,
        String notes,
        boolean blocked,
        Instant consentAt,
        Instant createdAt
) {}