package com.nexoralabs.navalhahub.customer.mapper;

import com.nexoralabs.navalhahub.barbershop.entity.BarbershopCustomer;
import com.nexoralabs.navalhahub.barbershop.entity.embeddables.PhoneNumber;

import com.nexoralabs.navalhahub.customer.dto.CustomerResponse;
import com.nexoralabs.navalhahub.customer.dto.PhoneNumberDto;
import com.nexoralabs.navalhahub.customer.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public CustomerResponse toResponse(BarbershopCustomer link) {
        Customer customer = link.getCustomer();
        return new CustomerResponse(
                customer.getId(),
                displayName(link),
                toDto(customer.getPhoneNumber()),
                link.getNotes(),
                link.isBlocked(),
                link.getConsentAt(),
                link.getCreatedAt()
        );
    }

    // ---- Telefone: ÚNICO ponto que conhece a estrutura do embeddable PhoneNumber ----
    // Premissa: PhoneNumber tem os campos countryCode e number e um construtor (countryCode, number).

    public PhoneNumber toEntity(PhoneNumberDto dto) {
        return new PhoneNumber(digits(dto.countryCode()), dto.type(), digits(dto.number()));
    }

    public PhoneNumberDto toDto(PhoneNumber phone) {
        return phone == null ? null : new PhoneNumberDto(phone.getCountryCode(), phone.getType(), phone.getNumber());
    }

    private String displayName(BarbershopCustomer link) {
        if (link.getName() != null && !link.getName().isBlank()) {
            return link.getName();
        }
        Customer c = link.getCustomer();
        String last = c.getLastName() == null ? "" : " " + c.getLastName();
        return (c.getFirstName() + last).trim();
    }

    private String digits(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }
}