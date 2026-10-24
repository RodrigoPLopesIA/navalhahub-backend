package com.nexoralabs.navalhahub.barbershop.services;

import com.nexoralabs.navalhahub.barbershop.dto.request.BarbershopRequest;
import com.nexoralabs.navalhahub.barbershop.dto.response.BarbershopResponse;
import com.nexoralabs.navalhahub.barbershop.entity.Barbershop;
import com.nexoralabs.navalhahub.barbershop.mapper.BarbershopMapper;
import com.nexoralabs.navalhahub.barbershop.repository.BarbershopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BarbershopService {

    private final BarbershopRepository repository;
    private final BarbershopMapper mapper;

    @Transactional
    public BarbershopResponse create(BarbershopRequest request) {
        if (repository.existsBySlug(request.getSlug())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe uma barbearia com esse slug"
            );
        }

        Barbershop barbershop = mapper.toEntity(request);
        Barbershop saved = repository.save(barbershop);

        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<BarbershopResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);

    }

    @Transactional(readOnly = true)
    public BarbershopResponse findById(String id) {
        return mapper.toResponse(getBarbershopOrThrow(id));
    }

    @Transactional
    public BarbershopResponse update(
            String id,
            BarbershopRequest request
    ) {
        Barbershop barbershop = getBarbershopOrThrow(id);

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

    @Transactional
    public BarbershopResponse updateStatus(
            String id,
            boolean active
    ) {
        Barbershop barbershop = getBarbershopOrThrow(id);
        barbershop.setActive(active);

        return mapper.toResponse(repository.save(barbershop));
    }

    @Transactional
    public void delete(String id) {
        Barbershop barbershop = getBarbershopOrThrow(id);
        repository.delete(barbershop);
    }

    private Barbershop getBarbershopOrThrow(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Barbearia não encontrada"
                ));
    }
}

