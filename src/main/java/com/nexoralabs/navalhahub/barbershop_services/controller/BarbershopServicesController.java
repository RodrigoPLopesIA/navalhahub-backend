package com.nexoralabs.navalhahub.barbershop_services.controller;


import com.nexoralabs.navalhahub.barbershop_services.dto.BarbershopServiceRequest;
import com.nexoralabs.navalhahub.barbershop_services.dto.BarbershopServiceResponse;
import com.nexoralabs.navalhahub.barbershop_services.service.BarbershopServicesService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/barbershops/{barbershopId}/services")
@RequiredArgsConstructor
public class BarbershopServicesController {

    private final BarbershopServicesService service;

    @PostMapping
    public ResponseEntity<BarbershopServiceResponse> create(
            @PathVariable String barbershopId,
            @Valid @RequestBody BarbershopServiceRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(barbershopId, request));
    }

    @GetMapping
    public ResponseEntity<List<BarbershopServiceResponse>> findAll(
            @PathVariable String barbershopId) {

        return ResponseEntity.ok(
                service.findAll(barbershopId)
        );
    }

    @PutMapping("/{serviceId}")
    public ResponseEntity<BarbershopServiceResponse> update(
            @PathVariable String barbershopId,
            @PathVariable UUID serviceId,
            @Valid @RequestBody BarbershopServiceRequest request) {

        return ResponseEntity.ok(
                service.update(barbershopId, serviceId, request)
        );
    }

    @DeleteMapping("/{serviceId}")
    public ResponseEntity<Void> deactivate(
            @PathVariable String barbershopId,
            @PathVariable UUID serviceId) {

        service.deactivate(barbershopId, serviceId);

        return ResponseEntity.noContent().build();
    }
}
