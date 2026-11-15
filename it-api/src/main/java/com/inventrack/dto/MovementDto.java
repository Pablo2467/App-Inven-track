package com.inventrack.dto;

import com.inventrack.entity.MovementType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class MovementDto {
  private MovementDto() {}

  public record Response(Long id, MovementType type, Long productId, String productName, Long lotId, String lotNumber,
                         BigDecimal quantity, Long entryId, Long outputId, String detail, Long userId, String userName,
                         LocalDateTime createdAt) {}
}
