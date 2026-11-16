package com.nexoralabs.navalhahub.security.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Component
public class KeycloakJwtRolesConverter
        implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String CLIENT_ID = "navalha-hub-client";

    private final JwtGrantedAuthoritiesConverter scopesConverter =
            new JwtGrantedAuthoritiesConverter();

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {

        Collection<GrantedAuthority> authorities =
                new ArrayList<>(scopesConverter.convert(jwt));

        Map<String, Object> resourceAccess =
                jwt.getClaim("resource_access");

        if (resourceAccess == null) {
            return authorities;
        }

        Object clientAccess = resourceAccess.get(CLIENT_ID);

        if (!(clientAccess instanceof Map<?, ?> clientRoles)) {
            return authorities;
        }

        Object rolesObject = clientRoles.get("roles");

        if (!(rolesObject instanceof Collection<?> roles)) {
            return authorities;
        }

        roles.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .forEach(authorities::add);

        return authorities;
    }
}
