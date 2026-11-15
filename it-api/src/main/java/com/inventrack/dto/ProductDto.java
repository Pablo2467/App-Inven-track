package com.inventrack.dto;

import com.inventrack.entity.Status;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class ProductDto {
  private ProductDto() {}

  public record Request(
      @NotBlank(message = "El código es obligatorio") @Size(max = 40, message = "El código admite máximo 40 caracteres") String code,
      @NotBlank(message = "El nombre es obligatorio") @Size(max = 150, message = "El nombre admite máximo 150 caracteres") String name,
      @Size(max = 500, message = "La descripción admite máximo 500 caracteres") String description,
      @Size(max = 100, message = "La categoría admite máximo 100 caracteres") String category,
      @NotBlank(message = "La unidad de medida es obligatoria") @Size(max = 30) String unit,
      @DecimalMin(value = "0", message = "El stock mínimo no puede ser negativo") @Digits(integer = 11, fraction = 3) BigDecimal minStock,
      Status status) {}

  public record Response(Long id, String code, String name, String description, String category, String unit,
                         BigDecimal minStock, Status status, LocalDateTime createdAt, LocalDateTime updatedAt) {}
}
