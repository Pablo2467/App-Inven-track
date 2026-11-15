package com.inventrack.service;

import com.inventrack.dto.AuthDto;
import com.inventrack.dto.UserDto;
import com.inventrack.entity.Status;
import com.inventrack.entity.User;
import com.inventrack.exception.BadRequestException;
import com.inventrack.exception.UnauthorizedException;
import com.inventrack.mapper.Mappers;
import com.inventrack.repository.UserRepository;
import com.inventrack.security.AuthenticatedUser;
import com.inventrack.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final UserRepository users;
  private final PasswordEncoder encoder;
  private final JwtService jwt;

  @Transactional
  public AuthDto.LoginResponse login(AuthDto.LoginRequest r) {
    String generic = "Usuario o contraseña incorrectos";
    User u = users.findByUsernameIgnoreCase(r.username().trim()).orElseThrow(() -> new UnauthorizedException(generic));
    if (u.getStatus() != Status.ACTIVE || !encoder.matches(r.password(), u.getPasswordHash())) throw new UnauthorizedException(generic);
    u.setLastLoginAt(LocalDateTime.now());
    users.save(u);
    return new AuthDto.LoginResponse(jwt.generate(u), "Bearer", jwt.expirationSeconds(), Mappers.user(u));
  }

  @Transactional(readOnly = true)
  public UserDto.Response me(AuthenticatedUser auth) {
    return Mappers.user(users.findById(auth.id()).orElseThrow(() -> new UnauthorizedException("Sesión inválida")));
  }

  @Transactional
  public void changePassword(AuthenticatedUser auth, AuthDto.ChangePasswordRequest r) {
    User u = users.findById(auth.id()).orElseThrow(() -> new UnauthorizedException("Sesión inválida"));
    if (!encoder.matches(r.currentPassword(), u.getPasswordHash())) throw new BadRequestException("La contraseña actual no coincide");
    u.setPasswordHash(encoder.encode(r.newPassword()));
    users.save(u);
  }
}
