package com.nexoralabs.navalhahub.barbershop.entity;
import com.nexoralabs.navalhahub.barbershop.roles.BarbershopRole;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "barbershop_members",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_barbershop_member",
                        columnNames = {"identity_id", "barbershop_id"}
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BarbershopMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "identity_id", nullable = false)
    private String identityID;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "barbershop_id", nullable = false)
    private Barbershop barbershop;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "barbershop_member_roles",
            joinColumns = @JoinColumn(name = "member_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @Builder.Default
    private Set<BarbershopRole> roles = new HashSet<>();

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @PrePersist
    public void prePersist() {
        joinedAt = LocalDateTime.now();
    }
}
