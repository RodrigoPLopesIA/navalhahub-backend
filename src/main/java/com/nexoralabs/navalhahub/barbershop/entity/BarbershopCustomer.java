package com.nexoralabs.navalhahub.barbershop.entity;
import com.nexoralabs.navalhahub.customer.entity.Customer;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "barbershop_customers",
        uniqueConstraints = @UniqueConstraint(columnNames = {"barbershop_id", "customer_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BarbershopCustomer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "barbershop_id", nullable = false)
    private Barbershop barbershop;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(length = 150)
    private String name;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(nullable = false)
    private boolean blocked;

    @Column(name = "consent_at")
    private Instant consentAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}