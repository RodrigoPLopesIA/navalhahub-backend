package com.nexoralabs.navalhahub.barbershop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BarbershopResponse {

    private String id;
    private String name;
    private String slug;
    private AddressResponse address;
    private List<PhoneNumberResponse> phones;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
