package com.inventrack.controller;

import com.inventrack.dto.AuthDto;
import com.inventrack.dto.CommonDto.MessageResponse;
import com.inventrack.dto.UserDto;
import com.inventrack.security.AuthenticatedUser;
import com.inventrack.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService auth;

  @PostMapping("/login")
  public AuthDto.LoginResponse login(@Valid @RequestBody AuthDto.LoginRequest r) { return auth.login(r); }

  @GetMapping("/me")
  public UserDto.Response me(@AuthenticationPrincipal AuthenticatedUser user) { return auth.me(user); }

  @PostMapping("/change-password")
  public MessageResponse changePassword(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody AuthDto.ChangePasswordRequest r) {
    auth.changePassword(user, r);
    return new MessageResponse("OK", "Contraseña actualizada");
  }
}
