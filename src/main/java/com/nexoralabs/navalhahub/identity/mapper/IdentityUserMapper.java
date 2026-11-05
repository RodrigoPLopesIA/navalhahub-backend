package com.nexoralabs.navalhahub.identity.mapper;

import com.nexoralabs.navalhahub.User.dto.UserRequest;
import com.nexoralabs.navalhahub.User.dto.UserResponse;
import com.nexoralabs.navalhahub.identity.dto.IdentityUserRequest;
import com.nexoralabs.navalhahub.identity.dto.IdentityUserResponse;

import org.springframework.stereotype.Component;

@Component
public class IdentityUserMapper {

    public IdentityUserRequest toIdentityUserRequest(UserRequest request) {

        return IdentityUserRequest.builder()
                .username(request.username())
                .email(request.email())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .password(request.password())
                .enabled(request.enabled())
                .build();
    }

    public UserResponse toUserResponse(IdentityUserResponse response) {

        return new UserResponse(
                response.identityId(),
                response.username(),
                null,
                response.email(),
                response.firstName(),
                response.lastName(),
                response.enabled(),
               null
        );
    }
}