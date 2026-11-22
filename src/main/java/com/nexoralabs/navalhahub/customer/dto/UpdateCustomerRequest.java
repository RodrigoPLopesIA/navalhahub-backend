package com.nexoralabs.navalhahub.customer.dto;

import jakarta.validation.constraints.Size;

/** Altera apenas dados do vínculo, visíveis somente para a barbearia. Campos nulos são ignorados. */
public record UpdateCustomerRequest(

        @Size(max = 150)
        String name,

        @Size(max = 5000)
        String notes
) {}
