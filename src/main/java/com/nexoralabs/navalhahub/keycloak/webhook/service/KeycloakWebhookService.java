package com.nexoralabs.navalhahub.keycloak.webhook.service;

import com.nexoralabs.navalhahub.User.entity.User;
import com.nexoralabs.navalhahub.User.repository.UserRepository;
import com.nexoralabs.navalhahub.keycloak.webhook.dto.KeycloakWebhookRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class KeycloakWebhookService {

    private final UserRepository userRepository;

    @Transactional
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

        UUID userId = UUID.fromString(request.userId().trim());

        String username = request.details() != null
                ? request.details().username()
                : null;

        if (userRepository.existsById(userId)) {
            log.debug(
                    "Usuário já sincronizado. Keycloak ID: {}",
                    userId
            );
            return;
        }

        User user = User.builder()
                .id(userId)
                .username(username)
                .build();

        userRepository.save(user);

        log.info(
                "Usuário sincronizado com sucesso. Keycloak ID: {}, Username: {}",
                userId,
                username
        );
    }
}