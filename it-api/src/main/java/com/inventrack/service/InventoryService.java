package com.inventrack.service;

import com.inventrack.dto.InventoryDto;
import com.inventrack.entity.Lot;
import com.inventrack.entity.Product;
import com.inventrack.entity.Status;
import com.inventrack.repository.LotRepository;
import com.inventrack.repository.ProductRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InventoryService {
  private final ProductRepository products;
  private final LotRepository lots;
  private final Clock clock;
  private final int defaultDays;

  public InventoryService(ProductRepository products, LotRepository lots, Clock clock, @Value("${app.alerts.expiring-days:7}") int defaultDays) {
    this.products = products; this.lots = lots; this.clock = clock; this.defaultDays = defaultDays;
  }

  public int defaultDays() { return defaultDays; }

  /** Existencias por producto activo, calculadas desde los lotes reales. */
  @Transactional(readOnly = true)
  public List<InventoryDto.Item> list(String q, Integer days) {
    LocalDate today = LocalDate.now(clock);
    LocalDate limit = today.plusDays(days == null ? defaultDays : days);
    Specification<Product> spec = (root, cq, cb) -> {
      List<Predicate> ps = new ArrayList<>();
      ps.add(cb.equal(root.get("status"), Status.ACTIVE));
      if (q != null && !q.isBlank()) {
        String like = "%" + q.trim().toLowerCase() + "%";
        ps.add(cb.or(cb.like(cb.lower(root.get("name")), like), cb.like(cb.lower(root.get("code")), like)));
      }
      return cb.and(ps.toArray(new Predicate[0]));
    };
    Map<Long, List<Lot>> byProduct = lots.findAllWithStock().stream().collect(Collectors.groupingBy(l -> l.getProduct().getId()));
    return products.findAll(spec, Sort.by("name")).stream()
        .map(p -> item(p, byProduct.getOrDefault(p.getId(), List.of()), today, limit)).toList();
  }

  static InventoryDto.Item item(Product p, List<Lot> productLots, LocalDate today, LocalDate limit) {
    BigDecimal stock = BigDecimal.ZERO, expiring = BigDecimal.ZERO, expired = BigDecimal.ZERO;
    LocalDate nearest = null;
    for (Lot l : productLots) {
      if (l.getExpirationDate().isBefore(today)) {
        expired = expired.add(l.getAvailableQuantity());
        continue;
      }
      stock = stock.add(l.getAvailableQuantity());
      if (!l.getExpirationDate().isAfter(limit)) expiring = expiring.add(l.getAvailableQuantity());
      if (nearest == null || l.getExpirationDate().isBefore(nearest)) nearest = l.getExpirationDate();
    }
    boolean low = p.getMinStock().signum() > 0 && stock.compareTo(p.getMinStock()) <= 0;
    String status;
    if (expired.signum() > 0 || stock.signum() <= 0) status = "RED";
    else if (expiring.signum() > 0 || low) status = "YELLOW";
    else status = "GREEN";
    return new InventoryDto.Item(p.getId(), p.getCode(), p.getName(), p.getCategory(), p.getUnit(), p.getMinStock(),
        stock, expiring, expired, nearest, low, status);
  }
}
