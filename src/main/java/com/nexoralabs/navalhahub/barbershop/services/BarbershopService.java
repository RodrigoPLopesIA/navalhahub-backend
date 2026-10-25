package com.nexoralabs.navalhahub.barbershop.services;

import com.nexoralabs.navalhahub.User.entity.User;
import com.nexoralabs.navalhahub.User.repository.UserRepository;

import com.nexoralabs.navalhahub.barbershop.dto.request.BarbershopRequest;
import com.nexoralabs.navalhahub.barbershop.dto.response.BarbershopResponse;
import com.nexoralabs.navalhahub.barbershop.entity.Barbershop;
import com.nexoralabs.navalhahub.barbershop.entity.BarbershopMember;
import com.nexoralabs.navalhahub.barbershop.mapper.BarbershopMapper;
import com.nexoralabs.navalhahub.barbershop.repository.BarbershopRepository;
import com.nexoralabs.navalhahub.barbershop.repository.BarbershopMemberRepository;
import com.nexoralabs.navalhahub.barbershop.roles.BarbershopRole;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BarbershopService {

    private final BarbershopRepository repository;
    private final BarbershopMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final BarbershopMapper mapper;

    // CREATE
    @Transactional
    public BarbershopResponse create(
            BarbershopRequest request,
            UUID userId
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuário não encontrado"
                ));

        if (repository.existsBySlug(request.getSlug())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe uma barbearia com esse slug"
            );
        }

        Barbershop barbershop = mapper.toEntity(request);

        Barbershop saved = repository.save(barbershop);

        BarbershopMember owner = BarbershopMember.builder()
                .user(user)
                .barbershop(saved)
                .roles(new HashSet<>(Set.of(BarbershopRole.OWNER)))
                .active(true)
                .build();

        memberRepository.save(owner);

        return mapper.toResponse(saved);
    }

    // FIND ALL
    @Transactional(readOnly = true)
    public Page<BarbershopResponse> findAllByOwner(
            UUID userId,
            Pageable pageable
    ) {

        return repository.findAllByUserIdAndRole(
                userId,
                BarbershopRole.OWNER,
                pageable
        ).map(mapper::toResponse);
    }

    // FIND BY ID
    @Transactional(readOnly = true)
    public BarbershopResponse findById(
            String id,
            UUID userId
    ) {

        return mapper.toResponse(
                getOwnedBarbershopOrThrow(id, userId)
        );
    }

    // UPDATE
    @Transactional
    public BarbershopResponse update(
            String id,
            BarbershopRequest request,
            UUID userId
    ) {

        Barbershop barbershop = getOwnedBarbershopOrThrow(id, userId);

        repository.findBySlug(request.getSlug())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Já existe uma barbearia com esse slug"
                    );
                });

        mapper.updateEntity(barbershop, request);

        return mapper.toResponse(repository.save(barbershop));
    }

    // UPDATE STATUS
    @Transactional
    public BarbershopResponse updateStatus(
            String id,
            boolean active,
            UUID userId
    ) {

        Barbershop barbershop = getOwnedBarbershopOrThrow(id, userId);

        barbershop.setActive(active);

        return mapper.toResponse(repository.save(barbershop));
    }

    // DELETE (SOFT DELETE)
    @Transactional
    public void delete(
            String id,
            UUID userId
    ) {

        Barbershop barbershop = getOwnedBarbershopOrThrow(id, userId);

        barbershop.setActive(false);

        repository.save(barbershop);
    }

    // PRIVATE METHODS
    private Barbershop getOwnedBarbershopOrThrow(
            String id,
            UUID userId
    ) {

        return repository.findByIdAndUserIdAndRole(
                id,
                userId,
                BarbershopRole.OWNER
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Barbearia não encontrada"
        ));
    }
}