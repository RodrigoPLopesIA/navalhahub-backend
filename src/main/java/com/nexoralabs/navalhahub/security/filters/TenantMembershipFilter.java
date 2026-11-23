package com.nexoralabs.navalhahub.security.filters;

import com.nexoralabs.navalhahub.security.services.TenantMembershipService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import com.nexoralabs.navalhahub.security.context.TenantContext;
import com.nexoralabs.navalhahub.security.context.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class TenantMembershipFilter extends OncePerRequestFilter {

    private static final Pattern TENANT_PATTERN =
            Pattern.compile("^/api/barbershops/([^/]+)(?:/.*)?$");

    private final TenantMembershipService tenantMembershipService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return extractBarbershopId(request) == null;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            var authentication =
                    SecurityContextHolder.getContext().getAuthentication();

            if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String identityId = jwtAuth.getToken()
                    .getClaimAsString("identityId");

            if (identityId == null || identityId.isBlank()) {
                response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Identity ID not found"
                );
                return;
            }

            String barbershopId = extractBarbershopId(request);

            TenantContext context = tenantMembershipService.findMembership(
                    identityId,
                    barbershopId
            );

            TenantContextHolder.set(context);

            filterChain.doFilter(request, response);

        } finally {
            TenantContextHolder.clear();
        }
    }

    private String extractBarbershopId(HttpServletRequest request) {

        Matcher matcher = TENANT_PATTERN.matcher(
                request.getRequestURI()
        );

        return matcher.matches() ? matcher.group(1) : null;
    }
}
