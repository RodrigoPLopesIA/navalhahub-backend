package com.nexoralabs.navalhahub.identity.strategies;

import com.nexoralabs.navalhahub.identity.dto.IdentityUserRequest;
import com.nexoralabs.navalhahub.identity.dto.IdentityUserResponse;
import java.util.UUID;

public abstract class IdentityProvider {

    public abstract IdentityUserResponse createUser(
            IdentityUserRequest request
    );

    public abstract IdentityUserResponse findUser(
            UUID identityId
    );

    public abstract IdentityUserResponse updateUser(
            UUID identityId,
            IdentityUserRequest request
    );

    public abstract void deleteUser(UUID identityId);

    public abstract void resetPassword(
            UUID identityId,
            String password
    );

    public String generateIdentityId() {
        return UUID.randomUUID().toString();
    }
}

