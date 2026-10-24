
package com.nexoralabs.navalhahub.barbershop.entity;

import com.nexoralabs.navalhahub.barbershop.entity.embeddables.Address;
import com.nexoralabs.navalhahub.barbershop.entity.embeddables.PhoneNumber;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "barbershops",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_barbershops_slug",
                        columnNames = "slug"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Barbershop {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 140)
    private String slug;

    @ElementCollection
    @CollectionTable(
            name = "barbershop_phones",
            joinColumns = @JoinColumn(name = "barbershop_id")
    )
    @Builder.Default
    private List<PhoneNumber> phones = new ArrayList<>();

    @Embedded
    private Address address;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
