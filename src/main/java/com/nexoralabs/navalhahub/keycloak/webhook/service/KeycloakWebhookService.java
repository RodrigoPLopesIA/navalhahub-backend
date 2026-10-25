package com.nexoralabs.navalhahub.keycloak.webhook.service;

import com.nexoralabs.navalhahub.User.entity.User;
import com.nexoralabs.navalhahub.User.repository.UserRepository;
import com.nexoralabs.navalhahub.keycloak.webhook.dto.KeycloakWebhookRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@Slf4j
public class KeycloakWebhookService {

    private final UserRepository userRepository;

    public KeycloakWebhookService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Transactional
    public void sync(KeycloakWebhookRequest request) {

        if (request.type() == null) {
            throw new IllegalArgumentException("Tipo do evento não informado");
        }

        String eventType = request.type()
                .trim()
                .toUpperCase(Locale.ROOT);

        if (!eventType.equals("REGISTER") && !eventType.equals("LOGIN")) {
            log.debug("Evento ignorado: {}", eventType);
            return;
        }

        if (request.userId() == null || request.userId().isBlank()) {
            throw new IllegalArgumentException("Keycloak userId não informado");
        }

        String keycloakId = request.userId().trim();

        if (userRepository.existsByKeycloakId(keycloakId)) {
            log.info("Usuário já cadastrado: {}", keycloakId);
            return;
        }

        String username = request.details() != null
                ? request.details().username()
                : null;

        User user = User.builder()
                .keycloakId(keycloakId)
                .username(username)
                .build();

        userRepository.save(user);

        log.info(
                "Novo usuário cadastrado. Keycloak ID: {}, Username: {}",
                keycloakId,
                username
        );
    }
}
