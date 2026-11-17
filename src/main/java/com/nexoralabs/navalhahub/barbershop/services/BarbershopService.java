package com.nexoralabs.navalhahub.barbershop.services;

import com.nexoralabs.navalhahub.barbershop.dto.request.BarbershopRequest;
import com.nexoralabs.navalhahub.barbershop.dto.response.BarbershopResponse;
import com.nexoralabs.navalhahub.barbershop.entity.Barbershop;
import com.nexoralabs.navalhahub.barbershop.entity.BarbershopMember;
import com.nexoralabs.navalhahub.barbershop.mapper.BarbershopMapper;
import com.nexoralabs.navalhahub.barbershop.repository.BarbershopMemberRepository;
import com.nexoralabs.navalhahub.barbershop.repository.BarbershopRepository;
import com.nexoralabs.navalhahub.barbershop.roles.BarbershopRole;
import com.nexoralabs.navalhahub.barbershop.util.RolesUtil;
import com.nexoralabs.navalhahub.security.context.TenantContextHolder;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BarbershopService {

    private final BarbershopRepository barbershopRepository;
    private final BarbershopMemberRepository memberRepository;
    private final BarbershopMapper mapper;

    /** Cria a barbearia e o vinculo do OWNER na mesma transacao. */
    @Transactional
    public BarbershopResponse save(BarbershopRequest request, String identityId) {

        Barbershop barbershop = barbershopRepository.save(mapper.toEntity(request));

        BarbershopMember owner = BarbershopMember.builder()
                .identityID(identityId)
                .barbershop(barbershop)
                .roles(new HashSet<>(Set.of(BarbershopRole.OWNER)))
                .active(true)
                .build();

        memberRepository.save(owner);

        return mapper.toResponse(barbershop);
    }

    @Transactional(readOnly = true)
    public Page<BarbershopResponse> findMine(String identityId, Pageable pageable) {
        return memberRepository.findActiveByIdentityId(identityId, pageable)
                .map(member -> mapper.toResponse(member.getBarbershop()));
    }

    @Transactional(readOnly = true)
    public BarbershopResponse findById(String barbershopId) {
        TenantContextHolder.verifyBarberShopIdContext(barbershopId);
        return mapper.toResponse(getOrThrow(barbershopId));
    }

    @Transactional
    public BarbershopResponse update(String barbershopId, BarbershopRequest request) {

        var context = TenantContextHolder.verifyBarberShopIdContext(barbershopId);

        if (!RolesUtil.isAdmin(context.roles()) && !RolesUtil.isOwner(context.roles())) {
            throw new AccessDeniedException("Only ADMIN or OWNER can update the barbershop");
        }

        Barbershop barbershop = getOrThrow(barbershopId);
        mapper.updateEntity(barbershop, request);

        return mapper.toResponse(barbershop);
    }

    @Transactional
    public void delete(String barbershopId) {

        var context = TenantContextHolder.verifyBarberShopIdContext(barbershopId);

        if (!RolesUtil.isOwner(context.roles())) {
            throw new AccessDeniedException("Only the OWNER can delete the barbershop");
        }

        // Apaga os vinculos; as identidades globais permanecem no Keycloak.
        memberRepository.deleteAllByBarbershop_Id(barbershopId);
        barbershopRepository.deleteById(barbershopId);
    }

    private Barbershop getOrThrow(String barbershopId) {
        return barbershopRepository.findById(barbershopId)
                .orElseThrow(() -> new EntityNotFoundException("Barbershop not found"));
    }
}