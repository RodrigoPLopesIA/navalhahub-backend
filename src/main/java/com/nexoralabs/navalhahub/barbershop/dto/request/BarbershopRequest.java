
package com.nexoralabs.navalhahub.barbershop.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BarbershopRequest {

    @NotBlank(message = "O nome da barbearia é obrigatório")
    @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres")
    private String name;

    @NotBlank(message = "O slug é obrigatório")
    @Size(max = 140, message = "O slug deve ter no máximo 140 caracteres")
    private String slug;

    @NotNull(message = "O endereço é obrigatório")
    @Valid
    private AddressRequest address;

    @NotEmpty(message = "Informe pelo menos um telefone")
    @Valid
    private List<PhoneNumberRequest> phones;
}
