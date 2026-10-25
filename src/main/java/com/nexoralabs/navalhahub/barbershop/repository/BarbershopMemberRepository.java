package com.nexoralabs.navalhahub.barbershop.repository;

import com.nexoralabs.navalhahub.barbershop.entity.BarbershopMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BarbershopMemberRepository
        extends JpaRepository<BarbershopMember, String> {
}
