package com.inventrack.service;

import com.inventrack.dto.AlertDto;
import com.inventrack.dto.DashboardDto;
import com.inventrack.dto.InventoryDto;
import com.inventrack.entity.Status;
import com.inventrack.mapper.Mappers;
import com.inventrack.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {
  private static final int CHART_DAYS = 14;

  private final ProductRepository products;
  private final SupplierRepository suppliers;
  private final UserRepository users;
  private final LotRepository lots;
  private final StockEntryRepository entries;
  private final StockExitRepository exits;
  private final InventoryMovementRepository movements;
  private final InventoryService inventory;
  private final AlertService alerts;
  private final Clock clock;

  @Transactional(readOnly = true)
  public DashboardDto.Response get() {
    LocalDate today = LocalDate.now(clock);
    List<InventoryDto.Item> items = inventory.list(null, null);
    AlertDto.Response a = alerts.get(null, null);

    Comparator<AlertDto.Item> bySeverity = Comparator.comparingInt(i -> switch (i.severity()) { case "RED" -> 0; case "YELLOW" -> 1; default -> 2; });
    List<AlertDto.Item> topAlerts = a.items().stream().sorted(bySeverity).limit(6).toList();

    List<InventoryDto.Item> lowest = items.stream().sorted(Comparator.comparing(InventoryDto.Item::stock)).limit(5).toList();

    return new DashboardDto.Response(
        products.countByStatus(Status.ACTIVE), suppliers.countByStatus(Status.ACTIVE), users.countByStatus(Status.ACTIVE),
        lots.sumUsableStock(today), entries.count(), exits.count(),
        a.expiringCount(), a.expiredCount(), a.depletedCount(),
        items.stream().filter(i -> i.status().equals("GREEN")).count(),
        items.stream().filter(i -> i.status().equals("YELLOW")).count(),
        items.stream().filter(i -> i.status().equals("RED")).count(),
        chart(today), lowest,
        movements.findTop8ByOrderByIdDesc().stream().map(Mappers::movement).toList(),
        topAlerts);
  }

  private List<DashboardDto.DailyPoint> chart(LocalDate today) {
    LocalDate from = today.minusDays(CHART_DAYS - 1L);
    Map<LocalDate, BigDecimal> in = toMap(entries.dailyTotals(from));
    Map<LocalDate, BigDecimal> out = toMap(exits.dailyTotals(from));
    List<DashboardDto.DailyPoint> points = new ArrayList<>();
    for (int i = 0; i < CHART_DAYS; i++) {
      LocalDate d = from.plusDays(i);
      points.add(new DashboardDto.DailyPoint(d, in.getOrDefault(d, BigDecimal.ZERO), out.getOrDefault(d, BigDecimal.ZERO)));
    }
    return points;
  }

  private Map<LocalDate, BigDecimal> toMap(List<Object[]> rows) {
    Map<LocalDate, BigDecimal> m = new HashMap<>();
    for (Object[] r : rows) m.put((LocalDate) r[0], (BigDecimal) r[1]);
    return m;
  }
}
