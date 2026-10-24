package com.nexoralabs.navalhahub.barbershop.mapper;

import com.nexoralabs.navalhahub.barbershop.dto.request.AddressRequest;
import com.nexoralabs.navalhahub.barbershop.dto.request.BarbershopRequest;
import com.nexoralabs.navalhahub.barbershop.dto.request.PhoneNumberRequest;
import com.nexoralabs.navalhahub.barbershop.dto.response.AddressResponse;
import com.nexoralabs.navalhahub.barbershop.dto.response.BarbershopResponse;
import com.nexoralabs.navalhahub.barbershop.dto.response.PhoneNumberResponse;
import com.nexoralabs.navalhahub.barbershop.entity.Barbershop;
import com.nexoralabs.navalhahub.barbershop.entity.embeddables.Address;
import com.nexoralabs.navalhahub.barbershop.entity.embeddables.PhoneNumber;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BarbershopMapper {

    public Barbershop toEntity(BarbershopRequest request) {
        return Barbershop.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .address(toAddressEntity(request.getAddress()))
                .phones(toPhoneEntities(request.getPhones()))
                .build();
    }

    public void updateEntity(
            Barbershop entity,
            BarbershopRequest request
    ) {
        entity.setName(request.getName());
        entity.setSlug(request.getSlug());
        entity.setAddress(toAddressEntity(request.getAddress()));

        entity.getPhones().clear();
        entity.getPhones().addAll(toPhoneEntities(request.getPhones()));
    }

    public BarbershopResponse toResponse(Barbershop entity) {
        return BarbershopResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .slug(entity.getSlug())
                .address(toAddressResponse(entity.getAddress()))
                .phones(toPhoneResponses(entity.getPhones()))
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private Address toAddressEntity(AddressRequest request) {
        if (request == null) {
            return null;
        }

        return Address.builder()
                .street(request.getStreet())
                .number(request.getNumber())
                .complement(request.getComplement())
                .neighborhood(request.getNeighborhood())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .country(request.getCountry())
                .build();
    }

    private AddressResponse toAddressResponse(Address address) {
        if (address == null) {
            return null;
        }

        return AddressResponse.builder()
                .street(address.getStreet())
                .number(address.getNumber())
                .complement(address.getComplement())
                .neighborhood(address.getNeighborhood())
                .city(address.getCity())
                .state(address.getState())
                .zipCode(address.getZipCode())
                .country(address.getCountry())
                .build();
    }

    private List<PhoneNumber> toPhoneEntities(
            List<PhoneNumberRequest> requests
    ) {
        if (requests == null) {
            return new ArrayList<>();
        }

        return requests.stream()
                .map(this::toPhoneEntity)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private PhoneNumber toPhoneEntity(PhoneNumberRequest request) {
        return PhoneNumber.builder()
                .number(request.getNumber())
                .type(request.getType())
                .build();
    }

    private List<PhoneNumberResponse> toPhoneResponses(
            List<PhoneNumber> phones
    ) {
        if (phones == null) {
            return List.of();
        }

        return phones.stream()
                .map(phone -> PhoneNumberResponse.builder()
                        .number(phone.getNumber())
                        .type(phone.getType())
                        .build())
                .toList();
    }
}

