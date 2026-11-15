package com.inventrack.dto;

import com.inventrack.entity.LotStatus;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class LotDto {
  private LotDto() {}

  /** Solo se pueden corregir datos informativos; las cantidades solo cambian por entradas y salidas. */
  public record UpdateRequest(
      @NotBlank(message = "El número de lote es obligatorio") @Size(max = 80) String lotNumber,
      @NotNull(message = "La fecha de vencimiento es obligatoria") LocalDate expirationDate,
      @DecimalMin(value = "0", message = "El costo no puede ser negativo") @Digits(integer = 12, fraction = 2) BigDecimal unitCost) {}

  public record Response(Long id, String lotNumber, Long productId, String productCode, String productName,
                         Long supplierId, String supplierName, BigDecimal initialQuantity, BigDecimal availableQuantity,
                         LocalDate entryDate, LocalDate expirationDate, BigDecimal unitCost, LotStatus status, long daysToExpire) {}
}
