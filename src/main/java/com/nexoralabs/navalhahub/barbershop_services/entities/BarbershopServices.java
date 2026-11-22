
package com.nexoralabs.navalhahub.barbershop_services.entities;

import com.nexoralabs.navalhahub.barbershop.entity.Barbershop;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
        name = "barbershop_services",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_barbershop_service_name",
                        columnNames = {"barbershop_id", "name"}
                )
        }
)
@Getter
@Setter
public class BarbershopServices {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barbershop_id", nullable = false)
    private Barbershop barbershop;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(nullable = false)
    private boolean active = true;
}
