package com.inventrack.dto;

import com.inventrack.entity.Status;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public final class SupplierDto {
  private SupplierDto() {}

  public record Request(
      @NotBlank(message = "El nombre o razón social es obligatorio") @Size(max = 150) String name,
      @NotBlank(message = "El NIT o identificación es obligatorio") @Size(max = 40) String taxId,
      @Size(max = 40) @Pattern(regexp = "^[0-9+()\\-\\s]*$", message = "El teléfono solo admite números, espacios, + - ( )") String phone,
      @Email(message = "El correo electrónico no es válido") @Size(max = 150) String email,
      @Size(max = 255) String address,
      Status status) {}

  public record Response(Long id, String name, String taxId, String phone, String email, String address,
                         Status status, LocalDateTime createdAt, LocalDateTime updatedAt) {}
}
