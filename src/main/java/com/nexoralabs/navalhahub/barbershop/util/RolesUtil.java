package com.nexoralabs.navalhahub.barbershop.util;

import com.nexoralabs.navalhahub.barbershop.roles.BarbershopRole;
import org.springframework.security.access.AccessDeniedException;

public final class RolesUtil {

    public static void validateRoleAssignment(
            BarbershopRole requestedRole,
            java.util.Set<BarbershopRole> currentRoles
    ) {

        boolean isOwner = currentRoles.contains(
                BarbershopRole.OWNER
        );

        boolean isAdmin = currentRoles.contains(
                BarbershopRole.ADMIN
        );

        if (requestedRole == BarbershopRole.OWNER) {
            throw new AccessDeniedException(
                    "OWNER cannot be assigned through user creation"
            );
        }

        if (isAdmin && !isOwner &&
                requestedRole != BarbershopRole.BARBER) {
            throw new AccessDeniedException(
                    "ADMIN can only create BARBER users"
            );
        }
    }
}
