package com.nexoralabs.navalhahub.identity.keycloak.webhook.service;

import com.nexoralabs.navalhahub.identity.keycloak.KeycloakIdentityProvider;
import com.nexoralabs.navalhahub.identity.keycloak.webhook.dto.KeycloakWebhookRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class KeycloakWebhookService {

    private final KeycloakIdentityProvider identityProvider;

    public void sync(KeycloakWebhookRequest request) {

        if (request.type() == null || request.type().isBlank()) {
            throw new IllegalArgumentException(
                    "Tipo do evento não informado"
            );
        }

        String eventType = request.type()
                .trim()
                .toUpperCase(Locale.ROOT);

        if (!eventType.equals("REGISTER")
                && !eventType.equals("LOGIN")) {

            log.debug("Evento ignorado: {}", eventType);
            return;
        }

        if (request.userId() == null || request.userId().isBlank()) {
            throw new IllegalArgumentException(
                    "Keycloak userId não informado"
            );
        }

        String keycloakUserId = request.userId().trim();

        UUID identityId = identityProvider.ensureIdentityId(
                keycloakUserId
        );

        String username = request.details() != null
                ? request.details().username()
                : null;

        log.info(
                "Usuário verificado com sucesso. Event: {}, Keycloak ID: {}, Identity ID: {}, Username: {}",
                eventType,
                keycloakUserId,
                identityId,
                username
        );
    }
}