package com.nexoralabs.navalhahub.appointments.controller;

import com.nexoralabs.navalhahub.appointments.dto.AppointmentRequest;
import com.nexoralabs.navalhahub.appointments.dto.AppointmentResponse;
import com.nexoralabs.navalhahub.appointments.dto.AppointmentStatusRequest;
import com.nexoralabs.navalhahub.appointments.service.AppointmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/barbershops/{barbershopId}/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<AppointmentResponse> create(
            @PathVariable String barbershopId,
            @Valid @RequestBody AppointmentRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(appointmentService.create(barbershopId, request));
    }

    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> findAll(
            @PathVariable String barbershopId
    ) {

        return ResponseEntity.ok(
                appointmentService.findAll(barbershopId)
        );
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<AppointmentResponse> findById(
            @PathVariable String barbershopId,
            @PathVariable UUID appointmentId
    ) {

        return ResponseEntity.ok(
                appointmentService.findById(
                        barbershopId,
                        appointmentId
                )
        );
    }

    @PatchMapping("/{appointmentId}/cancel")
    public ResponseEntity<Void> cancel(
            @PathVariable String barbershopId,
            @PathVariable UUID appointmentId
    ) {

        appointmentService.cancel(barbershopId, appointmentId);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{appointmentId}/status")
    public ResponseEntity<AppointmentResponse> updateStatus(
            @PathVariable String barbershopId,
            @PathVariable UUID appointmentId,
            @Valid @RequestBody AppointmentStatusRequest request
    ) {

        return ResponseEntity.ok(
                appointmentService.updateStatus(
                        barbershopId,
                        appointmentId,
                        request.status()
                )
        );
    }
}
