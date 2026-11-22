package com.nexoralabs.navalhahub.customer.dto.request;

import com.nexoralabs.navalhahub.customer.dto.PhoneNumberDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCustomerRequest(

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100)
        String firstName,

        @Size(max = 100)
        String lastName,

        @Email(message = "E-mail inválido")
        @Size(max = 255)
        String email,

        @NotNull(message = "O telefone é obrigatório")
        @Valid
        PhoneNumberDto phoneNumber,

        /** Como a barbearia chama o cliente. Padrão: firstName + lastName. */
        @Size(max = 150)
        String name,

        @Size(max = 5000)
        String notes,

        /** O cliente consentiu com o tratamento dos dados (LGPD)? Grava consentAt quando true. */
        boolean consent
) {}