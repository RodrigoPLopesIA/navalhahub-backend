package com.nexoralabs.navalhahub.barber_service.entity;


import com.nexoralabs.navalhahub.barbershop.entity.BarbershopMember;
import com.nexoralabs.navalhahub.barbershop_services.entities.BarbershopServices;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
        name = "barber_services",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_barber_service_member",
                        columnNames = {
                                "barbershop_member_id",
                                "service_id"
                        }
                )
        }
)
@Getter
@Setter
public class BarberService {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barbershop_member_id", nullable = false)
    private BarbershopMember barbershopMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private BarbershopServices service;

    @Column(nullable = false)
    private boolean active = true;
}

