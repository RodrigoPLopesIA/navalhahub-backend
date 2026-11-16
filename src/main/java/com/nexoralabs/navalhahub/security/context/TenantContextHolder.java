package com.nexoralabs.navalhahub.security.context;

import org.springframework.security.access.AccessDeniedException;

public final class TenantContextHolder {

    private static final ThreadLocal<TenantContext> CONTEXT =
            new ThreadLocal<>();

    private TenantContextHolder() {}

    public static void set(TenantContext context) {
        CONTEXT.set(context);
    }

    public static TenantContext get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public static TenantContext verifyBarberShopIdContext(String barbershopId){
        var context = TenantContextHolder.get();

        if (context == null ||
                !context.barbershopId().equals(barbershopId)) {

            throw new AccessDeniedException(
                    "Invalid barbershop context"
            );
        }
        return context;
    }
}
