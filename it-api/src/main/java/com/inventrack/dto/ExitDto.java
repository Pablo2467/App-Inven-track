package com.inventrack.dto;

import com.inventrack.entity.ExitReason;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class ExitDto {
  private ExitDto() {}

  public record Request(
      @NotNull(message = "El producto es obligatorio") Long productId,
      @NotNull(message = "La cantidad es obligatoria") @DecimalMin(value = "0.001", message = "La cantidad debe ser mayor que cero") @Digits(integer = 11, fraction = 3, message = "La cantidad admite máximo 3 decimales") BigDecimal quantity,
      @NotNull(message = "El motivo es obligatorio") ExitReason reason,
      LocalDate exitDate,
      @Size(max = 500) String notes) {}

  /** Lote del que se descontaron unidades (trazabilidad FIFO). */
  public record Allocation(Long lotId, String lotNumber, LocalDate entryDate, LocalDate expirationDate, BigDecimal quantity) {}

  /** allocations viene lleno al registrar y al consultar por id; en los listados es null. */
  public record Response(Long id, Long productId, String productName, BigDecimal quantity, ExitReason reason, String notes,
                         LocalDate exitDate, Long userId, String userName, LocalDateTime createdAt, List<Allocation> allocations) {}
}
