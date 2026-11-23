
package com.nexoralabs.navalhahub.customer.controller;

import com.nexoralabs.navalhahub.customer.dto.CustomerResponse;
import com.nexoralabs.navalhahub.customer.dto.UpdateCustomerRequest;
import com.nexoralabs.navalhahub.customer.dto.request.CreateCustomerRequest;
import com.nexoralabs.navalhahub.customer.service.CustomerService;
import com.nexoralabs.navalhahub.security.context.TenantContextHolder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@RestController
@RequestMapping("/api/barbershops/{barbershopId}/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerResponse> create(
            @PathVariable UUID barbershopId,
            @Valid @RequestBody CreateCustomerRequest request) {

        var response = customerService.create(barbershopId.toString(), request);

        var uri = UriComponentsBuilder
                .fromPath("/api/barbershops/{barbershopId}/customers/{customerId}")
                .buildAndExpand(barbershopId, response.id())
                .toUri();

        return ResponseEntity.created(uri).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<CustomerResponse>> list(
            @PathVariable UUID barbershopId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean blocked,
            Pageable pageable) {

        var identityId = TenantContextHolder.get().identityId();

        var response = customerService.list(
                identityId,
                barbershopId,
                search,
                blocked,
                pageable
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> get(
            @PathVariable UUID barbershopId,
            @PathVariable UUID customerId) {

        var identityId = TenantContextHolder.get().identityId();

        var response = customerService.get(
                identityId,
                barbershopId,
                customerId
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> update(
            @PathVariable UUID barbershopId,
            @PathVariable UUID customerId,
            @Valid @RequestBody UpdateCustomerRequest request) {

        var identityId = TenantContextHolder.get().identityId();

        var response = customerService.update(
                identityId,
                barbershopId,
                customerId,
                request
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{customerId}/block")
    public ResponseEntity<CustomerResponse> block(
            @PathVariable UUID barbershopId,
            @PathVariable UUID customerId) {

        var identityId = TenantContextHolder.get().identityId();

        var response = customerService.block(
                identityId,
                barbershopId,
                customerId
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{customerId}/unblock")
    public ResponseEntity<CustomerResponse> unblock(
            @PathVariable UUID barbershopId,
            @PathVariable UUID customerId) {

        var identityId = TenantContextHolder.get().identityId();

        var response = customerService.unblock(
                identityId,
                barbershopId,
                customerId
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> remove(
            @PathVariable UUID barbershopId,
            @PathVariable UUID customerId) {

        var identityId = TenantContextHolder.get().identityId();

        customerService.remove(
                identityId,
                barbershopId,
                customerId
        );

        return ResponseEntity.noContent().build();
    }
}
