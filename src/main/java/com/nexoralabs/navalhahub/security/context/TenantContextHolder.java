package com.nexoralabs.navalhahub.security.context;

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
}
