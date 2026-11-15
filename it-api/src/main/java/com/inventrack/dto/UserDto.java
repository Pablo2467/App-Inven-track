package com.inventrack.dto;

import com.inventrack.entity.Role;
import com.inventrack.entity.Status;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public final class UserDto {
  private UserDto() {}

  public record CreateRequest(
      @NotBlank(message = "El nombre completo es obligatorio") @Size(max = 150) String fullName,
      @NotBlank(message = "El correo es obligatorio") @Email(message = "El correo electrónico no es válido") @Size(max = 150) String email,
      @NotBlank(message = "El nombre de usuario es obligatorio") @Size(min = 3, max = 60, message = "El usuario debe tener entre 3 y 60 caracteres")
      @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "El usuario solo admite letras, números, punto, guion y guion bajo") String username,
      @NotBlank(message = "La contraseña es obligatoria") @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres") String password,
      @NotNull(message = "El rol es obligatorio") Role role) {}

  /** password es opcional: si viene vacío no se cambia. */
  public record UpdateRequest(
      @NotBlank(message = "El nombre completo es obligatorio") @Size(max = 150) String fullName,
      @NotBlank(message = "El correo es obligatorio") @Email(message = "El correo electrónico no es válido") @Size(max = 150) String email,
      @NotNull(message = "El rol es obligatorio") Role role,
      @Size(max = 72) String password) {}

  public record StatusRequest(@NotNull(message = "El estado es obligatorio") Status status) {}

  /** Nunca incluye la contraseña ni su hash. */
  public record Response(Long id, String fullName, String email, String username, Role role, Status status,
                         LocalDateTime createdAt, LocalDateTime lastLoginAt) {}
}
