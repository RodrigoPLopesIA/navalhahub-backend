package com.nexoralabs.navalhahub.barbershop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhoneNumberResponse {

    private String number;
    private String type;
}

