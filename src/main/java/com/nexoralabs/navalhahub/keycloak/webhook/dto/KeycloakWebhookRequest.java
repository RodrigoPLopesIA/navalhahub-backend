package com.nexoralabs.navalhahub.keycloak.webhook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KeycloakWebhookRequest(
        String type,
        String realmId,
        String id,
        Long time,
        String clientId,
        String userId,
        String ipAddress,
        Details details
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Details(
            String username
    ) {}
}
