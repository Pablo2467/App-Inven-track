package com.inventrack.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class InventoryDto {
  private InventoryDto() {}

  /** status: GREEN (sin alerta), YELLOW (próximo a vencer o stock bajo), RED (vencido o sin existencias). */
  public record Item(Long productId, String code, String name, String category, String unit, BigDecimal minStock,
                     BigDecimal stock, BigDecimal expiringQuantity, BigDecimal expiredQuantity,
                     LocalDate nearestExpiration, boolean lowStock, String status) {}
}
