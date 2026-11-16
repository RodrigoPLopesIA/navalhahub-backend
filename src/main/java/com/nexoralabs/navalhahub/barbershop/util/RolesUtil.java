package com.nexoralabs.navalhahub.barbershop.util;

import com.nexoralabs.navalhahub.barbershop.roles.BarbershopRole;

import java.util.Set;

import org.springframework.security.access.AccessDeniedException;

public final class RolesUtil {

    public static void validateRoleAssignment(
            BarbershopRole requestedRole,
            java.util.Set<BarbershopRole> currentRoles
    ) {


        if (requestedRole == BarbershopRole.OWNER) {
            throw new AccessDeniedException(
                    "OWNER cannot be assigned through user creation"
            );
        }

        if (isAdmin(currentRoles) && !isOwner(currentRoles) &&
                requestedRole != BarbershopRole.BARBER) {
            throw new AccessDeniedException(
                    "ADMIN can only create BARBER users"
            );
        }
    }

    public static boolean isAdmin(Set<BarbershopRole> currentRoles) {
        return currentRoles.contains(BarbershopRole.ADMIN);
    }

    public static boolean isOwner(Set<BarbershopRole> currentRoles){
        return currentRoles.contains(
                BarbershopRole.OWNER
        );
    }
}
