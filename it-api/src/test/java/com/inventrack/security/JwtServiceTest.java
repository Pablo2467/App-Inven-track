package com.inventrack.security;

import com.inventrack.entity.Role;
import com.inventrack.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
  private static final String SECRET = "una-clave-de-prueba-con-mas-de-32-caracteres-1234567890";
  private static final long EIGHT_HOURS = 8L * 60 * 60 * 1000;

  private User user() {
    User u = new User();
    u.setId(7L); u.setUsername("empleada"); u.setRole(Role.EMPLEADA);
    return u;
  }

  @Test
  void generatedToken_containsUserAndRole_andExpiresIn8Hours() {
    JwtService jwt = new JwtService(SECRET, EIGHT_HOURS);
    Claims c = jwt.parse(jwt.generate(user()));
    assertEquals("empleada", c.getSubject());
    assertEquals(7, ((Number) c.get("uid")).intValue());
    assertEquals("EMPLEADA", c.get("role", String.class));
    assertEquals(EIGHT_HOURS, c.getExpiration().getTime() - c.getIssuedAt().getTime());
    assertEquals(8 * 3600, jwt.expirationSeconds());
  }

  @Test
  void tokenSignedWithAnotherKey_isRejected() {
    JwtService other = new JwtService("otra-clave-totalmente-distinta-de-mas-de-32-caracteres!!", EIGHT_HOURS);
    String forged = other.generate(user());
    assertThrows(JwtException.class, () -> new JwtService(SECRET, EIGHT_HOURS).parse(forged));
  }

  @Test
  void expiredToken_isRejected() {
    JwtService jwt = new JwtService(SECRET, -1000);
    String token = jwt.generate(user());
    assertThrows(ExpiredJwtException.class, () -> jwt.parse(token));
  }

  @Test
  void garbageToken_isRejected() {
    assertThrows(JwtException.class, () -> new JwtService(SECRET, EIGHT_HOURS).parse("esto.no.es-un-jwt"));
  }

  @Test
  void shortSecret_isRejected() {
    assertThrows(IllegalStateException.class, () -> new JwtService("corta", EIGHT_HOURS));
  }
}
