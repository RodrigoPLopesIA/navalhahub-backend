package com.nexoralabs.navalhahub.barbershop.controller;


import com.nexoralabs.navalhahub.barbershop.dto.request.BarbershopRequest;
import com.nexoralabs.navalhahub.barbershop.dto.response.BarbershopResponse;
import com.nexoralabs.navalhahub.barbershop.services.BarbershopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("api/barbershops")
@RequiredArgsConstructor
public class BarbershopController {


    private final BarbershopService barbershopService;
    @PostMapping
    public ResponseEntity<BarbershopResponse> save(@RequestBody BarbershopRequest barbershop, @AuthenticationPrincipal Jwt jwt) {

        String identityId = jwt.getClaimAsString("identityId");
        var response = this.barbershopService.save(barbershop, identityId);
        var uri = UriComponentsBuilder.fromUriString("/api/barbershops/{id}").buildAndExpand(response.getId()).toUri();
        return ResponseEntity.created(uri).body(response);
    }
}
