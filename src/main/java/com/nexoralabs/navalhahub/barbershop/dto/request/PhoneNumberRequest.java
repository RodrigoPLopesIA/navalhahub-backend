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
public class PhoneNumberRequest {

    @NotBlank(message = "O telefone é obrigatório")
    @Size(max = 30)
    private String number;
    private String type;
}
