package com.nexoralabs.navalhahub.barbershop.entity.embeddables;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Embeddable
@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {

    @NotBlank
    @Size(max = 150)
    @Column(name = "street", nullable = false, length = 150)
    private String street;

    @NotBlank
    @Size(max = 20)
    @Column(name = "number", nullable = false, length = 20)
    private String number;

    @Size(max = 100)
    @Column(name = "complement", length = 100)
    private String complement;

    @NotBlank
    @Size(max = 100)
    @Column(name = "neighborhood", nullable = false, length = 100)
    private String neighborhood;

    @NotBlank
    @Size(max = 100)
    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @NotBlank
    @Size(min = 2, max = 2)
    @Column(name = "state", nullable = false, length = 2)
    private String state;

    @NotBlank
    @Size(max = 9)
    @Column(name = "zip_code", nullable = false, length = 9)
    private String zipCode;

    @NotBlank
    @Size(max = 100)
    @Column(name = "country", nullable = false, length = 100)
    private String country = "Brasil";

}