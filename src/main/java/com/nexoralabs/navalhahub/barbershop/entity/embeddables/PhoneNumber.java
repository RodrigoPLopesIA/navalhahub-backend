package com.nexoralabs.navalhahub.barbershop.entity.embeddables;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhoneNumber {
    @Column(nullable = false, length = 20)
    private String number;
    private String type;
}
