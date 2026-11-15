package com.inventrack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDto {
  private AuthDto() {}

  public record LoginRequest(@NotBlank(message = "El usuario es obligatorio") String username,
                             @NotBlank(message = "La contraseña es obligatoria") String password) {}

  public record LoginResponse(String token, String tokenType, long expiresInSeconds, UserDto.Response user) {}

  public record ChangePasswordRequest(@NotBlank(message = "La contraseña actual es obligatoria") String currentPassword,
                                      @NotBlank @Size(min = 8, max = 72, message = "La nueva contraseña debe tener entre 8 y 72 caracteres") String newPassword) {}
}
