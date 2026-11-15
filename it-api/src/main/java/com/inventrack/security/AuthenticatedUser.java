package com.inventrack.security;

import com.inventrack.entity.Role;

/** Usuario autenticado tomado del contexto de seguridad (nunca del cliente). */
public record AuthenticatedUser(Long id, String username, Role role) {}
