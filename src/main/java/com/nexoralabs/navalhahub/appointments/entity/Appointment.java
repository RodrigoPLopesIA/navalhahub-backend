package com.nexoralabs.navalhahub.appointments.entity;

import com.nexoralabs.navalhahub.appointments.enums.AppointmentStatus;
import com.nexoralabs.navalhahub.barbershop.entity.Barbershop;
import com.nexoralabs.navalhahub.barbershop.entity.BarbershopMember;
import com.nexoralabs.navalhahub.barbershop_services.entities.BarbershopServices;
import com.nexoralabs.navalhahub.customer.entity.Customer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointments")
@Getter
@Setter
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "barbershop_id", nullable = false)
    private Barbershop barbershop;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "barber_member_id", nullable = false)
    private BarbershopMember barber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private BarbershopServices service;

    @Column(nullable = false)
    private OffsetDateTime startAt;

    @Column(nullable = false)
    private OffsetDateTime endAt;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
