package com.inventrack.service;

import com.inventrack.dto.AlertDto;
import com.inventrack.dto.InventoryDto;
import com.inventrack.entity.Lot;
import com.inventrack.repository.LotRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class AlertService {
  private final LotRepository lots;
  private final InventoryService inventory;
  private final Clock clock;

  public AlertService(LotRepository lots, InventoryService inventory, Clock clock) {
    this.lots = lots; this.inventory = inventory; this.clock = clock;
  }

  /** Alertas calculadas con las fechas reales de los lotes. type opcional: EXPIRING, EXPIRED, DEPLETED, LOW_STOCK. */
  @Transactional(readOnly = true)
  public AlertDto.Response get(Integer days, String type) {
    LocalDate today = LocalDate.now(clock);
    int window = days == null ? inventory.defaultDays() : days;
    List<AlertDto.Item> items = new ArrayList<>();

    List<Lot> expired = lots.findExpired(today);
    expired.forEach(l -> items.add(lotItem("EXPIRED", "RED", l, today, "Lote vencido con " + qty(l.getAvailableQuantity()) + " " + l.getProduct().getUnit() + " en inventario")));

    List<Lot> expiring = lots.findExpiringBetween(today, today.plusDays(window));
    expiring.forEach(l -> {
      long left = ChronoUnit.DAYS.between(today, l.getExpirationDate());
      items.add(lotItem("EXPIRING", "YELLOW", l, today, left == 0 ? "Vence hoy" : "Vence en " + left + (left == 1 ? " día" : " días")));
    });

    List<Lot> depleted = lots.findDepleted(PageRequest.of(0, 50));
    depleted.forEach(l -> items.add(lotItem("DEPLETED", "YELLOW", l, today, "Lote agotado")));

    List<InventoryDto.Item> low = inventory.list(null, window).stream().filter(InventoryDto.Item::lowStock).toList();
    low.forEach(i -> items.add(new AlertDto.Item("LOW_STOCK", i.stock().signum() <= 0 ? "RED" : "YELLOW", i.productId(), i.name(), null, null,
        i.stock(), null, null, "Existencias bajas: " + qty(i.stock()) + " de un mínimo de " + qty(i.minStock()) + " " + i.unit())));

    List<AlertDto.Item> filtered = (type == null || type.isBlank()) ? items : items.stream().filter(i -> i.type().equalsIgnoreCase(type.trim())).toList();
    return new AlertDto.Response(expiring.size(), expired.size(), (int) lots.countByAvailableQuantityLessThanEqual(BigDecimal.ZERO), low.size(), window, filtered);
  }

  private AlertDto.Item lotItem(String type, String severity, Lot l, LocalDate today, String message) {
    return new AlertDto.Item(type, severity, l.getProduct().getId(), l.getProduct().getName(), l.getId(), l.getLotNumber(),
        l.getAvailableQuantity(), l.getExpirationDate(), ChronoUnit.DAYS.between(today, l.getExpirationDate()), message);
  }

  private static String qty(BigDecimal v) { return v.stripTrailingZeros().toPlainString(); }
}
