package com.nexoralabs.navalhahub.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Extrai do JWT do Keycloak o identity id do usuário.
 * O claim é configurável: navalhahub.security.identity-claim (padrão: identity_id).
 * No Keycloak, o atributo precisa de um "User Attribute" protocol mapper que o inclua no access token.
 */
@Component
public class IdentityResolver {

    private final String identityClaim;

    public IdentityResolver(@Value("${navalhahub.security.identity-claim:identity_id}") String identityClaim) {
        this.identityClaim = identityClaim;
    }

    public String resolve(Jwt jwt) {
        String identityId = jwt == null ? null : jwt.getClaimAsString(identityClaim);
        if (identityId == null || identityId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Token sem o claim de identidade: " + identityClaim);
        }
        return identityId;
    }
}