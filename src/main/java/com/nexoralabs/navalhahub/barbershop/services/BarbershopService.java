package com.nexoralabs.navalhahub.barbershop.services;

import com.nexoralabs.navalhahub.barbershop.dto.request.BarbershopRequest;
import com.nexoralabs.navalhahub.barbershop.dto.response.BarbershopResponse;
import com.nexoralabs.navalhahub.barbershop.entity.Barbershop;
import com.nexoralabs.navalhahub.barbershop.entity.BarbershopMember;
import com.nexoralabs.navalhahub.barbershop.mapper.BarbershopMapper;
import com.nexoralabs.navalhahub.barbershop.repository.BarbershopMemberRepository;
import com.nexoralabs.navalhahub.barbershop.repository.BarbershopRepository;
import com.nexoralabs.navalhahub.barbershop.roles.BarbershopRole;
import com.nexoralabs.navalhahub.security.context.TenantContextHolder;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class BarbershopService {

    private final BarbershopRepository barbershopRepository;
    private final BarbershopMemberRepository barbershopMemberRepository;
    private final BarbershopMapper barbershopMapper;

    @Transactional
    public BarbershopResponse save(BarbershopRequest request, String identityId) {

        if (barbershopRepository
                .findBarbershopByName(request.getName())
                .isPresent()) {
            throw new IllegalArgumentException(
                    "Já existe uma barbearia com esse nome."
            );
        }


        Barbershop barbershop = barbershopMapper.toEntity(request);

        Barbershop saved = barbershopRepository.save(barbershop);

        BarbershopMember member = BarbershopMember.builder()
                .barbershop(saved)
                .identityID(identityId)
                .roles(Set.of(BarbershopRole.OWNER))
                .active(true)
                .joinedAt(LocalDateTime.now())
                .build();

        barbershopMemberRepository.save(member);

        saved.setMembers(new ArrayList<>(List.of(member)));

        return barbershopMapper.toResponse(saved);
    }
}

