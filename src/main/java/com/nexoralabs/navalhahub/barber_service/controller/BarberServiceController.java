package com.nexoralabs.navalhahub.barber_service.controller;

import com.nexoralabs.navalhahub.barber_service.dto.BarberServiceResponse;
import com.nexoralabs.navalhahub.barber_service.service.BarberServiceService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/barbershops/{barbershopId}")
@RequiredArgsConstructor
public class BarberServiceController {

    private final BarberServiceService barberService;

    @PostMapping(
            "/services/{serviceId}/barbers/{targetMemberId}"
    )
    public ResponseEntity<BarberServiceResponse> assign(
            @PathVariable String barbershopId,
            @PathVariable UUID serviceId,
            @PathVariable String targetMemberId) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(barberService.assign(
                        barbershopId,
                        serviceId,
                        targetMemberId
                ));
    }

    @GetMapping(
            "/barbers/{targetMemberId}/services"
    )
    public ResponseEntity<List<BarberServiceResponse>> findAllByBarber(
            @PathVariable String barbershopId,
            @PathVariable String targetMemberId) {

        return ResponseEntity.ok(
                barberService.findAllByBarber(
                        barbershopId,
                        targetMemberId
                )
        );
    }

    @DeleteMapping(
            "/services/{serviceId}/barbers/{targetMemberId}"
    )
    public ResponseEntity<Void> unassign(
            @PathVariable String barbershopId,
            @PathVariable UUID serviceId,
            @PathVariable String targetMemberId) {

        barberService.unassign(
                barbershopId,
                serviceId,
                targetMemberId
        );

        return ResponseEntity.noContent().build();
    }
}
