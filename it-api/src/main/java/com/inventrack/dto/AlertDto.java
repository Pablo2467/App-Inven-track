package com.inventrack.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class AlertDto {
  private AlertDto() {}

  /** type: EXPIRING, EXPIRED, DEPLETED, LOW_STOCK. severity: GREEN, YELLOW, RED. */
  public record Item(String type, String severity, Long productId, String productName, Long lotId, String lotNumber,
                     BigDecimal quantity, LocalDate expirationDate, Long daysLeft, String message) {}

  public record Response(int expiringCount, int expiredCount, int depletedCount, int lowStockCount, int thresholdDays, List<Item> items) {}
}
