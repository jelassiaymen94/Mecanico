package com.mecanico.vehiclepassport.security;

public record AuthUser(Long id, String email, String role, String fullName) {
}
