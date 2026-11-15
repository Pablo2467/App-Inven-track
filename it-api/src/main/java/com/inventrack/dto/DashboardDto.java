package com.inventrack.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class DashboardDto {
  private DashboardDto() {}

  public record DailyPoint(LocalDate date, BigDecimal entries, BigDecimal exits) {}

  public record Response(long totalProducts, long totalSuppliers, long activeUsers, BigDecimal availableUnits,
                         long totalEntries, long totalExits, long expiringLots, long expiredLots, long depletedLots,
                         long greenProducts, long yellowProducts, long redProducts,
                         List<DailyPoint> chart, List<InventoryDto.Item> lowStockProducts,
                         List<MovementDto.Response> recentMovements, List<AlertDto.Item> alerts) {}
}
