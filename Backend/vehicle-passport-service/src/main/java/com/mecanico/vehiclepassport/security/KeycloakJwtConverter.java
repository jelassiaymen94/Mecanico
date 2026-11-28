package com.mecanico.vehiclepassport.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Converts a Keycloak access token into the {@link AuthUser} principal used by the services.
 * <ul>
 *   <li>id: numeric {@code user_id} claim (Keycloak user attribute); falls back to a stable
 *       positive number derived from the {@code sub} UUID for self-registered users;</li>
 *   <li>role: highest Mecanico realm role (ADMIN &gt; FLEET_MANAGER &gt; PROVIDER &gt; CLIENT).</li>
 * </ul>
 */
public class KeycloakJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final List<String> ROLE_PRIORITY = List.of("ADMIN", "FLEET_MANAGER", "PROVIDER", "CLIENT");

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        List<String> roles = realmRoles(jwt);
        String mainRole = ROLE_PRIORITY.stream().filter(roles::contains).findFirst().orElse("CLIENT");
        AuthUser principal = new AuthUser(userId(jwt), jwt.getClaimAsString("email"), mainRole, fullName(jwt));
        Collection<GrantedAuthority> authorities = roles.stream()
                .filter(ROLE_PRIORITY::contains)
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
        return new UsernamePasswordAuthenticationToken(principal, jwt, authorities);
    }

    public static Long userId(Jwt jwt) {
        Object claim = jwt.getClaims().get("user_id");
        if (claim instanceof Number number) {
            return number.longValue();
        }
        if (claim instanceof String text && !text.isBlank()) {
            try {
                return Long.parseLong(text.trim());
            } catch (NumberFormatException ignored) {
                // fall through to the UUID-derived id
            }
        }
        return idFromSubject(jwt.getSubject());
    }

    static Long idFromSubject(String subject) {
        if (subject == null) {
            return 0L;
        }
        UUID uuid;
        try {
            uuid = UUID.fromString(subject);
        } catch (IllegalArgumentException e) {
            uuid = UUID.nameUUIDFromBytes(subject.getBytes(StandardCharsets.UTF_8));
        }
        // Offset keeps derived ids far from seeded demo ids (1..999 999).
        return 1_000_000L + ((uuid.getMostSignificantBits() ^ uuid.getLeastSignificantBits()) & 0x0000_FFFF_FFFF_FFFFL);
    }

    @SuppressWarnings("unchecked")
    private static List<String> realmRoles(Jwt jwt) {
        Object realmAccess = jwt.getClaims().get("realm_access");
        if (realmAccess instanceof Map<?, ?> map && map.get("roles") instanceof Collection<?> roles) {
            return ((Collection<Object>) roles).stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    private static String fullName(Jwt jwt) {
        String name = jwt.getClaimAsString("name");
        return name != null ? name : jwt.getClaimAsString("preferred_username");
    }
}
