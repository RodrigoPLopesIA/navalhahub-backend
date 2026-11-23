package com.nexoralabs.navalhahub.barber_availability.controller;

import com.nexoralabs.navalhahub.barber_availability.dto.BarberAvailabilityRequest;
import com.nexoralabs.navalhahub.barber_availability.dto.BarberAvailabilityResponse;
import com.nexoralabs.navalhahub.barber_availability.service.BarberAvailabilityService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/barbershops/{barbershopId}/barber/availabilities")
@RequiredArgsConstructor
public class BarberAvailabilityController {

    private final BarberAvailabilityService availabilityService;

    @PostMapping
    public ResponseEntity<BarberAvailabilityResponse> create(
            @PathVariable String barbershopId,
            @Valid @RequestBody BarberAvailabilityRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(availabilityService.create(barbershopId, request));
    }

    @GetMapping
    public ResponseEntity<List<BarberAvailabilityResponse>> findAll(
            @PathVariable String barbershopId) {

        return ResponseEntity.ok(
                availabilityService.findAll(barbershopId)
        );
    }

    @PutMapping("/{availabilityId}")
    public ResponseEntity<BarberAvailabilityResponse> update(
            @PathVariable String barbershopId,
            @PathVariable UUID availabilityId,
            @Valid @RequestBody BarberAvailabilityRequest request) {

        return ResponseEntity.ok(
                availabilityService.update(
                        barbershopId,
                        availabilityId,
                        request
                )
        );
    }

    @DeleteMapping("/{availabilityId}")
    public ResponseEntity<Void> delete(
            @PathVariable String barbershopId,
            @PathVariable UUID availabilityId) {

        availabilityService.delete(barbershopId, availabilityId);

        return ResponseEntity.noContent().build();
    }
}
