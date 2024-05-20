package com.kasperovich.patients.security;

import lombok.NonNull;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

    public JwtAuthConverter() {
        this.jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    }

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        // Extract the user roles and authorities from the JWT token
        final Set<GrantedAuthority> authorities = Stream.concat(
                jwtGrantedAuthoritiesConverter.convert(jwt).stream(),
                extractUserAuthorities(jwt).stream()
        ).collect(Collectors.toSet());

        // Extract the username from the JWT token
        String username = jwt.getClaimAsString("preferred_username");

        // Create a new JwtAuthenticationToken with the extracted authorities and username
        return new JwtAuthenticationToken(jwt, authorities, username);
    }

    private Set<? extends GrantedAuthority> extractUserAuthorities(Jwt jwt) {

        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();

        // Extracting realm roles
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            List<String> realmRoles = (List<String>) realmAccess.get("roles");
            if (!CollectionUtils.isEmpty(realmRoles)) {
                realmRoles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                        .forEach(grantedAuthorities::add);
            }
        }

        // Extracting resource access
        Map<String, Map<String, List<String>>> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null) {
            for (Map.Entry<String, Map<String, List<String>>> entry : resourceAccess.entrySet()) {
                List<String> clientRoles = entry.getValue().get("roles");
                if (!CollectionUtils.isEmpty(clientRoles)) {
                    clientRoles.stream()
                            .map(role -> new SimpleGrantedAuthority(entry.getKey() + "_" + role.toUpperCase()))
                            .forEach(grantedAuthorities::add);
                }
            }
        }

        return grantedAuthorities;
    }
}
