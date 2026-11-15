package com.inventrack.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class EntryDto {
  private EntryDto() {}

  /** lotNumber, entryDate y expirationDate son opcionales: se generan/calculan (vencimiento = ingreso + 30 días). */
  public record Request(
      @NotNull(message = "El producto es obligatorio") Long productId,
      @NotNull(message = "El proveedor es obligatorio") Long supplierId,
      @Size(max = 80) String lotNumber,
      @NotNull(message = "La cantidad es obligatoria") @DecimalMin(value = "0.001", message = "La cantidad debe ser mayor que cero") @Digits(integer = 11, fraction = 3, message = "La cantidad admite máximo 3 decimales") BigDecimal quantity,
      LocalDate entryDate,
      LocalDate expirationDate,
      @DecimalMin(value = "0", message = "El costo no puede ser negativo") @Digits(integer = 12, fraction = 2) BigDecimal unitCost,
      @Size(max = 500) String notes) {}

  public record Response(Long id, Long productId, String productName, Long supplierId, String supplierName,
                         Long lotId, String lotNumber, BigDecimal quantity, LocalDate entryDate, LocalDate expirationDate,
                         Long userId, String userName, String notes, LocalDateTime createdAt) {}
}
