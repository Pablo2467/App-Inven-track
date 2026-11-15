package com.inventrack.security;

import com.inventrack.entity.Status;
import com.inventrack.entity.User;
import com.inventrack.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

/**
 * Valida el JWT, confirma en la base de datos que el usuario sigue activo y toma el rol vigente.
 * Si llega X-User-Id, debe coincidir con el usuario del token (si no, 403).
 */
public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtService jwt;
  private final UserRepository users;
  private final JsonErrorWriter errors;

  public JwtAuthFilter(JwtService jwt, UserRepository users, JsonErrorWriter errors) {
    this.jwt = jwt; this.users = users; this.errors = errors;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
    String header = req.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
      User user = null;
      try {
        Claims claims = jwt.parse(header.substring(7).trim());
        long uid = ((Number) claims.get("uid")).longValue();
        user = users.findById(uid).filter(u -> u.getStatus() == Status.ACTIVE).orElse(null);
      } catch (Exception ignored) {
        SecurityContextHolder.clearContext();
      }
      if (user != null) {
        String sent = req.getHeader("X-User-Id");
        if (sent != null && !sent.isBlank() && !sent.trim().equals(String.valueOf(user.getId()))) {
          errors.write(req, res, HttpStatus.FORBIDDEN, "X-User-Id no corresponde al usuario autenticado");
          return;
        }
        AuthenticatedUser principal = new AuthenticatedUser(user.getId(), user.getUsername(), user.getRole());
        var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
        SecurityContextHolder.getContext().setAuthentication(auth);
      }
    }
    chain.doFilter(req, res);
  }
}
