package com.nexoralabs.navalhahub.identity.service;

import com.nexoralabs.navalhahub.identity.dto.IdentityUserRequest;
import com.nexoralabs.navalhahub.identity.dto.IdentityUserResponse;
import com.nexoralabs.navalhahub.identity.strategies.IdentityProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IdentityService {

    private final IdentityProvider identityProvider;

    public IdentityUserResponse createUser(IdentityUserRequest request) {
        return identityProvider.createUser(request);
    }

    public IdentityUserResponse findUser(UUID userId) {
        return identityProvider.findUser(userId);
    }

    public IdentityUserResponse updateUser(
            UUID userId,
            IdentityUserRequest request
    ) {
        return identityProvider.updateUser(userId, request);
    }

    public void deleteUser(UUID userId) {
        identityProvider.deleteUser(userId);
    }

    public void resetPassword(UUID userId, String password) {
        identityProvider.resetPassword(userId, password);
    }
}
