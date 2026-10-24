package com.nexoralabs.navalhahub.barbershop.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {

    @NotBlank(message = "A rua é obrigatória")
    @Size(max = 150)
    private String street;

    @NotBlank(message = "O número é obrigatório")
    @Size(max = 20)
    private String number;

    @Size(max = 100)
    private String complement;

    @NotBlank(message = "O bairro é obrigatório")
    @Size(max = 100)
    private String neighborhood;

    @NotBlank(message = "A cidade é obrigatória")
    @Size(max = 100)
    private String city;

    @NotBlank(message = "O estado é obrigatório")
    @Size(max = 100)
    private String state;

    @NotBlank(message = "O CEP é obrigatório")
    @Size(max = 20)
    private String zipCode;

    @NotBlank(message = "O país é obrigatório")
    @Size(max = 100)
    private String country;
}

