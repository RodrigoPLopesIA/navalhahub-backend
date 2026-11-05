package com.nexoralabs.navalhahub.security.services;

import com.nexoralabs.navalhahub.barbershop.repository.BarbershopMemberRepository;
import com.nexoralabs.navalhahub.security.context.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TenantMembershipService {

    private final BarbershopMemberRepository repository;

    @Transactional(readOnly = true)
    public TenantContext findMembership(
            String identityId,
            String barbershopId
    ) {
        var member = repository
                .findByIdentityIDAndBarbershopIdAndActiveTrue(
                        identityId,
                        barbershopId
                )
                .orElseThrow(() ->
                        new IllegalStateException("Membership not found")
                );

        return new TenantContext(
                identityId,
                barbershopId,
                member.getRoles()
        );
    }
}
