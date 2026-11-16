package com.nexoralabs.navalhahub.security.authorization;

import com.nexoralabs.navalhahub.barbershop.roles.BarbershopRole;
import com.nexoralabs.navalhahub.security.context.TenantContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component("tenantAuthorization")
public class TenantAuthorizationService {

    public boolean hasAnyRole(BarbershopRole... requiredRoles) {

        var context = TenantContextHolder.get();

        if (context == null) {
            return false;
        }

        return Arrays.stream(requiredRoles)
                .anyMatch(context.roles()::contains);
    }

    public boolean canManageUsers() {
        return hasAnyRole(
                BarbershopRole.OWNER,
                BarbershopRole.ADMIN
        );
    }
}