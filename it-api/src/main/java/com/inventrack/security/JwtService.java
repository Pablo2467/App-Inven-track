package com.inventrack.security;

import com.inventrack.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
  private final SecretKey key;
  private final long expirationMs;

  public JwtService(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.expiration-ms}") long expirationMs) {
    byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
    if (bytes.length < 32) throw new IllegalStateException("JWT_SECRET debe tener al menos 32 caracteres");
    this.key = Keys.hmacShaKeyFor(bytes);
    this.expirationMs = expirationMs;
  }

  public String generate(User user) {
    long now = System.currentTimeMillis();
    return Jwts.builder()
        .subject(user.getUsername())
        .claim("uid", user.getId())
        .claim("role", user.getRole().name())
        .issuedAt(new Date(now))
        .expiration(new Date(now + expirationMs))
        .signWith(key)
        .compact();
  }

  /** Lanza JwtException si el token es inválido, está alterado o expiró. */
  public Claims parse(String token) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
  }

  public long expirationSeconds() { return expirationMs / 1000; }
}
