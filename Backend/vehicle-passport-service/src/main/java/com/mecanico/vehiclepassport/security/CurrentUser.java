package com.mecanico.vehiclepassport.security;

import com.mecanico.vehiclepassport.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    public AuthUser require() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUser user)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return user;
    }

    public Long id() {
        return require().id();
    }

    public boolean isAdmin() {
        return "ADMIN".equals(require().role());
    }

    public boolean isFleetManager() {
        return "FLEET_MANAGER".equals(require().role());
    }

    public boolean canAccessAllVehicles() {
        return isAdmin() || isFleetManager();
    }
}
