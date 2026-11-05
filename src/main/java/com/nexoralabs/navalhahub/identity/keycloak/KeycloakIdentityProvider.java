
package com.nexoralabs.navalhahub.identity.keycloak;

import com.nexoralabs.navalhahub.identity.dto.IdentityUserRequest;
import com.nexoralabs.navalhahub.identity.dto.IdentityUserResponse;
import com.nexoralabs.navalhahub.identity.strategies.IdentityProvider;
import static com.nexoralabs.navalhahub.identity.constants.Constants.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.core.Response;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakIdentityProvider extends IdentityProvider {


    private final Keycloak keycloak;

    @Value("${keycloak.admin.realm}")
    private String realm;

    private RealmResource getRealm() {
        return keycloak.realm(realm);
    }

    @Override
    public IdentityUserResponse createUser(IdentityUserRequest request) {

        String identityId = generateIdentityId();

        UserRepresentation user = new UserRepresentation();

        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEnabled(request.enabled());
        user.setEmailVerified(false);

        Map<String, List<String>> attributes = new HashMap<>();

        attributes.put(
                IDENTITY_ID_ATTRIBUTE,
                List.of(identityId)
        );

        user.setAttributes(attributes);

        if (request.password() != null &&
                !request.password().isBlank()) {

            CredentialRepresentation credential =
                    new CredentialRepresentation();

            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(request.password());
            credential.setTemporary(true);

            user.setCredentials(List.of(credential));
        }

        try (Response response = getRealm()
                .users()
                .create(user)) {

            if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
                throw new IllegalStateException(
                        "Erro ao criar usuário no Keycloak. HTTP: "
                                + response.getStatus()
                );
            }

            return findUser(UUID.fromString(identityId));
        }
    }

    @Override
    public IdentityUserResponse findUser(UUID identityId) {

        UserRepresentation user = findUserRepresentation(identityId);

        return toResponse(user);
    }

    @Override
    public IdentityUserResponse updateUser(
            UUID identityId,
            IdentityUserRequest request
    ) {

        UserRepresentation existingUser =
                findUserRepresentation(identityId);

        UserResource userResource = getRealm()
                .users()
                .get(existingUser.getId());

        existingUser.setUsername(request.username());
        existingUser.setEmail(request.email());
        existingUser.setFirstName(request.firstName());
        existingUser.setLastName(request.lastName());
        existingUser.setEnabled(request.enabled());

        userResource.update(existingUser);

        return findUser(identityId);
    }

    @Override
    public void deleteUser(UUID identityId) {

        UserRepresentation user =
                findUserRepresentation(identityId);

        getRealm()
                .users()
                .get(user.getId())
                .remove();
    }

    @Override
    public void resetPassword(UUID identityId, String password) {

        UserRepresentation user =
                findUserRepresentation(identityId);

        CredentialRepresentation credential =
                new CredentialRepresentation();

        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);

        getRealm()
                .users()
                .get(user.getId())
                .resetPassword(credential);
    }

    private UserRepresentation findUserRepresentation(UUID identityId) {

        List<UserRepresentation> users = getRealm()
                .users()
                .searchByAttributes(
                        IDENTITY_ID_ATTRIBUTE + ":" + identityId
                );

        return users.stream()
                .filter(user -> user.getAttributes() != null)
                .filter(user -> user.getAttributes()
                        .getOrDefault(
                                IDENTITY_ID_ATTRIBUTE,
                                List.of()
                        )
                        .contains(identityId.toString()))
                .findFirst()
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Usuário não encontrado para identityId: "
                                        + identityId
                        )
                );
    }

    private IdentityUserResponse toResponse(UserRepresentation user) {

        String identityId = user.getAttributes()
                .get(IDENTITY_ID_ATTRIBUTE)
                .getFirst();

        return new IdentityUserResponse(
                identityId,
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.isEnabled()
        );
    }
    public UUID ensureIdentityId(String keycloakUserId) {

        UserResource userResource = getRealm()
                .users()
                .get(keycloakUserId);

        UserRepresentation user = userResource.toRepresentation();

        Map<String, List<String>> attributes = user.getAttributes();

        if (attributes == null) {
            attributes = new HashMap<>();
        }

        List<String> identityIds = attributes.get(IDENTITY_ID_ATTRIBUTE);

        String currentIdentityId = identityIds != null && !identityIds.isEmpty()
                ? identityIds.getFirst()
                : null;

        // Se já existe um identityId válido, mantém o atual.
        if (currentIdentityId != null && !currentIdentityId.isBlank()) {
            try {
                return UUID.fromString(currentIdentityId.trim());
            } catch (IllegalArgumentException exception) {
                // Se não for um UUID válido, gera um novo.
            }
        }

        UUID newIdentityId = UUID.randomUUID();

        attributes.put(
                IDENTITY_ID_ATTRIBUTE,
                List.of(newIdentityId.toString())
        );

        user.setAttributes(attributes);

        userResource.update(user);

        log.info(
                "Novo identityId atribuído ao usuário {}: {}",
                keycloakUserId,
                newIdentityId
        );

        return newIdentityId;
    }
}
