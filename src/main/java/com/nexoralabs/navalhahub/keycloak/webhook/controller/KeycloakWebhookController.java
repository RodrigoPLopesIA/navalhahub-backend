package com.nexoralabs.navalhahub.keycloak.webhook.controller;

import com.nexoralabs.navalhahub.keycloak.webhook.dto.KeycloakWebhookRequest;
import com.nexoralabs.navalhahub.keycloak.webhook.service.KeycloakWebhookService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webhook")
@Slf4j
@AllArgsConstructor
public class KeycloakWebhookController {

    private final KeycloakWebhookService service;

    @PostMapping({"/users/sync", "/users/sync/"})
    public ResponseEntity<Void> sync(
            @RequestBody KeycloakWebhookRequest request
    ) {

        log.info(
                "Webhook recebido do Keycloak. Evento: {}, UserId: {}, user: {}",
                request.type(),
                request.userId(),
                request
        );

        service.sync(request);

        return ResponseEntity.ok().build();
    }
}
