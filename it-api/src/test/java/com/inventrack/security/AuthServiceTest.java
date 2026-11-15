package com.inventrack.security;

import com.inventrack.dto.AuthDto;
import com.inventrack.entity.Role;
import com.inventrack.entity.Status;
import com.inventrack.entity.User;
import com.inventrack.exception.UnauthorizedException;
import com.inventrack.repository.UserRepository;
import com.inventrack.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {
  UserRepository users;
  PasswordEncoder encoder = new BCryptPasswordEncoder();
  JwtService jwt = new JwtService("una-clave-de-prueba-con-mas-de-32-caracteres-1234567890", 8L * 3600 * 1000);
  AuthService auth;
  User dueno;

  @BeforeEach
  void setUp() {
    users = mock(UserRepository.class);
    auth = new AuthService(users, encoder, jwt);
    dueno = new User();
    dueno.setId(1L); dueno.setUsername("dueno"); dueno.setFullName("Dueño"); dueno.setEmail("d@test.com");
    dueno.setRole(Role.DUENO); dueno.setStatus(Status.ACTIVE); dueno.setPasswordHash(encoder.encode("Dueno123*"));
    when(users.findByUsernameIgnoreCase("dueno")).thenReturn(Optional.of(dueno));
    when(users.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
  }

  @Test
  void login_validCredentials_returnsJwtAndUserWithoutPassword() {
    AuthDto.LoginResponse r = auth.login(new AuthDto.LoginRequest("dueno", "Dueno123*"));
    assertEquals("Bearer", r.tokenType());
    assertEquals(8 * 3600, r.expiresInSeconds());
    assertEquals("dueno", jwt.parse(r.token()).getSubject());
    assertEquals(Role.DUENO, r.user().role());
    assertNotNull(dueno.getLastLoginAt());
  }

  @Test
  void login_wrongPassword_isUnauthorized() {
    assertThrows(UnauthorizedException.class, () -> auth.login(new AuthDto.LoginRequest("dueno", "incorrecta")));
  }

  @Test
  void login_unknownUser_isUnauthorized_withSameMessage() {
    when(users.findByUsernameIgnoreCase("nadie")).thenReturn(Optional.empty());
    UnauthorizedException a = assertThrows(UnauthorizedException.class, () -> auth.login(new AuthDto.LoginRequest("nadie", "x")));
    UnauthorizedException b = assertThrows(UnauthorizedException.class, () -> auth.login(new AuthDto.LoginRequest("dueno", "mala")));
    assertEquals(a.getMessage(), b.getMessage());
  }

  @Test
  void login_inactiveUser_isUnauthorized() {
    dueno.setStatus(Status.INACTIVE);
    assertThrows(UnauthorizedException.class, () -> auth.login(new AuthDto.LoginRequest("dueno", "Dueno123*")));
  }
}
