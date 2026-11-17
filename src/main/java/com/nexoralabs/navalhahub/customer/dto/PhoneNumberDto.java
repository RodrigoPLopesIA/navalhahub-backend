package com.nexoralabs.navalhahub.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Telefone trafegado na API. Aceita máscara ("(11) 99999-8888"); o mapper normaliza para dígitos.
 */
public record PhoneNumberDto(

        @NotBlank(message = "O código do país é obrigatório")
        @Pattern(regexp = "^\\+?\\d{1,3}$", message = "Código do país inválido (ex.: 55 ou +55)")
        String countryCode,

        @NotBlank(message = "O número é obrigatório")
        @Pattern(regexp = "^(?=(?:\\D*\\d){8,15}\\D*$)[\\d\\s()\\-]+$",
                message = "Número inválido (de 8 a 15 dígitos)")
        String number
) {}